package de.caritas.cob.userservice.api.actions.chat;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.service.ChatService;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to perform all necessary steps to stop an active group chat.
 */
@Component
@RequiredArgsConstructor
public class StopChatActionCommand implements ActionCommand<Chat> {

  private static final RocketChatRoomNameGenerator roomNameGenerator = new RocketChatRoomNameGenerator();

  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatService rocketChatService;

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
      deleteChatGroup(chat);
      if (chat.isRepetitive()) {
        var rcGroupId = recreateMessengerChat(chat);
        recreateChat(chat, rcGroupId);
      }
    }
  }

  private void checkActiveState(Chat chat) {
    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format("Chat with id %s is already stopped.", chat.getId()));
    }
  }

  // duplicated because currently not E2E-testable due to thread change in registry execution
  @SuppressWarnings({"Duplicates", "java:S4144", "common-java:DuplicatedBlocks"})
  private void recreateChat(Chat chat, String rcGroupId) {
    final var chatAgencyIds = chat.getChatAgencies().stream()
        .map(ChatAgency::getAgencyId)
        .collect(Collectors.toList());

    chat.setId(null);
    chat.setGroupId(rcGroupId);
    chat.setStartDate(chat.nextStart());
    chat.setUpdateDate(nowInUtc());
    chat.setActive(false);
    chat.setChatAgencies(null);
    chatService.saveChat(chat);

    chatAgencyIds.forEach(chatAgencyId -> {
      var recreatedChatAgency = new ChatAgency(chat, chatAgencyId);
      chatService.saveChatAgencyRelation(recreatedChatAgency);
    });
  }

  @SuppressWarnings({"Duplicates", "java:S4144", "common-java:DuplicatedBlocks"})
  private String recreateMessengerChat(Chat chat) {
    String rcGroupId = null;
    var groupName = roomNameGenerator.generateGroupChatName(chat);
    try {
      var response = rocketChatService
          .createPrivateGroupWithSystemUser(groupName)
          .orElseThrow(() -> new RocketChatCreateGroupException(
              "RocketChat group is not present while creating chat: " + chat)
          );
      rcGroupId = response.getGroup().getId();
      rocketChatService.addTechnicalUserToGroup(rcGroupId);
    } catch (RocketChatCreateGroupException
             | RocketChatAddUserToGroupException
             | RocketChatUserNotInitializedException e) {
      if (nonNull(rcGroupId)) {
        rocketChatService.deleteGroupAsSystemUser(rcGroupId);
      }
      throw new InternalServerErrorException("Error while creating private group in Rocket.Chat "
          + "for group chat: " + chat);
    }

    return rcGroupId;
  }

  private void deleteChatGroup(Chat chat) {
    if (!rocketChatService.deleteGroupAsSystemUser(chat.getGroupId())) {
      throw new InternalServerErrorException(
          String.format("Could not delete Rocket.Chat group with id %s", chat.getGroupId()));
    }
    chatService.deleteChat(chat);
  }
}
