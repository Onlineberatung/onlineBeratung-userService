package de.caritas.cob.userservice.api.actions.chat;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReCreator {

  private static final RocketChatRoomNameGenerator roomNameGenerator =
      new RocketChatRoomNameGenerator();

  private final ChatService chatService;
  private final RocketChatService rocketChatService;

  public void updateAsNextChat(Chat chat, String rcGroupId) {
    chat.setGroupId(rcGroupId);
    chat.setStartDate(chat.nextStart());
    chat.setUpdateDate(nowInUtc());
    chat.setActive(false);

    chatService.saveChat(chat);
  }

  public String recreateMessengerChat(Chat chat) {
    String rcGroupId = null;
    var groupName = roomNameGenerator.generateGroupChatName(chat);
    try {
      var response =
          rocketChatService
              .createPrivateGroupWithSystemUser(groupName)
              .orElseThrow(
                  () ->
                      new RocketChatCreateGroupException(
                          "RocketChat group is not present while creating chat: " + chat));
      rcGroupId = response.getGroup().getId();
      rocketChatService.addTechnicalUserToGroup(rcGroupId);
    } catch (RocketChatCreateGroupException
        | RocketChatAddUserToGroupException
        | RocketChatUserNotInitializedException e) {
      if (nonNull(rcGroupId)) {
        rocketChatService.deleteGroupAsSystemUser(rcGroupId);
      }
      throw new InternalServerErrorException(
          "Error while creating private group in Rocket.Chat " + "for group chat: " + chat);
    }

    return rcGroupId;
  }
}
