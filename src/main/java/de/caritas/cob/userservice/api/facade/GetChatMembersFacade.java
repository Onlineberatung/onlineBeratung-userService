package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ChatMemberResponseDTO;
import de.caritas.cob.userservice.api.model.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.UserService;

/**
 * Facade to encapsulate the steps for get the chat members.
 */

@Service
@RequiredArgsConstructor
public class GetChatMembersFacade {

  private final @NonNull ChatService chatService;
  private final @NonNull UserService userService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ChatHelper chatHelper;
  private final @NonNull UserHelper userHelper;

  /**
   * Get a filtered list of the members of a chat (without technical/system user).
   *
   * @param chatId            chat ID
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link ChatMembersResponseDTO}
   */
  public ChatMembersResponseDTO getChatMembers(Long chatId, AuthenticatedUser authenticatedUser) {

    Optional<Chat> chat = chatService.getChat(chatId);
    if (!chat.isPresent()) {
      throw new NotFoundException(String.format("Chat with id %s not found", chatId));
    }

    if (isFalse(chat.get().isActive())) {
      throw new ConflictException(
          String.format("Could not get members of chat with id %s, because it's not started.",
              chat.get().getId()));
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

    if (chat.get().getGroupId() == null) {
      throw new InternalServerErrorException(
          String.format("Chat with id %s has no Rocket.Chat group id", chat.get().getId()));
    }

    try {
      return convertGroupMemberDTOListToChatMemberResponseDTO(
          rocketChatService.getStandardMembersOfGroup(chat.get().getGroupId()));
    } catch (RocketChatGetGroupMembersException | RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  private ChatMembersResponseDTO convertGroupMemberDTOListToChatMemberResponseDTO(
      List<GroupMemberDTO> groupMemberDTOList) {
    return new ChatMembersResponseDTO().members(groupMemberDTOList.stream()
        .map(member -> new ChatMemberResponseDTO()
            .id(member.get_id())
            .status(member.getStatus())
            .username(userHelper.decodeUsername(member.getUsername()))
            .name(member.getName())
            .utcOffset(member.getUtcOffset()))
        .collect(Collectors.toList()));
  }

}
