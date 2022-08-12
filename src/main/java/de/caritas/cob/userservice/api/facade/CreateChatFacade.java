package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

  private final @NonNull ChatConverter chatConverter;

  /**
   * Creates a chat in MariaDB, it's relation to the agency and Rocket.Chat-room.
   *
   * @param chatDTO    {@link ChatDTO}
   * @param consultant {@link Consultant}
   * @return the generated chat link URL (String)
   */
  public CreateChatResponseDTO createChatV1(ChatDTO chatDTO, Consultant consultant) {
    return createChat(chatDTO, consultant, this::saveChatAndChatAgencyRelation);
  }

  /**
   * Creates a chat in MariaDB and Rocket.Chat-room and do not save any chat_agency relation.
   *
   * @param chatDTO    {@link ChatDTO}
   * @param consultant {@link Consultant}
   * @return the generated chat link URL (String)
   */
  public CreateChatResponseDTO createChatV2(ChatDTO chatDTO, Consultant consultant) {
    return createChat(chatDTO, consultant, this::saveChat);
  }

  private CreateChatResponseDTO createChat(ChatDTO chatDTO, Consultant consultant,
      BiFunction<Consultant, ChatDTO, Chat> saveChat) {
    Chat chat = null;
    String rcGroupId = null;

    try {
      chat = saveChat.apply(consultant, chatDTO);
      rcGroupId = createRocketChatGroupWithTechnicalUser(chatDTO, chat);
      chat.setGroupId(rcGroupId);
      chatService.saveChat(chat);
      return new CreateChatResponseDTO()
          .groupId(rcGroupId)
          .chatLink(generateChatUrl(chat));
    } catch (InternalServerErrorException e) {
      doRollback(chat, rcGroupId);
      throw e;
    }
  }

  private String generateChatUrl(Chat chat) {
    return chat.getConsultingTypeId() != null ? userHelper.generateChatUrl(chat.getId(),
        chat.getConsultingTypeId()) : StringUtils.EMPTY;
  }

  private Chat saveChat(Consultant consultant, ChatDTO chatDTO) {
    return chatService.saveChat(chatConverter.convertToEntity(chatDTO, consultant));
  }

  private Chat saveChatAndChatAgencyRelation(Consultant consultant, ChatDTO chatDTO) {
    if (isEmpty(consultant.getConsultantAgencies())) {
      throw new InternalServerErrorException(String
          .format("Consultant with id %s is not assigned to any agency", consultant.getId()));
    }
    Long agencyId = consultant.getConsultantAgencies().iterator().next().getAgencyId();
    AgencyDTO agency = this.agencyService.getAgency(agencyId);

    Chat chat = chatService.saveChat(chatConverter.convertToEntity(chatDTO, consultant, agency));
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

  private void doRollback(Chat chat, String rcGroupId) {
    if (nonNull(chat)) {
      chatService.deleteChat(chat);
    }
    if (nonNull(rcGroupId)) {
      rocketChatService.deleteGroupAsSystemUser(rcGroupId);
    }
  }

}
