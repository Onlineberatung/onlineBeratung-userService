package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAttachmentDTO;
import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Analyser methods for the session list. */
@Component
public class SessionListAnalyser {

  private final DecryptionService decryptionService;

  public SessionListAnalyser(DecryptionService decryptionService) {
    this.decryptionService = requireNonNull(decryptionService);
  }

  /**
   * Decrypts and returns a Rocket.Chat message and truncates it to the maximum length given by the
   * frontend.
   *
   * @param message Encrypted message
   * @param groupId Rocket.Chat group id of the message
   * @return Decrypted message
   */
  public String prepareMessageForSessionList(String message, String groupId) {
    try {
      return decryptionService.decrypt(message, groupId);
    } catch (CustomCryptoException cryptoEx) {
      throw new InternalServerErrorException(
          String.format("Could not decrypt message for group id %s", groupId),
          LogService::logInternalServerError);
    }
  }

  /**
   * Check, if messages for given session were read by user.
   *
   * @param messagesReadMap list with room information from Rocket.Chat
   * @param groupId the Rocket.Chat group id of the session or chat
   * @return true, if messages were read for given thread or no messages in chat room available
   */
  public boolean areMessagesForRocketChatGroupReadByUser(
      Map<String, Boolean> messagesReadMap, String groupId) {
    return messagesReadMap.getOrDefault(groupId, true);
  }

  /**
   * Check if if the last message is available for a Rocket.Chat group.
   *
   * @param roomLastMessageMap the map with the rooms update of Rocket.Chat
   * @param groupId the group id
   * @return true, if last message is available
   */
  public boolean isLastMessageForRocketChatGroupIdAvailable(
      Map<String, RoomsLastMessageDTO> roomLastMessageMap, String groupId) {
    return roomLastMessageMap.containsKey(groupId);
  }

  /**
   * Get a {@link SessionAttachmentDTO} from the Rocket.Chat room info.
   *
   * @param rcUserId the Rocket.Chat user id
   * @param roomsLastMessageDto the Rocket.Chat room info
   * @return a {@link SessionAttachmentDTO} instance
   */
  public SessionAttachmentDTO getAttachmentFromRocketChatMessageIfAvailable(
      String rcUserId, RoomsLastMessageDTO roomsLastMessageDto) {
    if (isNull(roomsLastMessageDto.getFile())) {
      return null;
    }
    return new SessionAttachmentDTO()
        .fileType(roomsLastMessageDto.getFile().getType())
        .imagePreview(roomsLastMessageDto.getAttachements()[0].getImagePreview())
        .fileReceived(!rcUserId.equals(roomsLastMessageDto.getUser().getId()));
  }
}
