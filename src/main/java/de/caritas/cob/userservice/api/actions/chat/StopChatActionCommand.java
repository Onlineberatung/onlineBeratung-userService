package de.caritas.cob.userservice.api.actions.chat;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to perform all neccessary steps to stop an active group chat.
 */
@Component
@RequiredArgsConstructor
public class StopChatActionCommand implements ActionCommand<Chat> {

  private static final long WEEKLY_PLUS = 1L;

  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatService rocketChatService;

  /**
   * Stops the given active chat, removes all messages if chat is repetitive and deletes the
   * complete chat if its not reprtitive.
   *
   * @param chat the {@link Chat} to be stopped
   */
  @Override
  public void execute(Chat chat) {
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
