package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.actions.chat.ChatReCreator;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade for capsuling to join a chat. */
@Service
@RequiredArgsConstructor
public class JoinAndLeaveChatFacade {

  private final ChatService chatService;
  private final ChatPermissionVerifier chatPermissionVerifier;
  private final ConsultantService consultantService;
  private final UserService userService;
  private final RocketChatService rocketChatService;
  private final ChatReCreator chatReCreator;

  /**
   * Join a chat.
   *
   * @param chatId the chat id
   * @param authenticatedUser the authenticated user
   */
  public void joinChat(Long chatId, AuthenticatedUser authenticatedUser) {
    Chat chat = getChat(chatId);
    String rcUserId = checkPermissionAndGetRcUserId(authenticatedUser, chat);

    try {
      rocketChatService.addUserToGroup(rcUserId, chat.getGroupId());
    } catch (RocketChatAddUserToGroupException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logRocketChatError);
    }
  }

  public void verifyCanModerate(Long chatId) {
    Chat chat = getChat(chatId);
    this.chatPermissionVerifier.verifyCanModerateChat(chat);
  }

  /**
   * Leave a chat.
   *
   * @param chatId the id of the chat
   * @param authenticatedUser the authenticated user
   */
  public void leaveChat(Long chatId, AuthenticatedUser authenticatedUser) {
    Chat chat = getChat(chatId);
    String rcUserId = checkPermissionAndGetRcUserId(authenticatedUser, chat);

    try {
      rocketChatService.removeUserFromGroup(rcUserId, chat.getGroupId());
      if (rocketChatService.getStandardMembersOfGroup(chat.getGroupId()).isEmpty()) {
        deleteMessengerChat(chat.getGroupId());
        if (chat.isRepetitive()) {
          var rcGroupId = chatReCreator.recreateMessengerChat(chat);
          chatReCreator.updateAsNextChat(chat, rcGroupId);
        } else {
          chatService.deleteChat(chat);
        }
      }
    } catch (RocketChatRemoveUserFromGroupException
        | RocketChatUserNotInitializedException
        | RocketChatGetGroupMembersException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  private void deleteMessengerChat(String groupId) {
    if (!rocketChatService.deleteGroupAsSystemUser(groupId)) {
      var message = String.format("Could not delete Rocket.Chat group with id %s", groupId);
      throw new InternalServerErrorException(message);
    }
  }

  private Chat getChat(Long chatId) {
    Chat chat =
        chatService
            .getChat(chatId)
            .orElseThrow(() -> new NotFoundException("Chat with id %s not found", chatId));

    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format(
              "User could not join/leave Chat with id %s, because it's not started.",
              chat.getId()));
    }

    return chat;
  }

  private String checkPermissionAndGetRcUserId(AuthenticatedUser authenticatedUser, Chat chat) {
    this.chatPermissionVerifier.verifyPermissionForChat(chat);

    String rcUserId = retrieveRcUserId(authenticatedUser);
    if (isBlank(rcUserId)) {
      throw new InternalServerErrorException(
          String.format("User with id %s has no Rocket.Chat-ID.", authenticatedUser.getUserId()));
    }

    return rcUserId;
  }

  private String retrieveRcUserId(AuthenticatedUser authenticatedUser) {
    final AtomicReference<String> rcUserId = new AtomicReference<>();
    consultantService
        .getConsultantViaAuthenticatedUser(authenticatedUser)
        .ifPresentOrElse(
            consultant -> rcUserId.set(consultant.getRocketChatId()),
            () ->
                userService
                    .getUserViaAuthenticatedUser(authenticatedUser)
                    .ifPresent(user -> rcUserId.set(user.getRcUserId())));

    return rcUserId.get();
  }
}
