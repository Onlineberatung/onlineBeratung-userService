package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.chat.ChatDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chat.ChatInterval;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.AgencyService;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for creating a chat.
 */
@Service
@RequiredArgsConstructor
public class CreateChatFacade {

  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull AgencyService agencyService;

  /**
   * Creates a chat in MariaDB, it's relation to the agency and Rocket.Chat-room.
   *
   * @param chatDTO    {@link ChatDTO}
   * @param consultant {@link Consultant}
   * @return the generated chat link URL (String)
   */
  public CreateChatResponseDTO createChat(ChatDTO chatDTO, Consultant consultant) {
    Chat chat = null;
    String rcGroupId = null;

    try {
      // Get agency for chat. Premise/presumption: Group chat consultants are in one agency only
      // (which is a Kreuzbund or a supportgroup agency)
      chat = createChatAgencyRelation(consultant, chatDTO);
      rcGroupId = createRocketChatGroupWithTechnicalUser(chatDTO, chat);
      chat.setGroupId(rcGroupId);
      chatService.saveChat(chat);
      return new CreateChatResponseDTO()
          .groupId(rcGroupId)
          .chatLink(userHelper.generateChatUrl(chat.getId(), chat.getConsultingId()));
    } catch (InternalServerErrorException e) {
      doRollback(chat, rcGroupId);
      throw e;
    }
  }

  private Chat createChatAgencyRelation(Consultant consultant, ChatDTO chatDTO) {
    if (isEmpty(consultant.getConsultantAgencies())) {
      throw new InternalServerErrorException(String
          .format("Consultant with id %s is not assigned to any agency", consultant.getId()));
    }
    Long agencyId = consultant.getConsultantAgencies().iterator().next().getAgencyId();
    AgencyDTO agency = this.agencyService.getAgency(agencyId);

    Chat chat = chatService.saveChat(convertChatDTOtoChat(chatDTO, consultant, agency));
    chatService.saveChatAgencyRelation(new ChatAgency(chat, agencyId));
    return chat;
  }

  private String createRocketChatGroupWithTechnicalUser(ChatDTO chatDTO, Chat chat) {
    String rcGroupId = createRocketChatGroup(chatDTO, chat);
    addTechnicalUserToGroup(chat, rcGroupId);
    return rcGroupId;
  }

  private void addTechnicalUserToGroup(Chat chat, String rcGroupId) {
    try {
      rocketChatService.addTechnicalUserToGroup(rcGroupId);
    } catch (RocketChatAddUserToGroupException e) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException("Technical user could not be added to group chat "
          + "room");
    } catch (RocketChatUserNotInitializedException e) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException("Rocket chat user is not initialized");
    }
  }

  private String createRocketChatGroup(ChatDTO chatDTO, Chat chat) {
    try {
      GroupResponseDTO rcGroupDTO = rocketChatService
          .createPrivateGroupWithSystemUser(
              new RocketChatRoomNameGenerator().generateGroupChatName(chat))
          .orElseThrow(() -> new RocketChatCreateGroupException(
              "RocketChat group is not present while creating chat: " + chatDTO));
      return rcGroupDTO.getGroup().getId();
    } catch (RocketChatCreateGroupException e) {
      doRollback(chat, null);
      throw new InternalServerErrorException(
          "Error while creating private group in Rocket.Chat for group chat: " + chatDTO
              .toString());
    }
  }

  private Chat convertChatDTOtoChat(ChatDTO chatDTO, Consultant consultant, AgencyDTO agencyDTO) {
    LocalDateTime startDate = LocalDateTime.of(chatDTO.getStartDate(), chatDTO.getStartTime());
    // The repetition interval can only be weekly atm.
    return new Chat(chatDTO.getTopic(), agencyDTO.getConsultingId(), startDate, startDate,
        chatDTO.getDuration(), isTrue(chatDTO.isRepetitive()),
        isTrue(chatDTO.isRepetitive()) ? ChatInterval.WEEKLY : null, consultant);
  }

  private void doRollback(Chat chat, String rcGroupId) {
    if (nonNull(chat)) {
      chatService.deleteChat(chat);
    }
    if (nonNull(rcGroupId)) {
      rocketChatService.deleteGroupAsSystemUser(rcGroupId);
    }
  }

}
