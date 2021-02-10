package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;

/**
 * Facade for capsuling starting a chat
 */
@Service
public class StartChatFacade {

  private ChatService chatService;
  private RocketChatService rocketChatService;
  private ChatHelper chatHelper;

  @Autowired
  public StartChatFacade(ChatService chatService, RocketChatService rocketChatService,
      ChatHelper chatHelper) {
    this.chatService = chatService;
    this.rocketChatService = rocketChatService;
    this.chatHelper = chatHelper;
  }

  public void startChat(Chat chat, Consultant consultant) {

    if (!chatHelper.isChatAgenciesContainConsultantAgency(chat, consultant)) {
      throw new ForbiddenException(
          String.format("Consultant with id %s has no permission to start chat with id %s",
              consultant.getId(), chat.getId()));
    }

    if (isTrue(chat.isActive())) {
      throw new ConflictException(
          String.format("Chat with id %s is already started.", chat.getId()));
    }

    if (chat.getGroupId() == null) {
      throw new InternalServerErrorException(
          String.format("Chat with id %s has no Rocket.Chat group id", chat.getId()));
    }

    try {
      rocketChatService.addUserToGroup(consultant.getRocketChatId(), chat.getGroupId());
      chat.setActive(true);
      chatService.saveChat(chat);
    } catch (RocketChatAddUserToGroupException | SaveChatException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logRocketChatError);
    }
  }

}
