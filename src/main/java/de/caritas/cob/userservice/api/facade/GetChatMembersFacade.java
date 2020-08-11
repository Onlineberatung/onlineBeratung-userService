package de.caritas.cob.userservice.api.facade;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
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
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.UserService;

/**
 * Facade to encapsulate the steps for get the chat members.
 *
 */

@Service
public class GetChatMembersFacade {

  private ChatService chatService;
  private UserService userService;
  private ConsultantService consultantService;
  private RocketChatService rocketChatService;
  private ChatHelper chatHelper;
  private UserHelper userHelper;

  @Autowired
  public GetChatMembersFacade(ChatService chatService, UserService userService,
      ChatHelper chatHelper, ConsultantService consultantService,
      RocketChatService rocketChatService, UserHelper userHelper) {
    this.chatService = chatService;
    this.userService = userService;
    this.chatHelper = chatHelper;
    this.consultantService = consultantService;
    this.rocketChatService = rocketChatService;
    this.userHelper = userHelper;
  }

  /**
   * Get a filtered list of the members of a chat (without technical/system user)
   * 
   * @param chatId
   * @param authenticatedUser
   * @throws NotFoundException, ConflictException, InternalServerErrorException
   * @return
   */
  public ChatMembersResponseDTO getChatMembers(Long chatId, AuthenticatedUser authenticatedUser) {

    Optional<Chat> chat = chatService.getChat(chatId);
    if (!chat.isPresent()) {
      throw new NotFoundException(String.format("Chat with id %s not found", chatId));
    }

    if (!chat.get().isActive()) {
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

    return convertGroupMemberDTOListToChatMemberResponseDTO(
        rocketChatService.getStandardMembersOfGroup(chat.get().getGroupId()));
  }

  /**
   * Convert a list of GroupMemberDTOs to a ChatMemberResponseDTO
   * 
   * @param groupMemberDTOList
   * @return
   */
  private ChatMembersResponseDTO convertGroupMemberDTOListToChatMemberResponseDTO(
      List<GroupMemberDTO> groupMemberDTOList) {
    return new ChatMembersResponseDTO(groupMemberDTOList.stream()
        .map(member -> new ChatMemberResponseDTO(member.get_id(), member.getStatus(),
            userHelper.decodeUsername(member.getUsername()), member.getName(),
            member.getUtcOffset()))
        .toArray(ChatMemberResponseDTO[]::new));
  }

}
