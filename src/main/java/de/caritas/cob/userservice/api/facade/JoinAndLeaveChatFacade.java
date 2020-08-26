package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.UserService;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Facade for capsuling to join a chat
 */
@Service
public class JoinAndLeaveChatFacade {

  private ChatService chatService;
  private ChatHelper chatHelper;
  private ConsultantService consultantService;
  private UserService userService;
  private RocketChatService rocketChatService;

  @Autowired
  public JoinAndLeaveChatFacade(ChatService chatService, ChatHelper chatHelper,
      ConsultantService consultantService, UserService userService,
      RocketChatService rocketChatService) {
    this.chatService = chatService;
    this.chatHelper = chatHelper;
    this.consultantService = consultantService;
    this.userService = userService;
    this.rocketChatService = rocketChatService;
  }

  /**
   * Join a chat
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
   * Leave a chat
   */
  public void leaveChat(Long chatId, AuthenticatedUser authenticatedUser) {

    Chat chat = getChat(chatId);
    String rcUserId = checkPermissionAndGetRcUserId(authenticatedUser, chat);

    try {
      rocketChatService.removeUserFromGroup(rcUserId, chat.getGroupId());
    } catch (RocketChatRemoveUserFromGroupException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }

  }

  /**
   * Get the chat
   */
  private Chat getChat(Long chatId) {

    Optional<Chat> chat = chatService.getChat(chatId);
    if (!chat.isPresent()) {
      throw new NotFoundException(String.format("Chat with id %s not found", chatId));
    }

    if (!chat.get().isActive()) {
      throw new ConflictException(
          String.format("User could not join/leave Chat with id %s, because it's not started.",
              chat.get().getId()));
    }

    return chat.get();

  }

  /**
   * Check chat permission for user/consultant and get the rc user id
   *
   * @return the rc user id of the consultant/user
   */
  private String checkPermissionAndGetRcUserId(AuthenticatedUser authenticatedUser, Chat chat) {

    String rcUserId = null;

    Set<String> roles = authenticatedUser.getRoles();

    if (roles.contains(UserRole.CONSULTANT.getValue())) {

      Optional<Consultant> consultant =
          consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

      if (!chatHelper.isChatAgenciesContainConsultantAgency(chat, consultant.get())) {
        throw new ForbiddenException(
            String.format("Consultant with id %s has no permission for chat with id %s",
                consultant.get().getId(), chat.getId()));
      }

      rcUserId =
          (consultant.get().getRocketChatId() != null) ? consultant.get().getRocketChatId() : null;
    }

    if (roles.contains(UserRole.USER.getValue())) {

      Optional<User> user = userService.getUserViaAuthenticatedUser(authenticatedUser);

      if (!chatHelper.isChatAgenciesContainUserAgency(chat, user.get())) {
        throw new ForbiddenException(
            String.format("User with id %s has no permission for chat with id %s",
                user.get().getUserId(), chat.getId()));
      }

      rcUserId = (user.get().getRcUserId() != null) ? user.get().getRcUserId() : null;
    }

    if (rcUserId == null) {
      throw new InternalServerErrorException(
          String.format("User with id %s has no Rocket.Chat-ID.", authenticatedUser.getUserId()));
    }

    return rcUserId;

  }

}
