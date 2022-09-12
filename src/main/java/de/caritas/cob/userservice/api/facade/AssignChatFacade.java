package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserChat;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade for capsuling to assign a user to a chat. */
@Service
@RequiredArgsConstructor
public class AssignChatFacade {

  private final ChatService chatService;
  private final UserService userService;

  /**
   * Assign a chat to the authenticatedUser.
   *
   * <p>In this assignment process is no further validation, because everyone is allowed to be added
   * to this chat.
   *
   * @param chatId the chat id
   * @param authenticatedUser that authenticated user
   */
  public void assignChat(Long chatId, AuthenticatedUser authenticatedUser) {
    Chat chat = getChat(chatId);
    User user = getUser(authenticatedUser);

    chatService.saveUserChatRelation(UserChat.builder().user(user).chat(chat).build());
  }

  private Chat getChat(Long chatId) {
    return chatService
        .getChat(chatId)
        .orElseThrow(() -> new NotFoundException("Chat with id %s not found", chatId));
  }

  private User getUser(AuthenticatedUser authenticatedUser) {
    return userService
        .getUserViaAuthenticatedUser(authenticatedUser)
        .orElseThrow(
            () ->
                new NotFoundException("User with id %s not found", authenticatedUser.getUserId()));
  }
}
