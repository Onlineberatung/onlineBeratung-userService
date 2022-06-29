package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.NEW;
import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FEEDBACKSESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.LIST_GROUP_MEMBER_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.U25_SESSION_WITHOUT_CONSULTANT;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import java.util.Objects;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AssignEnquiryFacadeTest {

  static final RocketChatAddUserToGroupException RC_ADD_USER_TO_GROUP_EXC =
      new RocketChatAddUserToGroupException(new Exception());

  @InjectMocks
  AssignEnquiryFacade assignEnquiryFacade;
  @Mock
  SessionService sessionService;
  @Mock
  RocketChatFacade rocketChatFacade;
  @Mock
  @SuppressWarnings("unused")
  KeycloakService keycloakService;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  ConsultantService consultantService;
  @Mock
  SessionToConsultantVerifier sessionToConsultantVerifier;
  @Mock
  Logger logger;
  @Mock
  UnauthorizedMembersProvider unauthorizedMembersProvider;
  @Mock
  StatisticsService statisticsService;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndNotRemoveSystemUser() {
    assignEnquiryFacade
        .assignRegisteredEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(FEEDBACKSESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
  }

  @Test
  public void assignEnquiry_Should_FireAssignSessionStatisticsEvent() {

    assignEnquiryFacade
        .assignRegisteredEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(statisticsService, times(1)).fireEvent(any(AssignSessionStatisticsEvent.class));

    ArgumentCaptor<AssignSessionStatisticsEvent> captor = ArgumentCaptor.forClass(
        AssignSessionStatisticsEvent.class);
    verify(statisticsService, times(1)).fireEvent(captor.capture());
    String userId = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "userId")).toString();
    assertThat(userId, is(CONSULTANT_WITH_AGENCY.getId()));
    String userRole = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "userRole")).toString();
    assertThat(userRole, is(UserRole.CONSULTANT.toString()));
    Long sessionId = Long.valueOf(Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "sessionId")).toString());
    assertThat(sessionId, is(FEEDBACKSESSION_WITHOUT_CONSULTANT.getId()));
  }

  private void verifyConsultantAndSessionHaveBeenChecked(Session session, Consultant consultant) {
    verify(sessionToConsultantVerifier, times(1)).verifySessionIsNew(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(consultant)
                && consultantSessionDTO.getSession().equals(session)));
    verify(sessionToConsultantVerifier, times(1)).verifyPreconditionsForAssignment(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(consultant)
                && consultantSessionDTO.getSession().equals(session)));
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromGroup() {
    when(rocketChatFacade.retrieveRocketChatMembers(anyString())).thenReturn(LIST_GROUP_MEMBER_DTO);

    assignEnquiryFacade.assignRegisteredEnquiry(SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    verifyAsync((a) -> verify(rocketChatFacade, times(1))
        .removeSystemMessagesFromRocketChatGroup(anyString()));
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromFeedbackGroup_WhenSessionIsFeedbackSession() {
    assignEnquiryFacade
        .assignRegisteredEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyAsync((a) -> verify(rocketChatFacade, times(0))
        .removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_GROUP_ID));
    verifyAsync(
        (a) -> verify(rocketChatFacade, atLeastOnce()).retrieveRocketChatMembers(Mockito.any()));
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenUpdateSessionFails() {
    doThrow(new InternalServerErrorException("")).when(sessionService)
        .updateConsultantAndStatusForSession(Mockito.any(), Mockito.any(), Mockito.any());

    assignEnquiryFacade
        .assignRegisteredEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(FEEDBACKSESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logger, times(1)).error(anyString(), anyString());
  }

  @Test
  public void assignEnquiry_Should_LogError_When_RCRemoveGroupMembersFails() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(anyString());

    assignEnquiryFacade
        .assignRegisteredEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY,
        SessionStatus.IN_PROGRESS);
    verifyAsync((a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString()));
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenSessionRollbackFails() {
    doThrow(new InternalServerErrorException("")).when(sessionService)
        .updateConsultantAndStatusForSession(any(), any(), any());

    assignEnquiryFacade
        .assignRegisteredEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(FEEDBACKSESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logger, atLeast(2)).error(anyString(), anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, FEEDBACKSESSION_WITHOUT_CONSULTANT.getConsultant(),
        FEEDBACKSESSION_WITHOUT_CONSULTANT.getStatus());
  }

  @Test
  public void assignEnquiry_Should_LogError_WhenAddPeerConsultantToFeedbackGroupFails() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade)
        .addUserToRocketChatGroup(ROCKETCHAT_ID, RC_FEEDBACK_GROUP_ID);

    assignEnquiryFacade
        .assignRegisteredEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verifyAsync((a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString()));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, IN_PROGRESS);
  }

  @Test
  public void assignEnquiry_Should_LogError_WhenRemoveSystemMessagesFromGroupFails() {
    doThrow(new InternalServerErrorException("error")).when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(Mockito.any());

    assignEnquiryFacade
        .assignRegisteredEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verifyAsync((a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString()));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, IN_PROGRESS);
  }

  @Test
  public void assignEnquiry_Should_LogError_When_RemoveSystemMessagesFromFeedbackChatFails() {
    doThrow(new InternalServerErrorException("error")).when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(Mockito.any());

    assignEnquiryFacade
        .assignRegisteredEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verifyAsync((a) -> verify(logger, times(1)).error(anyString(), anyString(), anyString()));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY, IN_PROGRESS);
  }

  @Test
  public void assignEnquiry_Should_AddPeerConsultantToFeedbackGroup_WhenSessionHasFeedbackIsTrue() {
    assignEnquiryFacade
        .assignRegisteredEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verifyAsync((a) -> verify(rocketChatFacade, times(1)).addUserToRocketChatGroup(ROCKETCHAT_ID,
        U25_SESSION_WITHOUT_CONSULTANT.getFeedbackGroupId()));
  }

  @Test
  public void assignEnquiry_Should_removeAllUnauthorizedMembers_When_sessionIsNotATeamSession() {
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
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));

    this.assignEnquiryFacade.assignRegisteredEnquiry(session, consultant);

    verifyConsultantAndSessionHaveBeenChecked(session, consultant);
    verifyAsync((a) -> verify(this.rocketChatFacade, times(1))
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync((a) -> verify(this.rocketChatFacade, times(1))
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
  }

  @Test
  public void assignEnquiry_ShouldNot_removeTeamMembers_When_sessionIsTeamSession() {
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
    when(unauthorizedMembersProvider.obtainConsultantsToRemove(any(), any(), any(), any()))
        .thenReturn(List.of(consultantToRemove));

    this.assignEnquiryFacade.assignRegisteredEnquiry(session, consultant);

    verifyConsultantAndSessionHaveBeenChecked(session, consultant);
    verifyAsync((a) -> verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getGroupId()));
    verifyAsync((a) -> verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(consultantToRemove.getRocketChatId(), session.getFeedbackGroupId()));
    verifyAsync((a) -> verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId", session.getGroupId()));
    verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId", session.getFeedbackGroupId());
    verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId2", session.getGroupId());
    verify(this.rocketChatFacade, never())
        .removeUserFromGroup("teamConsultantRcId2", session.getFeedbackGroupId());
  }

  @Test
  public void assignAnonymousEnquiry_Should_AddConsultantToGroup_WhenSessionIsAnonymousConversation() {
    assignEnquiryFacade
        .assignAnonymousEnquiry(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(1)).addUserToRocketChatGroup(ROCKETCHAT_ID,
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT.getGroupId());
  }

  @Test
  public void assignAnonymousEnquiry_Should_RemoveSystemMessagesFromGroup() {
    assignEnquiryFacade
        .assignAnonymousEnquiry(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(1)).removeSystemMessagesFromRocketChatGroup(anyString());
  }

  @Test(expected = ConflictException.class)
  public void assignAnonymousEnquiry_Should_ThrowConflictIfSessionIsInProgress() {
    var session = givenASessionInProgress();
    doThrow(new ConflictException(""))
        .when(sessionToConsultantVerifier).verifySessionIsNew(any(ConsultantSessionDTO.class));

    assignEnquiryFacade.assignAnonymousEnquiry(session, CONSULTANT_WITH_AGENCY);
  }

  @Test(expected = ConflictException.class)
  public void assignAnonymousEnquiry_Should_ThrowConflictIfSessionIsDone() {
    var session = givenADoneSession();
    doThrow(new ConflictException(""))
        .when(sessionToConsultantVerifier).verifySessionIsNew(any(ConsultantSessionDTO.class));

    assignEnquiryFacade.assignAnonymousEnquiry(session, CONSULTANT_WITH_AGENCY);
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignAnonymousEnquiry_Should_ReturnInternalServerErrorAndDoARollback_WhenAddConsultantToGroupFails() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade)
        .addUserToRocketChatGroup(ROCKETCHAT_ID, RC_GROUP_ID);

    assignEnquiryFacade
        .assignAnonymousEnquiry(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT.getConsultant(),
        ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT.getStatus());
    verify(sessionService, times(1))
        .updateConsultantAndStatusForSession(ANONYMOUS_ENQUIRY_WITHOUT_CONSULTANT, null, NEW);
  }

  private Session givenADoneSession() {
    return new Session(SESSION_ID, null, null, CONSULTING_TYPE_ID_SUCHT, ANONYMOUS, POSTCODE,
        AGENCY_ID, null, SessionStatus.DONE, nowInUtc(), RC_GROUP_ID, null, null, false, false,
        false, nowInUtc(), null, null);
  }

  private Session givenASessionInProgress() {
    return new Session(SESSION_ID, null, CONSULTANT_2, CONSULTING_TYPE_ID_SUCHT, ANONYMOUS,
        POSTCODE, AGENCY_ID, null, IN_PROGRESS, nowInUtc(), RC_GROUP_ID, null, null, false, false,
        false, nowInUtc(), null, null);
  }

}
