package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade for capsuling starting a chat. */
@Service
@RequiredArgsConstructor
public class StartChatFacade {

  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ChatPermissionVerifier chatPermissionVerifier;

  /**
   * Starts the given {@link Chat}.
   *
   * @param chat the {@link Chat} to be started
   * @param consultant the {@link Consultant}
   */
  public void startChat(Chat chat, Consultant consultant) {

    checkConsultantsPermission(chat, consultant);
    checkIfChatIsAlreadyActive(chat);
    checkRocketChatGroup(chat);

    try {
      rocketChatService.addUserToGroup(consultant.getRocketChatId(), chat.getGroupId());
      chat.setActive(true);
      chatService.saveChat(chat);
    } catch (RocketChatAddUserToGroupException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logRocketChatError);
    }
  }

  private void checkConsultantsPermission(Chat chat, Consultant consultant) {
    if (!chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant)) {
      throw new ForbiddenException(
          String.format(
              "Consultant with id %s has no permission to start chat with id %s",
              consultant.getId(), chat.getId()));
    }
  }

  private void checkIfChatIsAlreadyActive(Chat chat) {
    if (isTrue(chat.isActive())) {
      throw new ConflictException(
          String.format("Chat with id %s is already started.", chat.getId()));
    }
  }

  private void checkRocketChatGroup(Chat chat) {
    if (isNull(chat.getGroupId())) {
      throw new InternalServerErrorException(
          String.format("Chat with id %s has no Rocket.Chat group id", chat.getId()));
    }
  }
}
