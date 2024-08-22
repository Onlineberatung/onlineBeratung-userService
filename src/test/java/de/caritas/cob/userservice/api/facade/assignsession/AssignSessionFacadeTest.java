package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FEEDBACKSESSION_WITH_CONSULTANT;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignSessionFacadeTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks AssignSessionFacade assignSessionFacade;
  @Mock RocketChatFacade rocketChatFacade;
  @Mock ConsultingTypeManager consultingTypeManager;
  @Mock ConsultantService consultantService;
  @Mock RocketChatRollbackService rocketChatRollbackService;
  @Mock SessionService sessionService;
  @Mock AssignEnquiryFacade assignEnquiryFacade;
  @Mock RocketChatService rocketChatService;
  @Mock CreateEnquiryMessageFacade createEnquiryMessageFacade;
  @Mock ConsultantAgencyService consultantAgencyService;

  @Mock
  @SuppressWarnings("unused")
  KeycloakService keycloakService;

  @Mock LogService logService;
  @Mock EmailNotificationFacade emailNotificationFacade;
  @Mock AuthenticatedUser authenticatedUser;
  @Mock SessionToConsultantVerifier sessionToConsultantVerifier;
  @Mock UnauthorizedMembersProvider unauthorizedMembersProvider;
  @Mock StatisticsService statisticsService;
  @Mock HttpServletRequest httpServletRequest;

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
    Session session = easyRandom.nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    ConsultantAgency consultantAgency = easyRandom.nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("consultantRcId");
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString()))
        .thenReturn(
            asList(
                new GroupMemberDTO("userRcId", null, "name", null, null),
                new GroupMemberDTO("consultantRcId", null, "name", null, null),
                new GroupMemberDTO("otherRcId", null, "name", null, null)));
    Consultant consultantToRemove = easyRandom.nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    var consultantToKeep = easyRandom.nextObject(Consultant.class);

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
                .removeUserFromGroupIgnoreGroupNotFound(
                    consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroupIgnoreGroupNotFound(
                    consultantToRemove.getRocketChatId(), session.getGroupId()));
    verify(this.emailNotificationFacade, times(1))
        .sendAssignEnquiryEmailNotification(any(), any(), any(), any());
  }

  @Test
  public void assignSession_Should_DeleteOldFeedbackChat_When_ItExists()
      throws CreateEnquiryException {
    Session session = easyRandom.nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    session.setFeedbackGroupId("oldFeedbackGroupId");
    ConsultantAgency consultantAgency = easyRandom.nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("newConsultantRcId");
    Consultant consultantToRemove = easyRandom.nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(createEnquiryMessageFacade.createRcFeedbackGroup(
            eq(session), eq(session.getGroupId()), any()))
        .thenReturn("newFeedbackGroupId");

    this.assignSessionFacade.assignSession(session, consultant, CONSULTANT);

    verify(rocketChatService, times(1)).deleteGroupAsSystemUser("oldFeedbackGroupId");
  }

  @Test
  public void assignSession_Should_CreateNewFeedbackChat_When_ConsultingTypeHasFeedback()
      throws CreateEnquiryException {
    Session session = easyRandom.nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    session.setFeedbackGroupId("oldFeedbackGroupId");
    ConsultantAgency consultantAgency = easyRandom.nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("newConsultantRcId");
    Consultant consultantToRemove = easyRandom.nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(createEnquiryMessageFacade.createRcFeedbackGroup(
            eq(session), eq(session.getGroupId()), any()))
        .thenReturn("newFeedbackGroupId");
    when(rocketChatFacade.retrieveRocketChatMembers(any())).thenReturn(new ArrayList<>());

    this.assignSessionFacade.assignSession(session, consultant, CONSULTANT);

    assertThat(session.getFeedbackGroupId(), is("newFeedbackGroupId"));
    verify(sessionService, times(2)).saveSession(session);
  }

  @Test
  public void assignSession_ShouldNot_removeTeamMembers_When_sessionIsTeamSession() {
    Session session = easyRandom.nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    ConsultantAgency consultantAgency = easyRandom.nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
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
    Consultant consultantToRemove = easyRandom.nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    var consultantToKeep = easyRandom.nextObject(Consultant.class);

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
                .removeUserFromGroupIgnoreGroupNotFound(
                    consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroupIgnoreGroupNotFound(
                    "teamConsultantRcId", session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroupIgnoreGroupNotFound(
                    "teamConsultantRcId", session.getFeedbackGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroupIgnoreGroupNotFound(
                    "teamConsultantRcId2", session.getGroupId()));
    verifyAsync(
        a ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroupIgnoreGroupNotFound(
                    "teamConsultantRcId2", session.getFeedbackGroupId()));
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
    ConsultantAgency consultantAgency = easyRandom.nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("newConsultantRcId");
    Consultant consultantToRemove = easyRandom.nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(httpServletRequest.getRequestURI()).thenReturn(RandomStringUtils.randomAlphanumeric(32));
    when(httpServletRequest.getHeader("Referer"))
        .thenReturn(RandomStringUtils.randomAlphanumeric(32));

    this.assignSessionFacade.assignSession(session, consultant, CONSULTANT);

    verify(statisticsService, times(1)).fireEvent(any(AssignSessionStatisticsEvent.class));

    var captor = ArgumentCaptor.forClass(AssignSessionStatisticsEvent.class);
    verify(statisticsService).fireEvent(captor.capture());

    var event = captor.getValue();
    var userId = requireNonNull(getField(event, "userId")).toString();
    assertThat(userId, is(consultant.getId()));
    var userRole = requireNonNull(getField(event, "userRole")).toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    var sessionId = Long.valueOf(requireNonNull(getField(event, "sessionId").toString()));
    assertThat(sessionId, is(session.getId()));

    assertEquals(httpServletRequest.getRequestURI(), event.getRequestUri());
    assertEquals(httpServletRequest.getHeader("Referer"), event.getRequestReferer());
    assertEquals(authenticatedUser.getUserId(), event.getRequestUserId());
  }

  @Test
  public void assignSession_Should_FireAssignSessionStatisticsEventWithoutOptionalArgs() {
    var session = easyRandom.nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);

    var consultantAgency = easyRandom.nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    var consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("newConsultantRcId");

    var consultantToRemove = easyRandom.nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");

    when(authenticatedUser.getUserId()).thenReturn("authenticatedUserId");
    var extendedConsultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assignSessionFacade.assignSession(session, consultant, CONSULTANT);

    verify(statisticsService).fireEvent(any(AssignSessionStatisticsEvent.class));

    var captor = ArgumentCaptor.forClass(AssignSessionStatisticsEvent.class);
    verify(statisticsService).fireEvent(captor.capture());

    var event = captor.getValue();
    var userId = requireNonNull(getField(event, "userId")).toString();
    assertThat(userId, is(consultant.getId()));
    var userRole = requireNonNull(getField(event, "userRole")).toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    var sessionId = Long.valueOf(requireNonNull(getField(event, "sessionId")).toString());
    assertThat(sessionId, is(session.getId()));

    assertNull(event.getRequestUri());
    assertNull(event.getRequestReferer());
  }
}
