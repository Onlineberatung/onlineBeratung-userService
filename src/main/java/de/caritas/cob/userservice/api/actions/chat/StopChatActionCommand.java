package de.caritas.cob.userservice.api.actions.chat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Action to perform all necessary steps to stop an active group chat. */
@Component
@RequiredArgsConstructor
public class StopChatActionCommand implements ActionCommand<Chat> {

  private final ChatService chatService;
  private final RocketChatService rocketChatService;
  private final ChatReCreator chatReCreator;

  /**
   * Deletes the given active chat and recreates it if repetitive.
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

    if (!chat.isRepetitive() || nonNull(chat.nextStart())) {
      deleteMessengerChat(chat);
      if (chat.isRepetitive()) {
        var rcGroupId = chatReCreator.recreateMessengerChat(chat);
        chatReCreator.updateAsNextChat(chat, rcGroupId);
      } else {
        chatService.deleteChat(chat);
      }
    }
  }

  private void checkActiveState(Chat chat) {
    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format("Chat with id %s is already stopped.", chat.getId()));
    }
  }

  private void deleteMessengerChat(Chat chat) {
    if (!rocketChatService.deleteGroupAsSystemUser(chat.getGroupId())) {
      throw new InternalServerErrorException(
          String.format("Could not delete Rocket.Chat group with id %s", chat.getGroupId()));
    }
  }
}
