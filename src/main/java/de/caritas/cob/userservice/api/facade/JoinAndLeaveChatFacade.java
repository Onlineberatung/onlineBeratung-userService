package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade for capsuling to join a chat.
 */
@Service
@RequiredArgsConstructor
public class JoinAndLeaveChatFacade {

  private static final RocketChatRoomNameGenerator roomNameGenerator = new RocketChatRoomNameGenerator();

  private final @NonNull ChatService chatService;
  private final @NonNull ChatPermissionVerifier chatPermissionVerifier;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserService userService;
  private final @NonNull RocketChatService rocketChatService;

  /**
   * Join a chat.
   *
   * @param chatId            the chat id
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

  /**
   * Leave a chat.
   *
   * @param chatId            the id of the chat
   * @param authenticatedUser the authenticated user
   */
  public void leaveChat(Long chatId, AuthenticatedUser authenticatedUser) {
    Chat chat = getChat(chatId);
    String rcUserId = checkPermissionAndGetRcUserId(authenticatedUser, chat);

    try {
      rocketChatService.removeUserFromGroup(rcUserId, chat.getGroupId());
      if (rocketChatService.getStandardMembersOfGroup(chat.getGroupId()).isEmpty()) {
        deleteMessengerChat(chat.getGroupId());
        chatService.deleteChat(chat);
        if (chat.isRepetitive()) {
          var rcGroupId = recreateMessengerChat(chat);
          recreateChat(chat, rcGroupId);
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

  private Chat getChat(Long chatId) {
    Chat chat = chatService.getChat(chatId)
        .orElseThrow(
            () -> new NotFoundException(String.format("Chat with id %s not found", chatId)));

    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format("User could not join/leave Chat with id %s, because it's not started.",
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
    consultantService.getConsultantViaAuthenticatedUser(authenticatedUser)
        .ifPresentOrElse(consultant -> rcUserId.set(consultant.getRocketChatId()),
            () -> userService.getUserViaAuthenticatedUser(authenticatedUser)
                .ifPresent(user -> rcUserId.set(user.getRcUserId())));

    return rcUserId.get();
  }

}
