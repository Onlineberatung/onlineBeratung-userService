package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.service.LogService;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;

/*
 * Facade to encapsulate the steps to stop a running chat session.
 */
@Service
public class StopChatFacade {

  private final long weeklyPlus = 1L;
  private final boolean chatInactive = false;

  private ChatService chatService;
  private RocketChatService rocketChatService;
  private ChatHelper chatHelper;

  @Autowired
  public StopChatFacade(ChatService chatService, RocketChatService rocketChatService,
      ChatHelper chatHelper) {
    this.chatService = chatService;
    this.rocketChatService = rocketChatService;
    this.chatHelper = chatHelper;
  }

  /**
   * Stops the given {@link Chat} and resets or deletes it depending on if it's repetitive or not.
   * 
   * @param chat {@link Chat}
   * @param consultant {@link Consultant}
   */
  public void stopChat(Chat chat, Consultant consultant) {

    if (!chatHelper.isChatAgenciesContainConsultantAgency(chat, consultant)) {
      throw new ForbiddenException(
          String.format("Consultant with id %s has no permission to stop chat with id %s",
              consultant.getId(), chat.getId()));
    }

    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format("Chat with id %s is already stopped.", chat.getId()));
    }

    if (chat.getGroupId() == null) {
      throw new InternalServerErrorException(
          String.format("Chat with id %s has no Rocket.Chat group id", chat.getId()));
    }

    if (isTrue(chat.isRepetitive())) {
      // Repeating chat -> Remove Rocket.Chat messages and users and update next chat start date
      if (chat.getChatInterval() == null) {
        throw new InternalServerErrorException(String
            .format("Repetitive chat with id %s does not have a valid interval.", chat.getId()));
      }

      try {
        rocketChatService.removeAllMessages(chat.getGroupId());
      } catch (RocketChatRemoveSystemMessagesException e) {
        throw new InternalServerErrorException(
            String.format("Could not delete messages from chat with id %s", chat.getId()));
      }

      try {
        rocketChatService.removeAllStandardUsersFromGroup(chat.getGroupId());
      } catch (RocketChatGetGroupMembersException | RocketChatRemoveUserFromGroupException
          | RocketChatUserNotInitializedException e) {
        throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
      }

      chat.setStartDate(getNextStartDate(chat));
      chat.setActive(chatInactive);
      try {
        chatService.saveChat(chat);
      } catch (SaveChatException e) {
        throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
      }

    } else {
      // Single chat -> Delete Rocket.Chat group and chat data in MariaDB
      if (!rocketChatService.deleteGroupAsSystemUser(chat.getGroupId())) {
        throw new InternalServerErrorException(
            String.format("Could not delete Rocket.Chat group with id %s", chat.getGroupId()));
      }
      chatService.deleteChat(chat);
    }

  }

  /**
   * 
   * Returns the next start date for repetitive chats. Currently there is only the possibility for
   * weekly chats.
   * 
   * @param chat {@link Chat}
   * @return
   */
  private LocalDateTime getNextStartDate(Chat chat) {
    return chat.getStartDate().plusWeeks(weeklyPlus);
  }

}
