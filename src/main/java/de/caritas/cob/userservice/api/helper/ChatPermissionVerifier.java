package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.model.UserChat;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Analyzer class for chats. */
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

  public void verifyCanModerateChat(Chat chat) {
    Set<String> roles = authenticatedUser.getRoles();
    if (roles.contains(UserRole.CONSULTANT.getValue())) {
      this.verifyConsultantPermissionForChat(chat);
    } else {
      throw new ForbiddenException("User is not a consultant");
    }
  }

  /**
   * Check if the authenticated user has consultant permission on given chat.
   *
   * @param chat the {@link Chat}
   */
  private void verifyConsultantPermissionForChat(Chat chat) {
    Consultant consultant =
        consultantService
            .getConsultantViaAuthenticatedUser(authenticatedUser)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Consultant with id %s not found", authenticatedUser.getUserId()));

    if (!hasSameAgencyAssigned(chat, consultant)) {
      throw new ForbiddenException(
          String.format(
              "Consultant with id %s has no permission for chat with id %s",
              consultant.getId(), chat.getId()));
    }
  }

  /**
   * Checks if chat agencies contain consultant agency.
   *
   * @param chat the {@link Chat}
   * @param consultant the {@link Consultant}
   * @return true if agency of consultant is contained
   */
  public boolean hasSameAgencyAssigned(Chat chat, Consultant consultant) {
    return chat.getChatAgencies().stream()
        .map(ChatAgency::getAgencyId)
        .anyMatch(
            consultant.getConsultantAgencies().stream()
                    .map(ConsultantAgency::getAgencyId)
                    .collect(Collectors.toSet())
                ::contains);
  }

  /**
   * Checks if chat agencies contain user agency.
   *
   * @param chat the {@link Chat}
   * @param user the {@link User}
   * @return true if agency of user is contained
   */
  public boolean hasSameAgencyAssigned(Chat chat, User user) {
    return chat.getChatAgencies() != null
        && chat.getChatAgencies().stream()
            .map(ChatAgency::getAgencyId)
            .anyMatch(
                user.getUserAgencies().stream()
                        .map(UserAgency::getAgencyId)
                        .collect(Collectors.toSet())
                    ::contains);
  }

  /**
   * Checks if chat users contain user.
   *
   * @param chat the {@link Chat}
   * @param user the {@link User}
   * @return true if chat users contain given user
   */
  public boolean hasChatUserAssignment(Chat chat, User user) {
    return chat.getChatUsers() != null
        && chat.getChatUsers().stream()
            .map(UserChat::getUser)
            .collect(Collectors.toList())
            .contains(user);
  }

  /**
   * Check if the authenticated user has user permission on given chat.
   *
   * <p>This method combines a check for V1 (same agency assigned as chat) and V2 (user has a valid
   * chat assignment).
   *
   * @param chat the {@link Chat}
   */
  private void verifyUserPermissionForChat(Chat chat) {
    User user =
        userService
            .getUserViaAuthenticatedUser(authenticatedUser)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "User with id %s not found", authenticatedUser.getUserId()));

    if (!hasChatUserAssignment(chat, user) && !hasSameAgencyAssigned(chat, user)) {
      throw new ForbiddenException(
          String.format(
              "User with id %s has no permission for chat with id %s",
              user.getUserId(), chat.getId()));
    }
  }
}
