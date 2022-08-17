package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.ChatUser;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignChatFacadeTest {

  @InjectMocks
  private AssignChatFacade assignChatFacade;

  @Mock
  private ChatService chatService;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Mock
  private UserService userService;

  @Test
  void assignChat_Should_ThrowNotFoundException_WhenChatDoesNotExist() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> assignChatFacade.assignChat(CHAT_ID, authenticatedUser));

    verify(chatService).getChat(CHAT_ID);
    assertEquals(String.format("Chat with id %s not found", CHAT_ID),
        exception.getMessage());
  }

  @Test
  void assignChat_Should_ThrowNotFoundException_WhenUserDoesNotExist() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(
        Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> assignChatFacade.assignChat(CHAT_ID, authenticatedUser));

    verify(userService).getUserViaAuthenticatedUser(authenticatedUser);
    assertEquals(String.format("User with id %s not found", USER_ID),
        exception.getMessage());
  }

  @Test
  void assignChat_Should_AddUserToChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(
        Optional.of(USER));

    assignChatFacade.assignChat(CHAT_ID, authenticatedUser);

    verify(chatService).saveChatUserRelation(new ChatUser(ACTIVE_CHAT, USER));
  }
}
