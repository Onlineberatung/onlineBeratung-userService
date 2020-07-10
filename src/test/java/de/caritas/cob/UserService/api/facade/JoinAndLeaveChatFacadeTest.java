package de.caritas.cob.UserService.api.facade;

import static de.caritas.cob.UserService.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.UserService.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTANT_ROLES;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_ROLES;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import javax.ws.rs.InternalServerErrorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.exception.httpresponses.ConflictException;
import de.caritas.cob.UserService.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.UserService.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.helper.ChatHelper;
import de.caritas.cob.UserService.api.repository.chat.Chat;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.ChatService;
import de.caritas.cob.UserService.api.service.ConsultantService;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class JoinAndLeaveChatFacadeTest {

  @InjectMocks
  private JoinAndLeaveChatFacade joinAndLeaveChatFacade;
  @Mock
  private ChatService chatService;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ChatHelper chatHelper;
  @Mock
  private ConsultantService consultantService;
  @Mock
  private UserService userService;
  @Mock
  private User user;
  @Mock
  private Consultant consultant;
  @Mock
  private RocketChatService rocketChatService;

  /**
   * Method: joinChat
   */
  @Test
  public void joinChat_Should_ThrowNotFoundException_WhenChatDoesNotExist() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: NotFoundException");
    } catch (NotFoundException notFoundException) {
      assertTrue("Excepted NotFoundException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);

  }


  @Test
  public void joinChat_Should_ThrowConflictException_WhenChatIsNotActive() {

    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);

  }


  @Test
  public void joinChat_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant))
        .thenReturn(false);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void joinChat_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(false);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainUserAgency(ACTIVE_CHAT, user);
    verify(userService, times(1)).getUserViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void joinChat_Should_ThrowInternalServerErrorException_WhenConsultantHasNoRocketChatId() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant))
        .thenReturn(true);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));
    when(consultant.getRocketChatId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);
    verify(consultant, times(1)).getRocketChatId();

  }

  @Test
  public void joinChat_Should_ThrowInternalServerErrorException_WhenUserHasNoRocketChatId() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(true);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainUserAgency(ACTIVE_CHAT, user);
    verify(userService, times(1)).getUserViaAuthenticatedUser(authenticatedUser);
    verify(user, times(1)).getRcUserId();

  }

  @Test
  public void joinChat_Should_AddConsultantToRocketChatGroup() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(CONSULTANT));

    joinAndLeaveChatFacade.joinChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1)).addUserToGroup(CONSULTANT.getRocketChatId(),
        ACTIVE_CHAT.getGroupId());

  }

  @Test
  public void joinChat_Should_AddUserToRocketChatGroup() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(true);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(RC_USER_ID);

    joinAndLeaveChatFacade.joinChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1)).addUserToGroup(RC_USER_ID, ACTIVE_CHAT.getGroupId());

  }

  /**
   * 
   * Method: leaveChat
   * 
   */
  /**
   * Method: joinChat
   */
  @Test
  public void leaveChat_Should_ThrowNotFoundException_WhenChatDoesNotExist() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: NotFoundException");
    } catch (NotFoundException notFoundException) {
      assertTrue("Excepted NotFoundException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);

  }


  @Test
  public void leaveChat_Should_ThrowConflictException_WhenChatIsNotActive() {

    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);

  }


  @Test
  public void leaveChat_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant))
        .thenReturn(false);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void leaveChat_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(false);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainUserAgency(ACTIVE_CHAT, user);
    verify(userService, times(1)).getUserViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void leaveChat_Should_ThrowInternalServerErrorException_WhenConsultantHasNoRocketChatId() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant))
        .thenReturn(true);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));
    when(consultant.getRocketChatId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, consultant);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);
    verify(consultant, times(1)).getRocketChatId();

  }

  @Test
  public void leaveChat_Should_ThrowInternalServerErrorException_WhenUserHasNoRocketChatId() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(true);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainUserAgency(ACTIVE_CHAT, user);
    verify(userService, times(1)).getUserViaAuthenticatedUser(authenticatedUser);
    verify(user, times(1)).getRcUserId();

  }

  @Test
  public void leaveChat_Should_RemoveConsultantFromRocketChatGroup() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(CONSULTANT));

    joinAndLeaveChatFacade.leaveChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1)).removeUserFromGroup(CONSULTANT.getRocketChatId(),
        ACTIVE_CHAT.getGroupId());

  }

  @Test
  public void leaveChat_Should_RemoveUserFromRocketChatGroup() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(true);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(RC_USER_ID);

    joinAndLeaveChatFacade.leaveChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1)).removeUserFromGroup(RC_USER_ID, ACTIVE_CHAT.getGroupId());

  }


}
