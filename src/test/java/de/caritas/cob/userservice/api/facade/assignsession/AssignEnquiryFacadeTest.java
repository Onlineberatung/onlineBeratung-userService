package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.NEW;
import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FEEDBACKSESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.LIST_GROUP_MEMBER_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.U25_SESSION_WITHOUT_CONSULTANT;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class AssignEnquiryFacadeTest {
  public static final long CURRENT_TENANT_ID = 1L;

  @InjectMocks AssignEnquiryFacade assignEnquiryFacade;
  @Mock SessionService sessionService;
  @Mock RocketChatFacade rocketChatFacade;

  @Mock
  @SuppressWarnings("unused")
  KeycloakService keycloakService;

  @SuppressWarnings("unused")
  @Mock
  ConsultingTypeManager consultingTypeManager;

  @Mock SessionToConsultantVerifier sessionToConsultantVerifier;
  @Mock Logger logger;
  @Mock UnauthorizedMembersProvider unauthorizedMembersProvider;
  @Mock StatisticsService statisticsService;
  @Mock TenantContextProvider tenantContextProvider;
  @Mock HttpServletRequest httpServletRequest;

  @BeforeEach
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @AfterEach
  public void tearDown() {
    TenantContext.clear();
  }

  @Test
  void assignEnquiry_Should_ReturnOKAndNotRemoveSystemUser() {
    assignEnquiryFacade.assignRegisteredEnquiry(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_GROUP_ID);
  }

  @Test
  void
      assignRegisteredEnquiry_Should_ReturnOKAndNotRemoveSystemUser_AndSkipInProgressValidationIfSkipInProgressCheckIsRequired() {
    assignEnquiryFacade.assignRegisteredEnquiry(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, true);

    verifyConsultantAndSessionHaveBeenCheckedButInProgressValidationSkipped(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_GROUP_ID);
  }

  @Test
  void assignEnquiry_Should_FireAssignSessionStatisticsEvent() {
    when(httpServletRequest.getRequestURI()).thenReturn(RandomStringUtils.randomAlphanumeric(32));
    when(httpServletRequest.getHeader("Referer"))
        .thenReturn(RandomStringUtils.randomAlphanumeric(32));

    assignEnquiryFacade.assignRegisteredEnquiry(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(statisticsService, timeout(5000)).fireEvent(any(AssignSessionStatisticsEvent.class));

    var captor = ArgumentCaptor.forClass(AssignSessionStatisticsEvent.class);
    verify(statisticsService).fireEvent(captor.capture());
    var event = captor.getValue();

    var userId = requireNonNull(getField(event, "userId")).toString();
    assertThat(userId, is(CONSULTANT_WITH_AGENCY.getId()));
    var userRole = requireNonNull(getField(event, "userRole")).toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    var sessionId = Long.valueOf(requireNonNull(getField(event, "sessionId")).toString());
    assertThat(sessionId, is(FEEDBACKSESSION_WITHOUT_CONSULTANT.getId()));
    assertEquals(httpServletRequest.getRequestURI(), event.getRequestUri());
    assertEquals(httpServletRequest.getHeader("Referer"), event.getRequestReferer());
    assertEquals(CONSULTANT_WITH_AGENCY.getId(), event.getRequestUserId());
  }

  @Test
  void assignEnquiry_Should_FireAssignSessionStatisticsEventWithoutOptionalArgs() {
    assignEnquiryFacade.assignRegisteredEnquiry(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(statisticsService, timeout(5000)).fireEvent(any(AssignSessionStatisticsEvent.class));

    var captor = ArgumentCaptor.forClass(AssignSessionStatisticsEvent.class);
    verify(statisticsService).fireEvent(captor.capture());
    var event = captor.getValue();

    var userId = requireNonNull(getField(event, "userId")).toString();
    assertThat(userId, is(CONSULTANT_WITH_AGENCY.getId()));
    var userRole = requireNonNull(getField(event, "userRole")).toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    var sessionId = Long.valueOf(requireNonNull(getField(event, "sessionId")).toString());
    assertThat(sessionId, is(FEEDBACKSESSION_WITHOUT_CONSULTANT.getId()));
    assertNull(event.getRequestUri());
    assertNull(event.getRequestReferer());
    assertEquals(CONSULTANT_WITH_AGENCY.getId(), event.getRequestUserId());
  }

  private void verifyConsultantAndSessionHaveBeenChecked(Session session, Consultant consultant) {
    verify(sessionToConsultantVerifier, times(1))
        .verifySessionIsNotInProgress(
            argThat(
                consultantSessionDTO ->
                    consultantSessionDTO.getConsultant().equals(consultant)
                        && consultantSessionDTO.getSession().equals(session)));
    verify(sessionToConsultantVerifier, times(1))
        .verifyPreconditionsForAssignment(
            argThat(
                consultantSessionDTO ->
                    consultantSessionDTO.getConsultant().equals(consultant)
                        && consultantSessionDTO.getSession().equals(session)),
            Mockito.eq(false));
  }

  private void verifyConsultantAndSessionHaveBeenCheckedButInProgressValidationSkipped(
      Session session, Consultant consultant) {
    verify(sessionToConsultantVerifier, times(1))
        .verifyPreconditionsForAssignment(
            argThat(
                consultantSessionDTO ->
                    consultantSessionDTO.getConsultant().equals(consultant)
                        && consultantSessionDTO.getSession().equals(session)),
            Mockito.eq(true));
  }

  @Test
  void assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromGroup() {
    // given
    TenantContext.setCurrentTenant(CURRENT_TENANT_ID);
    when(rocketChatFacade.retrieveRocketChatMembers(anyString())).thenReturn(LIST_GROUP_MEMBER_DTO);

    // when
    assignEnquiryFacade.assignRegisteredEnquiry(SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    // then
    verifyConsultantAndSessionHaveBeenChecked(SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_GROUP_ID);
    verifyAsync(
        (a) ->
            verify(rocketChatFacade, times(1))
                .removeSystemMessagesFromRocketChatGroup(anyString()));
    verifyAsync(
        (a) -> verify(tenantContextProvider).setCurrentTenantContextIfMissing(CURRENT_TENANT_ID));
  }

  @Test
  void
      assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromFeedbackGroup_WhenSessionIsFeedbackSession() {
    // given
    TenantContext.setCurrentTenant(CURRENT_TENANT_ID);
    // when
    assignEnquiryFacade.assignRegisteredEnquiry(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    // then
    verifyAsync(
        (a) ->
            verify(rocketChatFacade, times(0))
                .removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_GROUP_ID));
    verifyAsync(
        (a) -> verify(rocketChatFacade, atLeastOnce()).retrieveRocketChatMembers(Mockito.any()));
    verifyAsync(
        (a) -> verify(tenantContextProvider).setCurrentTenantContextIfMissing(CURRENT_TENANT_ID));
  }

  @Test
  void assignEnquiry_Should_ReturnInternalServerError_WhenUpdateSessionFails() {
    doThrow(new InternalServerErrorException(""))
        .when(sessionService)
        .updateConsultantAndStatusForSession(Mockito.any(), Mockito.any(), Mockito.any());

    assertThrows(
        InternalServerErrorException.class,
        () -> {
          assignEnquiryFacade.assignRegisteredEnquiry(
              FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
        });

    verifyConsultantAndSessionHaveBeenChecked(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
  }

  @Test
  void assignEnquiry_Should_LogError_When_RCRemoveGroupMembersFails() {
    doThrow(new InternalServerErrorException(""))
        .when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(anyString());

    assignEnquiryFacade.assignRegisteredEnquiry(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(
            U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, SessionStatus.IN_PROGRESS);
    verifyAsync(
        (a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString(), anyString()));
  }

  @Test
  void assignEnquiry_Should_ReturnInternalServerError_WhenSessionRollbackFails() {
    doThrow(new InternalServerErrorException(""))
        .when(sessionService)
        .updateConsultantAndStatusForSession(any(), any(), any());

    assertThrows(
        InternalServerErrorException.class,
        () -> {
          assignEnquiryFacade.assignRegisteredEnquiry(
              FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
        });
  }

  @Test
  void assignEnquiry_Should_LogError_WhenAddPeerConsultantToFeedbackGroupFails() {
    doThrow(new InternalServerErrorException(""))
        .when(rocketChatFacade)
        .addUserToRocketChatGroup(ROCKETCHAT_ID, RC_FEEDBACK_GROUP_ID);

    assignEnquiryFacade.assignRegisteredEnquiry(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verifyAsync(
        (a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString(), anyString()));
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(
            U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, IN_PROGRESS);
  }

  @Test
  void assignEnquiry_Should_LogError_WhenRemoveSystemMessagesFromGroupFails() {
    doThrow(new InternalServerErrorException("error"))
        .when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(Mockito.any());

    assignEnquiryFacade.assignRegisteredEnquiry(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verifyAsync(
        (a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString(), anyString()));
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(
            U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, IN_PROGRESS);
  }

  @Test
  void assignEnquiry_Should_LogError_When_RemoveSystemMessagesFromFeedbackChatFails() {
    doThrow(new InternalServerErrorException("error"))
        .when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(Mockito.any());

    assignEnquiryFacade.assignRegisteredEnquiry(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verifyAsync(
        (a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString(), anyString()));
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(
            U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, IN_PROGRESS);
  }

  @Test
  void assignEnquiry_Should_AddPeerConsultantToFeedbackGroup_WhenSessionHasFeedbackIsTrue() {
    assignEnquiryFacade.assignRegisteredEnquiry(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verifyAsync(
        (a) ->
            verify(rocketChatFacade, times(1))
                .addUserToRocketChatGroup(
                    ROCKETCHAT_ID, U25_SESSION_WITHOUT_CONSULTANT.getFeedbackGroupId()));
  }

  @Test
  void assignEnquiry_Should_removeAllUnauthorizedMembers_When_sessionIsNotATeamSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(CURRENT_TENANT_ID);
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(CURRENT_TENANT_ID);
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
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));

    this.assignEnquiryFacade.assignRegisteredEnquiry(session, consultant);

    verifyConsultantAndSessionHaveBeenChecked(session, consultant);
    verifyAsync(
        (a) ->
            verify(this.rocketChatFacade, times(1))
                .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(
        (a) ->
            verify(this.rocketChatFacade, times(1))
                .removeUserFromGroup(
                    consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
  }

  @Test
  void assignEnquiry_ShouldNot_removeTeamMembers_When_sessionIsTeamSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(CURRENT_TENANT_ID);
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(CURRENT_TENANT_ID);
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
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));

    this.assignEnquiryFacade.assignRegisteredEnquiry(session, consultant);

    verifyConsultantAndSessionHaveBeenChecked(session, consultant);
    verifyAsync(
        (a) ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync(
        (a) ->
            verify(this.rocketChatFacade, atLeastOnce())
                .removeUserFromGroup(
                    consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
    verifyAsync(
        (a) ->
            verify(this.rocketChatFacade, never())
                .removeUserFromGroup("teamConsultantRcId", session.getGroupId()));
    verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId", session.getFeedbackGroupId());
    verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId2", session.getGroupId());
    verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId2", session.getFeedbackGroupId());
  }

  @Test
  void assignAnonymousEnquiry_Should_AddConsultantToGroup_WhenSessionIsAnonymousConversation() {
    assignEnquiryFacade.assignAnonymousEnquiry(
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(1))
        .addUserToRocketChatGroup(ROCKETCHAT_ID, ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT.getGroupId());
  }

  @Test
  void assignAnonymousEnquiry_Should_RemoveSystemMessagesFromGroup() {
    assignEnquiryFacade.assignAnonymousEnquiry(
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(1)).removeSystemMessagesFromRocketChatGroup(anyString());
  }

  @Test
  void
      assignAnonymousEnquiry_Should_ReturnInternalServerErrorAndDoARollback_WhenAddConsultantToGroupFails() {
    doThrow(new InternalServerErrorException(""))
        .when(rocketChatFacade)
        .addUserToRocketChatGroup(ROCKETCHAT_ID, RC_GROUP_ID);

    assertThrows(
        InternalServerErrorException.class,
        () -> {
          assignEnquiryFacade.assignAnonymousEnquiry(
              ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
        });

    verifyConsultantAndSessionHaveBeenChecked(
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(
            ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT,
            ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT.getConsultant(),
            ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT.getStatus());
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, null, NEW);
  }
}
