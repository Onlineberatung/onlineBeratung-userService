package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_NO_RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.ENQUIRY_SESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.FEEDBACKSESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.FEEDBACKSESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.LIST_GROUP_MEMBER_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.MAIN_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.MAIN_CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID_MAIN_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT_NO_RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.U25_SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.U25_SESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.authorization.Authority;
import de.caritas.cob.userservice.api.exception.UpdateSessionException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.userservice.api.service.helper.RocketChatRollbackHelper;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignSessionFacadeTest {

  private final String FIELD_NAME_ROCKET_CHAT_TECH_USERNAME = "rocketChatTechUserUsername";
  private final String FIELD_VALUE_ROCKET_CHAT_TECH_USERNAME = "techName";
  private final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "rocketChatSystemUserId";

  private final RocketChatAddUserToGroupException RC_ADD_USER_TO_GROUP_EXC =
      new RocketChatAddUserToGroupException(new Exception());

  @InjectMocks
  AssignSessionFacade assignSessionFacade;
  @Mock
  SessionService sessionService;
  @Mock
  RocketChatService rocketChatService;
  @Mock
  KeycloakAdminClientHelper keycloakHelper;
  @Mock
  ConsultantService consultantService;
  @Mock
  RocketChatRollbackHelper rocketChatRollbackHelper;
  @Mock
  LogService logService;
  @Mock
  EmailNotificationFacade emailNotificationFacade;
  @Mock
  AuthenticatedUser authenticatedUser;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(assignSessionFacade,
        assignSessionFacade.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_TECH_USERNAME),
        FIELD_VALUE_ROCKET_CHAT_TECH_USERNAME);
    FieldSetter.setField(assignSessionFacade,
        assignSessionFacade.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID),
        ROCKET_CHAT_SYSTEM_USER_ID);
  }

  @Test(expected = ConflictException.class)
  public void assignSession_Should_ReturnConflict_WhenSessionIsAlreadyAssignedToThisConsultantAndSessionIsInProgress() {

    assignSessionFacade.assignSession(SESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY_2);

    verify(logService, times(1)).logAssignSessionFacadeWarning(Mockito.any());
  }

  @Test(expected = ConflictException.class)
  public void assignSession_Should_ReturnConflict_WhenSessionIsAlreadyAssignedToAnyConsultantAndSessionIsNew() {

    assignSessionFacade.assignSession(ENQUIRY_SESSION_WITH_CONSULTANT, CONSULTANT_2);

    verify(logService, times(1)).logAssignSessionFacadeWarning(Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogError_WhenUserDoesNotHaveRocketChatIdInDb() {

    assignSessionFacade.assignSession(SESSION_WITHOUT_CONSULTANT_NO_RC_USER_ID, CONSULTANT);

    verify(logService, times(1)).logAssignSessionFacadeError(anyString());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogError_WhenConsultantDoesNotHaveRocketChatIdInDb() {

    assignSessionFacade.assignSession(SESSION_WITHOUT_CONSULTANT, CONSULTANT_NO_RC_USER_ID);

    verify(logService, times(1)).logAssignSessionFacadeError(anyString());
  }

  @Test(expected = ForbiddenException.class)
  public void assignEnquiry_Should_ReturnForbidden_WhenConsultantIsNotAssignedToCorrectAgency() {

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY_2);

    verify(logService, times(1)).logAssignSessionFacadeWarning(Mockito.any());
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndNotRemoveSystemUser()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatService, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);

  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromGroup()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignEnquiry(SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatService, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    verify(rocketChatService, times(1)).removeSystemMessages(anyString(), Mockito.any(),
        Mockito.any());
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndRemoveSystemMessagesFromFeedbackGroup_WhenSessionIsFeedbackSession()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatService, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    verify(rocketChatService, times(2)).removeSystemMessages(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @Test
  public void assignSession_Should_ReturnOKAndRemovePreviousPeerConsultantFromFeedbackGroup_WhenSessionIsFeedbackSessionAndNoEnquiry()
      throws Exception {

    doNothing().when(rocketChatService).addUserToGroup(CONSULTANT_WITH_AGENCY.getRocketChatId(),
        U25_SESSION_WITH_CONSULTANT.getGroupId());
    when(rocketChatService.getMembersOfGroup(anyString())).thenReturn(LIST_GROUP_MEMBER_DTO);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignSession(U25_SESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatService, times(1)).removeUserFromGroup(
        U25_SESSION_WITH_CONSULTANT.getConsultant().getRocketChatId(),
        U25_SESSION_WITH_CONSULTANT.getFeedbackGroupId());
  }

  @Test
  public void assignEnquiry_Should_ReturnOKAndNotRemoveAllPeerUsersAndNotAllMainConsultantsOnConsultingTypeU25()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT));
    when(consultantService.getConsultantByRcUserId(RC_USER_ID_MAIN_CONSULTANT))
        .thenReturn(Optional.of(MAIN_CONSULTANT));
    when(keycloakHelper.userHasAuthority(MAIN_CONSULTANT_ID, Authority.VIEW_ALL_PEER_SESSIONS))
        .thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatService, times(0)).removeUserFromGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    verify(rocketChatService, times(0)).removeUserFromGroup(RC_USER_ID_MAIN_CONSULTANT,
        RC_GROUP_ID);
    verify(rocketChatService, times(1)).removeUserFromGroup(RC_USER_ID_2, RC_GROUP_ID);

  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddConsultantToRcGroupFails_WhenSessionIsNoEnquiry()
      throws Exception {

    RocketChatAddUserToGroupException rcAddUserEx = new RocketChatAddUserToGroupException(ERROR);

    doThrow(rcAddUserEx).when(rocketChatService).addUserToGroup(
        CONSULTANT_WITH_AGENCY.getRocketChatId(), FEEDBACKSESSION_WITH_CONSULTANT.getGroupId());

    assignSessionFacade.assignSession(FEEDBACKSESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(rcAddUserEx));
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemovePreviousConsultantFromRcFeedbackGroupFails_WhenSessionIsNoEnquiry()
      throws Exception {

    RocketChatRemoveUserFromGroupException rcRemoveUserEx =
        new RocketChatRemoveUserFromGroupException(ERROR);

    doNothing().when(rocketChatService).addUserToGroup(CONSULTANT_WITH_AGENCY.getRocketChatId(),
        U25_SESSION_WITH_CONSULTANT.getGroupId());
    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);

    doThrow(rcRemoveUserEx).doNothing().when(rocketChatService).removeUserFromGroup(
        U25_SESSION_WITH_CONSULTANT.getConsultant().getRocketChatId(),
        U25_SESSION_WITH_CONSULTANT.getFeedbackGroupId());

    assignSessionFacade.assignSession(U25_SESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(rcRemoveUserEx));
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemovePreviousConsultantFromRcFeedbackGrouptAddTechUserToGroupThrowsException_WhenSessionIsNoEnquiry()
      throws Exception {

    RocketChatAddUserToGroupException rcaddTechUserUserEx =
        new RocketChatAddUserToGroupException(ERROR);

    doThrow(rcaddTechUserUserEx).when(rocketChatService)
        .addTechnicalUserToGroup(anyString());

    assignSessionFacade.assignSession(U25_SESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(rcaddTechUserUserEx));
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemovePreviousConsultantFromRcFeedbackGrouptAddTechUserToGroupFails_WhenSessionIsNoEnquiry()
      throws Exception {

    doThrow(new RocketChatAddUserToGroupException(ERROR)).when(rocketChatService)
        .addTechnicalUserToGroup(anyString());

    assignSessionFacade.assignSession(U25_SESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemovePreviousConsultantFromRcFeedbackGroupCouldNotGetKeycloakRoles_WhenSessionIsNoEnquiry()
      throws Exception {

    KeycloakException keycloakEx = new KeycloakException(ERROR);

    doNothing().when(rocketChatService).addUserToGroup(CONSULTANT_WITH_AGENCY.getRocketChatId(),
        U25_SESSION_WITH_CONSULTANT.getGroupId());
    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    doThrow(keycloakEx).when(keycloakHelper).userHasAuthority(
        U25_SESSION_WITH_CONSULTANT.getConsultant().getId(), Authority.VIEW_ALL_FEEDBACK_SESSIONS);

    assignSessionFacade.assignSession(U25_SESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(keycloakEx));
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenTechUserCouldNotBeRemoved()
      throws Exception {

    doThrow(new RocketChatRemoveUserFromGroupException("error")).when(rocketChatService)
        .removeTechnicalUserFromGroup(anyString());

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, FEEDBACKSESSION_WITHOUT_CONSULTANT.getConsultant(),
        FEEDBACKSESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenAddTechUserToGroupFails()
      throws Exception {

    doThrow(new RocketChatAddUserToGroupException(ERROR)).when(rocketChatService)
        .addTechnicalUserToGroup(anyString());

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenUpdateSessionFails()
      throws UpdateSessionException {

    UpdateSessionException updateSessionEx = new UpdateSessionException(new Exception());

    doThrow(updateSessionEx).when(sessionService).updateConsultantAndStatusForSession(Mockito.any(),
        Mockito.any(), Mockito.any());

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(updateSessionEx));
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddTechUserToFeedbackGroupFails()
      throws Exception {

    RocketChatAddUserToGroupException rcAddUserEx =
        new RocketChatAddUserToGroupException(new Exception());

    doThrow(rcAddUserEx).when(rocketChatService).addTechnicalUserToGroup(anyString());

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(rcAddUserEx));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, FEEDBACKSESSION_WITHOUT_CONSULTANT.getConsultant(),
        FEEDBACKSESSION_WITHOUT_CONSULTANT.getStatus());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRCGetGroupMembersFails()
      throws Exception {

    RocketChatGetGroupMembersException rcGetMembersEx =
        new RocketChatGetGroupMembersException(new Exception());

    doThrow(rcGetMembersEx).when(rocketChatService).getMembersOfGroup(anyString());

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(rcGetMembersEx));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, FEEDBACKSESSION_WITHOUT_CONSULTANT.getConsultant(),
        FEEDBACKSESSION_WITHOUT_CONSULTANT.getStatus());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRCRemoveGroupMembersFails()
      throws Exception {

    RocketChatRemoveUserFromGroupException rcRemoveMembersEx =
        new RocketChatRemoveUserFromGroupException(new Exception());

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT));
    doThrow(rcRemoveMembersEx).when(rocketChatService).removeUserFromGroup(anyString(),
        anyString());

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(rcRemoveMembersEx));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenKeycloakGetRolesFails()
      throws Exception {

    KeycloakException keycloakEx = new KeycloakException(ERROR);

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT));
    doThrow(keycloakEx).when(keycloakHelper).userHasAuthority(anyString(),
        anyString());

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(keycloakEx));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndDoARollback_WhenGetConsultantFails()
      throws Exception {

    InternalServerErrorException serviceEx = new InternalServerErrorException(ERROR);

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    doThrow(serviceEx).when(consultantService).getConsultantByRcUserId(anyString());

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogError_WhenSessionRollbackFails()
      throws UpdateSessionException {

    UpdateSessionException updateSessionException = new UpdateSessionException(new Exception());

    doThrow(updateSessionException).when(sessionService).updateConsultantAndStatusForSession(
        any(), any(), any());

    assignSessionFacade.assignEnquiry(FEEDBACKSESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, atLeast(2)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        FEEDBACKSESSION_WITHOUT_CONSULTANT, FEEDBACKSESSION_WITHOUT_CONSULTANT.getConsultant(),
        FEEDBACKSESSION_WITHOUT_CONSULTANT.getStatus());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddPeerConsultantToFeedbackGroupFails()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    doThrow(RC_ADD_USER_TO_GROUP_EXC).when(rocketChatService).addUserToGroup(ROCKETCHAT_ID,
        RC_FEEDBACK_GROUP_ID);

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(),
        Mockito.eq(RC_ADD_USER_TO_GROUP_EXC));
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemoveSystemMessagesFromGroupFails()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    doThrow(new RocketChatRemoveSystemMessagesException("error")).when(rocketChatService)
        .removeSystemMessages(Mockito.any(), Mockito.any(), Mockito.any());

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignEnquiry_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenRemoveSystemMessagesFromFeedbackChatFails()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    doThrow(new RocketChatRemoveSystemMessagesException("error")).when(rocketChatService)
        .removeSystemMessages(Mockito.any(), Mockito.any(), Mockito.any());

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString());
    verify(sessionService, times(1)).updateConsultantAndStatusForSession(
        U25_SESSION_WITHOUT_CONSULTANT, U25_SESSION_WITHOUT_CONSULTANT.getConsultant(),
        U25_SESSION_WITHOUT_CONSULTANT.getStatus());
    verify(rocketChatRollbackHelper, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), Mockito.any());
  }

  @Test
  public void assignEnquiry_Should_AddPeerConsultantToFeedbackGroup_WhenSessionHasFeedbackIsTrue()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(rocketChatService, times(1)).addUserToGroup(ROCKETCHAT_ID,
        U25_SESSION_WITHOUT_CONSULTANT.getFeedbackGroupId());

  }

  @Test
  public void assignEnquiry_Should_SendEmailNotification_WhenEnquiryIsAssignedByDifferentConsultant()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT));
    when(consultantService.getConsultantByRcUserId(RC_USER_ID_MAIN_CONSULTANT))
        .thenReturn(Optional.of(MAIN_CONSULTANT));
    when(keycloakHelper.userHasAuthority(MAIN_CONSULTANT_ID, Authority.VIEW_ALL_PEER_SESSIONS))
        .thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID_2);

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(emailNotificationFacade, times(1))
        .sendAssignEnquiryEmailNotification(CONSULTANT_WITH_AGENCY, CONSULTANT_ID_2, USERNAME);
  }

  @Test
  public void assignEnquiry_Should_NotSendEmailNotification_WhenEnquiryIsAssignedBySameConsultant()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(anyString()))
        .thenReturn(LIST_GROUP_MEMBER_DTO);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT));
    when(consultantService.getConsultantByRcUserId(RC_USER_ID_MAIN_CONSULTANT))
        .thenReturn(Optional.of(MAIN_CONSULTANT));
    when(keycloakHelper.userHasAuthority(MAIN_CONSULTANT_ID, Authority.VIEW_ALL_PEER_SESSIONS))
        .thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    assignSessionFacade.assignEnquiry(U25_SESSION_WITHOUT_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(emailNotificationFacade, times(0))
        .sendAssignEnquiryEmailNotification(CONSULTANT_WITH_AGENCY, CONSULTANT_ID_2, USERNAME);
  }

}
