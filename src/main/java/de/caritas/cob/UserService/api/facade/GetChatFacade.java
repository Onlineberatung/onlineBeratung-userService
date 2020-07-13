package de.caritas.cob.UserService.api.facade;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.UserRole;
import de.caritas.cob.UserService.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.UserService.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.helper.ChatHelper;
import de.caritas.cob.UserService.api.model.ChatInfoResponseDTO;
import de.caritas.cob.UserService.api.repository.chat.Chat;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.ChatService;
import de.caritas.cob.UserService.api.service.ConsultantService;
import de.caritas.cob.UserService.api.service.UserService;

/**
 * Facade for capsuling to get a chat
 */
@Service
public class GetChatFacade {

  private ChatService chatService;
  private ChatHelper chatHelper;
  private ConsultantService consultantService;
  private UserService userService;

  @Autowired
  public GetChatFacade(ChatService chatService, ChatHelper chatHelper,
      ConsultantService consultantService, UserService userService) {
    this.chatService = chatService;
    this.chatHelper = chatHelper;
    this.consultantService = consultantService;
    this.userService = userService;
  }

  /**
   * Get chat info
   * 
   * @param chatId
   * @param authenticatedUser
   * @return
   */
  public ChatInfoResponseDTO getChat(Long chatId, AuthenticatedUser authenticatedUser) {

    Optional<Chat> chat = chatService.getChat(chatId);
    if (!chat.isPresent()) {
      throw new NotFoundException(String.format("Chat with id %s not found.", chatId));
    }

    Set<String> roles = authenticatedUser.getRoles();

    if (roles.contains(UserRole.CONSULTANT.getValue())) {

      Optional<Consultant> consultant =
          consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

      if (!chatHelper.isChatAgenciesContainConsultantAgency(chat.get(), consultant.get())) {
        throw new ForbiddenException(
            String.format("Consultant with id %s has no permission for chat with id %s",
                consultant.get().getId(), chat.get().getId()));
      }
    }

    if (roles.contains(UserRole.USER.getValue())) {

      Optional<User> user = userService.getUserViaAuthenticatedUser(authenticatedUser);

      if (!chatHelper.isChatAgenciesContainUserAgency(chat.get(), user.get())) {
        throw new ForbiddenException(
            String.format("User with id %s has no permission for chat with id %s",
                user.get().getUserId(), chat.get().getId()));
      }

    }

    return new ChatInfoResponseDTO(chat.get().getId(), chat.get().getGroupId(),
        chat.get().isActive());
  }

}
