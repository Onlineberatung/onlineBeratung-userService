package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FEEDBACKSESSION_WITH_CONSULTANT;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AssignSessionFacadeTest {

  @InjectMocks AssignSessionFacade assignSessionFacade;
  @Mock RocketChatFacade rocketChatFacade;
  @Mock ConsultingTypeManager consultingTypeManager;
  @Mock ConsultantService consultantService;
  @Mock RocketChatRollbackService rocketChatRollbackService;
  @Mock SessionService sessionService;

  @Mock
  @SuppressWarnings("unused")
  KeycloakService keycloakService;

  @Mock LogService logService;
  @Mock EmailNotificationFacade emailNotificationFacade;
  @Mock AuthenticatedUser authenticatedUser;
  @Mock SessionToConsultantVerifier sessionToConsultantVerifier;
  @Mock UnauthorizedMembersProvider unauthorizedMembersProvider;
  @Mock StatisticsService statisticsService;

  @Test
  public void
      assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddConsultantToRcGroupFails_WhenSessionIsNoEnquiry() {
    var exception = new InternalServerErrorException(RandomStringUtils.random(16));
    doThrow(exception)
        .when(rocketChatFacade)
        .addUserToRocketChatGroup(
            CONSULTANT_WITH_AGENCY.getRocketChatId(), FEEDBACKSESSION_WITH_CONSULTANT.getGroupId());

    var thrown =
        assertThrows(
            InternalServerErrorException.class,
            () ->
                assignSessionFacade.assignSession(
                    FEEDBACKSESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY, CONSULTANT));

    assertEquals(exception.getMessage(), thrown.getMessage());
    verify(sessionToConsultantVerifier, times(1))
        .verifyPreconditionsForAssignment(
            argThat(
                consultantSessionDTO ->
                    consultantSessionDTO.getConsultant().equals(CONSULTANT_WITH_AGENCY)
                        && consultantSessionDTO
                            .getSession()
                            .equals(FEEDBACKSESSION_WITH_CONSULTANT)));
  }

  @Test
  public void assignSession_Should_removeAllUnauthorizedMembers_When_sessionIsNotATeamSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("consultantRcId");
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString()))
        .thenReturn(
            asList(
                new GroupMemberDTO("userRcId", null, "name", null, null),
                new GroupMemberDTO("consultantRcId", null, "name", null, null),
                new GroupMemberDTO("otherRcId", null, "name", null, null)));
    Consultant consultantToRemove = new EasyRandom().nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));
    var consultantToKeep = new EasyRandom().nextObject(Consultant.class);

    assignSessionFacade.assignSession(session, consultant, consultantToKeep);

    verify(sessionToConsultantVerifier, times(1))
        .verifyPreconditionsForAssignment(
            argThat(
                consultantSessionDTO ->
                    consultantSessionDTO.getConsultant().equals(consultant)
                        && consultantSessionDTO.getSession().equals(session)));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroup(
                    consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
    verify(this.emailNotificationFacade, times(1))
        .sendAssignEnquiryEmailNotification(any(), any(), any(), any());
  }

  @Test
  public void assignSession_ShouldNot_removeTeamMembers_When_sessionIsTeamSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("newConsultantRcId");
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString()))
        .thenReturn(
            asList(
                new GroupMemberDTO("userRcId", null, "name", null, null),
                new GroupMemberDTO("newConsultantRcId", null, "name", null, null),
                new GroupMemberDTO("otherRcId", null, "name", null, null),
                new GroupMemberDTO("teamConsultantRcId", null, "name", null, null),
                new GroupMemberDTO("teamConsultantRcId2", null, "name", null, null)));
    Consultant consultantToRemove = new EasyRandom().nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));
    var consultantToKeep = new EasyRandom().nextObject(Consultant.class);

    assignSessionFacade.assignSession(session, consultant, consultantToKeep);

    verify(sessionToConsultantVerifier, times(1))
        .verifyPreconditionsForAssignment(
            argThat(
                consultantSessionDTO ->
                    consultantSessionDTO.getConsultant().equals(consultant)
                        && consultantSessionDTO.getSession().equals(session)));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroup(
                    consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroup("teamConsultantRcId", session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroup("teamConsultantRcId", session.getFeedbackGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroup("teamConsultantRcId2", session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroup("teamConsultantRcId2", session.getFeedbackGroupId()));
    verifyAsync(
        a ->
            verify(this.emailNotificationFacade, times(1))
                .sendAssignEnquiryEmailNotification(any(), any(), any(), any()));
  }

  @Test
  public void assignSession_Should_FireAssignSessionStatisticsEvent() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("newConsultantRcId");
    Consultant consultantToRemove = new EasyRandom().nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");

    this.assignSessionFacade.assignSession(session, consultant, CONSULTANT);

    verify(statisticsService, times(1)).fireEvent(any(AssignSessionStatisticsEvent.class));

    ArgumentCaptor<AssignSessionStatisticsEvent> captor =
        ArgumentCaptor.forClass(AssignSessionStatisticsEvent.class);
    verify(statisticsService, times(1)).fireEvent(captor.capture());
    String userId =
        Objects.requireNonNull(ReflectionTestUtils.getField(captor.getValue(), "userId"))
            .toString();
    assertThat(userId, is(consultant.getId()));
    String userRole =
        Objects.requireNonNull(ReflectionTestUtils.getField(captor.getValue(), "userRole"))
            .toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    Long sessionId =
        Long.valueOf(
            Objects.requireNonNull(
                ReflectionTestUtils.getField(captor.getValue(), "sessionId").toString()));
    assertThat(sessionId, is(session.getId()));
  }
}
