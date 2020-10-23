package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.model.ChatInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.UserService;

/**
 * Facade for capsuling to get a chat
 */
@Service
@RequiredArgsConstructor
public class GetChatFacade {

  private final @NonNull ChatService chatService;
  private final @NonNull ChatHelper chatHelper;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserService userService;

  /**
   * Get chat info.
   *
   * @param chatId            chat ID
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link ChatInfoResponseDTO}
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

    return new ChatInfoResponseDTO()
        .id(chat.get().getId())
        .groupId(chat.get().getGroupId())
        .active(isTrue(chat.get().isActive()));
  }

}
