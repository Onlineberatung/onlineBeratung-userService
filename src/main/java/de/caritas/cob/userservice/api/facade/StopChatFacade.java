package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/*
 * Facade to encapsulate the steps to stop a running chat session.
 */
@Service
@RequiredArgsConstructor
public class StopChatFacade {

  private static final long WEEKLY_PLUS = 1L;

  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ChatPermissionVerifier chatPermissionVerifier;

  /**
   * Stops the given {@link Chat} and resets or deletes it depending on if it's repetitive or not.
   *
   * @param chat       {@link Chat}
   * @param consultant {@link Consultant}
   */
  public void stopChat(Chat chat, Consultant consultant) {
    checkConsultantChatPermission(chat, consultant);
    checkActiveState(chat);

    if (isNull(chat.getGroupId())) {
      throw new InternalServerErrorException(
          String.format("Chat with id %s has no Rocket.Chat group id", chat.getId()));
    }

    if (isTrue(chat.isRepetitive())) {
      handleRepetitiveChatReset(chat);
    } else {
      deleteSingleChatGroup(chat);
    }

  }

  private void checkConsultantChatPermission(Chat chat, Consultant consultant) {
    if (!chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant)) {
      throw new ForbiddenException(
          String.format("Consultant with id %s has no permission to stop chat with id %s",
              consultant.getId(), chat.getId()));
    }
  }

  private void checkActiveState(Chat chat) {
    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format("Chat with id %s is already stopped.", chat.getId()));
    }
  }

  private void handleRepetitiveChatReset(Chat chat) {
    if (isNull(chat.getChatInterval())) {
      throw new InternalServerErrorException(String
          .format("Repetitive chat with id %s does not have a valid interval.", chat.getId()));
    }
    removeAllMessages(chat);
    removeRocketChatStandardUsers(chat);
    reinitializeDatabaseChat(chat);
  }

  private void removeAllMessages(Chat chat) {
    try {
      rocketChatService.removeAllMessages(chat.getGroupId());
    } catch (RocketChatRemoveSystemMessagesException e) {
      throw new InternalServerErrorException(
          String.format("Could not delete messages from chat with id %s", chat.getId()));
    }
  }

  private void removeRocketChatStandardUsers(Chat chat) {
    try {
      rocketChatService.removeAllStandardUsersFromGroup(chat.getGroupId());
    } catch (RocketChatGetGroupMembersException | RocketChatRemoveUserFromGroupException
        | RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  private void reinitializeDatabaseChat(Chat chat) {
    chat.setStartDate(getNextStartDate(chat));
    chat.setActive(false);
    chatService.saveChat(chat);
  }

  private LocalDateTime getNextStartDate(Chat chat) {
    return chat.getStartDate().plusWeeks(WEEKLY_PLUS);
  }

  private void deleteSingleChatGroup(Chat chat) {
    if (!rocketChatService.deleteGroupAsSystemUser(chat.getGroupId())) {
      throw new InternalServerErrorException(
          String.format("Could not delete Rocket.Chat group with id %s", chat.getGroupId()));
    }
    chatService.deleteChat(chat);
  }

}
