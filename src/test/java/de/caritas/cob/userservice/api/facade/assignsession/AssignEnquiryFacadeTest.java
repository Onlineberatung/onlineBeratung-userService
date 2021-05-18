package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.FEEDBACKSESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.LIST_GROUP_MEMBER_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.U25_SESSION_WITHOUT_CONSULTANT;
import static java.util.Arrays.asList;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
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
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignEnquiryFacadeTest {

  static final String FIELD_NAME_ROCKET_CHAT_TECH_USERNAME = "rocketChatTechUserUsername";
  static final String FIELD_VALUE_ROCKET_CHAT_TECH_USERNAME = "techName";
  static final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "rocketChatSystemUserId";

  static final RocketChatAddUserToGroupException RC_ADD_USER_TO_GROUP_EXC =
      new RocketChatAddUserToGroupException(new Exception());

  @InjectMocks
  AssignEnquiryFacade assignEnquiryFacade;
  @Mock
  SessionService sessionService;
  @Mock
  RocketChatFacade rocketChatFacade;
  @Mock
  KeycloakAdminClientService keycloakAdminClientService;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  ConsultantService consultantService;
  @Mock
  RocketChatRollbackService rocketChatRollbackService;
  @Mock
  SessionToConsultantVerifier sessionToConsultantVerifier;
  @Mock
  LogService logService;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(assignEnquiryFacade, FIELD_NAME_ROCKET_CHAT_TECH_USERNAME,
        FIELD_VALUE_ROCKET_CHAT_TECH_USERNAME);
    setField(assignEnquiryFacade, FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID,
        ROCKET_CHAT_SYSTEM_USER_ID);
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndNotRemoveSystemUser() {
    assignEnquiryFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(FEEDBACKSESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
  }

  private void verifyConsultantAndSessionHaveBeenChecked(Session session, Consultant consultant) {
    verify(sessionToConsultantVerifier, times(1)).verifySessionIsNotInProgress(
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

    assignEnquiryFacade.assignEnquiry(SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    verify(rocketChatFacade, times(1)).removeSystemMessagesFromRocketChatGroup(anyString());
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromFeedbackGroup_WhenSessionIsFeedbackSession() {
    assignEnquiryFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatFacade, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    verify(rocketChatFacade, times(1)).retrieveRocketChatMembers(Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenUpdateSessionFails() {
    doThrow(new InternalServerErrorException("")).when(sessionService)
        .updateConsultantAndStatusForSession(Mockito.any(), Mockito.any(), Mockito.any());

    assignEnquiryFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(FEEDBACKSESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logService, times(1)).logInternalServerError(anyString(), any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRCRemoveGroupMembersFails() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(anyString());

    assignEnquiryFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        eq(U25_SESSION_WITHOUT_CONSULTANT), eq(CONSULTANT_WITH_AGENCY),
        eq(SessionStatus.IN_PROGRESS));
    verify(rocketChatRollbackService, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenSessionRollbackFails() {
    doThrow(new InternalServerErrorException("")).when(sessionService)
        .updateConsultantAndStatusForSession(any(), any(), any());

    assignEnquiryFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(FEEDBACKSESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logService, atLeast(2)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, FEEDBACKSESSION_WITHOUT_CONSULTANT.getConsultant(),
        FEEDBACKSESSION_WITHOUT_CONSULTANT.getStatus());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddPeerConsultantToFeedbackGroupFails() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade)
        .addUserToRocketChatGroup(ROCKETCHAT_ID,
            RC_FEEDBACK_GROUP_ID);

    assignEnquiryFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(RC_ADD_USER_TO_GROUP_EXC));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackService, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemoveSystemMessagesFromGroupFails() {
    doThrow(new InternalServerErrorException("error")).when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(Mockito.any());

    assignEnquiryFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logService, times(1)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackService, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemoveSystemMessagesFromFeedbackChatFails() {
    doThrow(new InternalServerErrorException("error")).when(rocketChatFacade)
        .removeSystemMessagesFromRocketChatGroup(Mockito.any());

    assignEnquiryFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(logService, times(1)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackService, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test
  public void assignEnquiry_Should_AddPeerConsultantToFeedbackGroup_WhenSessionHasFeedbackIsTrue() {
    assignEnquiryFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verifyConsultantAndSessionHaveBeenChecked(U25_SESSION_WITHOUT_CONSULTANT,
        CONSULTANT_WITH_AGENCY);
    verify(rocketChatFacade, times(1)).addUserToRocketChatGroup(ROCKETCHAT_ID,
        U25_SESSION_WITHOUT_CONSULTANT.getFeedbackGroupId());
  }

  @Test
  public void assignEnquiry_Should_removeOtherMembers_When_sessionIsNotATeamSession() {
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
    when(this.consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(consultantToRemove));

    this.assignEnquiryFacade.assignEnquiry(session, consultant);

    verifyConsultantAndSessionHaveBeenChecked(session, consultant);
    verify(this.rocketChatFacade, times(1))
        .removeUserFromGroup(eq(consultantToRemove.getRocketChatId()), eq(session.getGroupId()));
    verify(this.rocketChatFacade, times(1))
        .removeUserFromGroup(eq(consultantToRemove.getRocketChatId()),
            eq(session.getFeedbackGroupId()));
  }

}
