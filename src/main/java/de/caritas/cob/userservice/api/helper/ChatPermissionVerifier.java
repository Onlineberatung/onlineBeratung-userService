package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Analyzer class for chats.
 */
@Component
@RequiredArgsConstructor
public class ChatPermissionVerifier {

  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserService userService;
  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Verifies if the {@link AuthenticatedUser} has access right on given {@link Chat}.
   *
   * @param chat the {@link Chat} to verify
   */
  public void verifyPermissionForChat(Chat chat) {
    Set<String> roles = authenticatedUser.getRoles();
    if (roles.contains(UserRole.CONSULTANT.getValue())) {
      this.verifyConsultantPermissionForChat(chat);
    }
    if (roles.contains(UserRole.USER.getValue())) {
      this.verifyUserPermissionForChat(chat);
    }
  }

  /**
   * Check if the authenticated user has consultant permission on given chat.
   *
   * @param chat the chat
   */
  private void verifyConsultantPermissionForChat(Chat chat) {
    Consultant consultant =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser)
            .orElseThrow(() -> new NotFoundException(String.format("Consultant with id %s not "
                + "found", authenticatedUser.getUserId())));

    if (!hasSameAgencyAssigned(chat, consultant)) {
      throw new ForbiddenException(
          String.format("Consultant with id %s has no permission for chat with id %s",
              consultant.getId(), chat.getId()));
    }
  }

  /**
   * Checks if chat agencies contain consultant agency.
   *
   * @param chat       the chat
   * @param consultant the {@link Consultant}
   * @return true if agency of consultant is contained
   */
  public boolean hasSameAgencyAssigned(Chat chat, Consultant consultant) {
    return chat.getChatAgencies().stream()
        .map(ChatAgency::getAgencyId)
        .anyMatch(consultant.getConsultantAgencies().stream()
            .map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toSet())::contains);
  }

  /**
   * Checks if chat agencies contain user agency.
   *
   * @param chat the chat
   * @param user the {@link User}
   * @return true if agency of user is contained
   */
  public boolean hasSameAgencyAssigned(Chat chat, User user) {
    return chat.getChatAgencies().stream()
        .map(ChatAgency::getAgencyId)
        .anyMatch(user.getUserAgencies().stream()
            .map(UserAgency::getAgencyId)
            .collect(Collectors.toSet())::contains);
  }

  /**
   * Check if the authenticated user has user permission on given chat.
   *
   * @param chat the chat
   */
  private void verifyUserPermissionForChat(Chat chat) {
    User user = userService.getUserViaAuthenticatedUser(authenticatedUser)
        .orElseThrow(() -> new NotFoundException(String.format("User with id %s not found",
            authenticatedUser.getUserId())));

    if (!hasSameAgencyAssigned(chat, user)) {
      throw new ForbiddenException(
          String.format("User with id %s has no permission for chat with id %s",
              user.getUserId(), chat.getId()));
    }
  }

}
