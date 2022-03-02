package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.FEEDBACKSESSION_WITH_CONSULTANT;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import java.util.Objects;
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

  @InjectMocks
  AssignSessionFacade assignSessionFacade;
  @Mock
  RocketChatFacade rocketChatFacade;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  ConsultantService consultantService;
  @Mock
  RocketChatRollbackService rocketChatRollbackService;
  @Mock
  SessionService sessionService;
  @Mock
  KeycloakAdminClientService keycloakAdminClientService;
  @Mock
  LogService logService;
  @Mock
  EmailNotificationFacade emailNotificationFacade;
  @Mock
  AuthenticatedUser authenticatedUser;
  @Mock
  SessionToConsultantVerifier sessionToConsultantVerifier;
  @Mock
  UnauthorizedMembersProvider unauthorizedMembersProvider;
  @Mock
  StatisticsService statisticsService;

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddConsultantToRcGroupFails_WhenSessionIsNoEnquiry() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade).addUserToRocketChatGroup(
        CONSULTANT_WITH_AGENCY.getRocketChatId(), FEEDBACKSESSION_WITH_CONSULTANT.getGroupId());

    assignSessionFacade.assignSession(FEEDBACKSESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(), any());
    verify(sessionToConsultantVerifier, times(1)).verifyPreconditionsForAssignment(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(CONSULTANT_WITH_AGENCY)
                && consultantSessionDTO.getSession().equals(FEEDBACKSESSION_WITH_CONSULTANT)));
    verify(rocketChatRollbackService, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), any());
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
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString())).thenReturn(asList(
        new GroupMemberDTO("userRcId", null, "name", null, null),
        new GroupMemberDTO("consultantRcId", null, "name", null, null),
        new GroupMemberDTO("otherRcId", null, "name", null, null)
    ));
    Consultant consultantToRemove = new EasyRandom().nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));

    this.assignSessionFacade.assignSession(session, consultant);

    verify(sessionToConsultantVerifier, times(1)).verifyPreconditionsForAssignment(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(consultant)
                && consultantSessionDTO.getSession().equals(session)));
    verifyAsync(a -> verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(a -> verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
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
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString())).thenReturn(asList(
        new GroupMemberDTO("userRcId", null, "name", null, null),
        new GroupMemberDTO("newConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("otherRcId", null, "name", null, null),
        new GroupMemberDTO("teamConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("teamConsultantRcId2", null, "name", null, null)
    ));
    Consultant consultantToRemove = new EasyRandom().nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));

    this.assignSessionFacade.assignSession(session, consultant);

    verify(sessionToConsultantVerifier, times(1)).verifyPreconditionsForAssignment(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(consultant)
                && consultantSessionDTO.getSession().equals(session)));
    verifyAsync(a -> verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(a -> verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
    verifyAsync(a -> verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId", session.getGroupId()));
    verifyAsync(a -> verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId", session.getFeedbackGroupId()));
    verifyAsync(a -> verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId2", session.getGroupId()));
    verifyAsync(a -> verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId2", session.getFeedbackGroupId()));
    verifyAsync(a -> verify(this.emailNotificationFacade, times(1))
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

    this.assignSessionFacade.assignSession(session, consultant);

    verify(statisticsService, times(1)).fireEvent(any(AssignSessionStatisticsEvent.class));

    ArgumentCaptor<AssignSessionStatisticsEvent> captor = ArgumentCaptor.forClass(
        AssignSessionStatisticsEvent.class);
    verify(statisticsService, times(1)).fireEvent(captor.capture());
    String userId = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "userId")).toString();
    assertThat(userId, is(consultant.getId()));
    String userRole = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "userRole")).toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    Long sessionId = Long.valueOf(Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "sessionId").toString()));
    assertThat(sessionId, is(session.getId()));
  }

}
