package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JoinAndLeaveChatFacadeTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks private JoinAndLeaveChatFacade joinAndLeaveChatFacade;

  @Mock private ChatService chatService;

  @Mock private AuthenticatedUser authenticatedUser;

  @Mock private ChatPermissionVerifier chatPermissionVerifier;

  @Mock private ConsultantService consultantService;

  @Mock private UserService userService;

  @Mock private User user;

  @Mock private Consultant consultant;

  @Mock private RocketChatService rocketChatService;

  @Test
  void joinChat_Should_ThrowNotFoundException_WhenChatDoesNotExist() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: NotFoundException");
    } catch (NotFoundException notFoundException) {
      assertTrue(true, "Excepted NotFoundException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  void joinChat_Should_ThrowConflictException_WhenChatIsNotActive() {
    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue(true, "Excepted ConflictException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  void joinChat_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException(""))
        .when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  void joinChat_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException(""))
        .when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  void joinChat_Should_ThrowInternalServerErrorException_WhenConsultantHasNoRocketChatId() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));
    when(consultant.getRocketChatId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);
    verify(consultant, times(1)).getRocketChatId();
  }

  @Test
  void joinChat_Should_ThrowInternalServerErrorException_WhenUserHasNoRocketChatId() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.joinChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
    verify(user, times(1)).getRcUserId();
  }

  @Test
  void joinChat_Should_AddConsultantToRocketChatGroup() throws RocketChatAddUserToGroupException {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(CONSULTANT));

    joinAndLeaveChatFacade.joinChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1))
        .addUserToGroup(CONSULTANT.getRocketChatId(), ACTIVE_CHAT.getGroupId());
  }

  @Test
  void joinChat_Should_AddUserToRocketChatGroup() throws RocketChatAddUserToGroupException {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(RC_USER_ID);

    joinAndLeaveChatFacade.joinChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1)).addUserToGroup(RC_USER_ID, ACTIVE_CHAT.getGroupId());
  }

  @Test
  void leaveChat_Should_ThrowNotFoundException_WhenChatDoesNotExist() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: NotFoundException");
    } catch (NotFoundException notFoundException) {
      assertTrue(true, "Excepted NotFoundException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  void leaveChat_Should_ThrowConflictException_WhenChatIsNotActive() {
    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue(true, "Excepted ConflictException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  void leaveChat_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException(""))
        .when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  void leaveChat_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException(""))
        .when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  void leaveChat_Should_ThrowInternalServerErrorException_WhenConsultantHasNoRocketChatId() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));
    when(consultant.getRocketChatId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);
    verify(consultant, times(1)).getRocketChatId();
  }

  @Test
  void leaveChat_Should_ThrowInternalServerErrorException_WhenUserHasNoRocketChatId() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(null);

    try {
      joinAndLeaveChatFacade.leaveChat(CHAT_ID, authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
    verify(user, times(1)).getRcUserId();
  }

  @Test
  void leaveChat_Should_RemoveConsultantFromRocketChatGroup()
      throws RocketChatRemoveUserFromGroupException, RocketChatUserNotInitializedException,
          RocketChatGetGroupMembersException {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getStandardMembersOfGroup(eq(ACTIVE_CHAT.getGroupId())))
        .thenReturn(List.of(easyRandom.nextObject(GroupMemberDTO.class)));

    joinAndLeaveChatFacade.leaveChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, times(1))
        .removeUserFromGroup(CONSULTANT.getRocketChatId(), ACTIVE_CHAT.getGroupId());
  }

  @Test
  void leaveChat_Should_throwInternalServerErrorException_When_rocketChatUserCanNotBeRemoved()
      throws RocketChatRemoveUserFromGroupException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
          when(userService.getUserViaAuthenticatedUser(authenticatedUser))
              .thenReturn(Optional.of(user));
          when(user.getRcUserId()).thenReturn(RC_USER_ID);
          doThrow(new RocketChatRemoveUserFromGroupException(""))
              .when(rocketChatService)
              .removeUserFromGroup(any(), any());

          joinAndLeaveChatFacade.leaveChat(ACTIVE_CHAT.getId(), authenticatedUser);
        });
  }

  @Test
  void leaveChatShouldNotDeleteChatsWhenStandardMemberInChat()
      throws RocketChatUserNotInitializedException, RocketChatGetGroupMembersException {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(user.getRcUserId()).thenReturn(RC_USER_ID);
    when(rocketChatService.getStandardMembersOfGroup(eq(ACTIVE_CHAT.getGroupId())))
        .thenReturn(List.of(easyRandom.nextObject(GroupMemberDTO.class)));

    joinAndLeaveChatFacade.leaveChat(ACTIVE_CHAT.getId(), authenticatedUser);

    verify(rocketChatService, never()).deleteGroupAsSystemUser(anyString());
    verify(chatService, never()).deleteChat(any());
  }
}
