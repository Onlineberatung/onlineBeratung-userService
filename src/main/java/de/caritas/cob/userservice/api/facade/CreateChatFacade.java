package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.exception.SaveChatAgencyException;
import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.chat.ChatDTO;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for creating a chat.
 */
@Service
public class CreateChatFacade {

  private final ChatService chatService;
  private final RocketChatService rocketChatService;
  private final UserHelper userHelper;
  private final RocketChatHelper rocketChatHelper;
  private final ChatHelper chatHelper;

  public CreateChatFacade(ChatService chatService, UserHelper userHelper,
      RocketChatService rocketChatService, RocketChatHelper rocketChatHelper,
      ChatHelper chatHelper) {
    this.chatService = chatService;
    this.userHelper = userHelper;
    this.rocketChatService = rocketChatService;
    this.rocketChatHelper = rocketChatHelper;
    this.chatHelper = chatHelper;
  }

  /**
   * Creates a chat in MariaDB, it's relation to the agency and Rocket.Chat-room.
   *
   * @param chatDTO {@link ChatDTO}
   * @param consultant {@link Consultant}
   * @return the generated chat link URL (String)
   */
  public CreateChatResponseDTO createChat(ChatDTO chatDTO, Consultant consultant) {

    Chat chat = null;
    String rcGroupId = null;

    try {

      // Get agency id for chat. Premise/presumption: Kreuzbund consultants are in one agency only
      // (which is a Kreuzbund agency)
      Long agencyId = consultant.getConsultantAgencies() != null
          && !consultant.getConsultantAgencies().isEmpty()
          ? consultant.getConsultantAgencies().iterator().next().getAgencyId()
          : null;

      if (agencyId == null) {
        throw new InternalServerErrorException(String
            .format("Consultant with id %s is not assigned to any agency", consultant.getId()));
      }

      chat = chatService.saveChat(chatHelper.convertChatDTOtoChat(chatDTO, consultant));
      chatService.saveChatAgencyRelation(new ChatAgency(chat, agencyId));

      Optional<GroupResponseDTO> rcGroupDTO = rocketChatService
          .createPrivateGroupWithSystemUser(rocketChatHelper.generateGroupChatName(chat));

      if (!rcGroupDTO.isPresent()) {
        throw new RocketChatCreateGroupException(
            "RocketChat group is not present while creating chat: " + chatDTO.toString());
      }

      rcGroupId = rcGroupDTO.get().getGroup().getId();
      rocketChatService.addTechnicalUserToGroup(rcGroupId);

      chat.setGroupId(rcGroupId);
      chatService.saveChat(chat);

    } catch (RocketChatCreateGroupException rocketChatCreateGroupException) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException(
          "Error while creating private group in Rocket.Chat for group chat: " + chatDTO
              .toString());
    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException("Technical user could not be added to group chat "
          + "room");
    } catch (SaveChatAgencyException saveChatAgencyException) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException("Could not save chat agency relation in database");
    } catch (SaveChatException saveChatException) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException("Could not save chat in database");
    } catch (RocketChatUserNotInitializedException e) {
      doRollback(chat, rcGroupId);
      throw new InternalServerErrorException("Rocket chat user is not initialized");
    } catch (InternalServerErrorException e) {
      doRollback(chat, rcGroupId);
      throw e;
    }

    return new CreateChatResponseDTO()
        .groupId(rcGroupId)
        .chatLink(userHelper.generateChatUrl(chat.getId(), chat.getConsultingType()));

  }

  private void doRollback(Chat chat, String rcGroupId) {
    if (chat != null) {
      chatService.deleteChat(chat);
    }
    if (rcGroupId != null) {
      rocketChatService.deleteGroupAsSystemUser(rcGroupId);
    }
  }

}
