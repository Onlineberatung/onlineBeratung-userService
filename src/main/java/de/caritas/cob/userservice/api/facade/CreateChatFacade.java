package de.caritas.cob.userservice.api.facade;

import java.util.Optional;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.SaveChatAgencyException;
import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ChatDTO;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;

/**
 * Facade to encapsulate the steps for creating a chat.
 *
 */
@Service
public class CreateChatFacade {

  private final ChatService chatService;
  private final RocketChatService rocketChatService;
  private final UserHelper userHelper;
  private final RocketChatHelper rocketChatHelper;
  private final LogService logService;
  private final ChatHelper chatHelper;

  public CreateChatFacade(ChatService chatService, UserHelper userHelper,
      RocketChatService rocketChatService, RocketChatHelper rocketChatHelper, LogService logService,
      ChatHelper chatHelper) {
    this.chatService = chatService;
    this.userHelper = userHelper;
    this.rocketChatService = rocketChatService;
    this.rocketChatHelper = rocketChatHelper;
    this.logService = logService;
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

      // Get agency id for chat. Premise/presumption: Kreuzbund's consultants are in one agency only
      // (which is a Kreuzbund agency)
      Long agencyId = consultant.getConsultantAgencies() != null
          && !consultant.getConsultantAgencies().isEmpty()
              ? consultant.getConsultantAgencies().iterator().next().getAgencyId()
              : null;

      if (agencyId == null) {
        throw new ServiceException(String
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
      logService.logInternalServerError(
          "Error while creating private group in Rocket.Chat for group chat: " + chatDTO.toString(),
          rocketChatCreateGroupException);
      doRollback(chat, rcGroupId);
      return null;
    } catch (RocketChatLoginException rocketChatLoginException) {
      logService.logInternalServerError("Could not log in technical user for Rocket.Chat API",
          rocketChatLoginException);
      doRollback(chat, rcGroupId);
      return null;
    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      logService.logInternalServerError("Technical user could not be added to group chat room",
          rocketChatAddUserToGroupException);
      doRollback(chat, rcGroupId);
      return null;
    } catch (SaveChatAgencyException saveChatAgencyException) {
      logService.logInternalServerError("Could not save chat agency relation in database",
          saveChatAgencyException);
      doRollback(chat, rcGroupId);
      return null;
    } catch (SaveChatException saveChatException) {
      logService.logInternalServerError("Could not save chat in database", saveChatException);
      doRollback(chat, rcGroupId);
      return null;
    } catch (ServiceException serviceException) {
      logService.logInternalServerError("Could not create chat " + chatDTO.toString(),
          serviceException);
      return null;
    }

    return new CreateChatResponseDTO(rcGroupId,
        userHelper.generateChatUrl(chat.getId(), chat.getConsultingType()));

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
