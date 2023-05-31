package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatMemberResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate the steps for get the chat members. */
@Service
@RequiredArgsConstructor
public class GetChatMembersFacade {

  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ChatPermissionVerifier chatPermissionVerifier;

  private final @NonNull UserRepository userRepository;

  private final @NonNull ConsultantRepository consultantRepository;

  /**
   * Get a filtered list of the members of a chat (without technical/system user).
   *
   * @param chatId chat ID
   * @return {@link ChatMembersResponseDTO}
   */
  public ChatMembersResponseDTO getChatMembers(Long chatId) {

    Chat chat =
        chatService
            .getChat(chatId)
            .orElseThrow(() -> new NotFoundException("Chat with id %s not found", chatId));

    verifyActiveStatus(chat);
    this.chatPermissionVerifier.verifyPermissionForChat(chat);
    verifyRocketChatGroup(chat);

    try {
      return convertGroupMemberDTOListToChatMemberResponseDTO(
          rocketChatService.getStandardMembersOfGroup(chat.getGroupId()));
    } catch (RocketChatGetGroupMembersException | RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  private void verifyActiveStatus(Chat chat) {
    if (isFalse(chat.isActive())) {
      throw new ConflictException(
          String.format(
              "Could not get members of chat with id %s, because it's not started.", chat.getId()));
    }
  }

  private void verifyRocketChatGroup(Chat chat) {
    if (isNull(chat.getGroupId())) {
      throw new InternalServerErrorException(
          String.format("Chat with id %s has no Rocket.Chat group id", chat.getId()));
    }
  }

  private ChatMembersResponseDTO convertGroupMemberDTOListToChatMemberResponseDTO(
      List<GroupMemberDTO> groupMemberDTOList) {
    return new ChatMembersResponseDTO()
        .members(
            groupMemberDTOList.stream()
                .map(
                    member ->
                        new ChatMemberResponseDTO()
                            .id(member.get_id())
                            .userId(getByRcUserIdAndDeleteDateIsNull(member))
                            .status(member.getStatus())
                            .username(new UsernameTranscoder().decodeUsername(member.getUsername()))
                            .displayName(member.getName())
                            .utcOffset(member.getUtcOffset()))
                .collect(Collectors.toList()));
  }

  private String getByRcUserIdAndDeleteDateIsNull(GroupMemberDTO member) {
    Optional<User> user = userRepository.findByRcUserIdAndDeleteDateIsNull(member.get_id());
    if (user.isPresent()) {
      return user.get().getUserId();
    } else {
      Optional<Consultant> consultant =
          consultantRepository.findByRocketChatIdAndDeleteDateIsNull(member.get_id());
      if (consultant.isPresent()) {
        return consultant.get().getId();
      }
    }
    return null;
  }
}
