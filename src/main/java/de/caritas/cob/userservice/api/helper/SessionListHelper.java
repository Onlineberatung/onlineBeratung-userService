package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.SessionAttachmentDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
/**
 * Helper methods for the session list.
 */
public class SessionListHelper {

  private static final int MAX_MESSAGE_LENGTH_FOR_FRONTEND = 100;

  private final DecryptionService decryptionService;

  @Autowired
  public SessionListHelper(
      DecryptionService decryptionService) {
    this.decryptionService = decryptionService;
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
      return truncateMessageToMaximalLengthForFrontend(decryptionService.decrypt(message, groupId));
    } catch (CustomCryptoException cryptoEx) {
      throw new InternalServerErrorException(
          String.format("Could not decrypt message for group id %s", groupId),
          LogService::logInternalServerError);
    }
  }

  private String truncateMessageToMaximalLengthForFrontend(String decryptedMessage) {
    if (StringUtils.isEmpty(decryptedMessage.trim())) {
      return null;
    }
    return StringUtils.left(decryptedMessage,
        Math.min(decryptedMessage.length(), MAX_MESSAGE_LENGTH_FOR_FRONTEND));
  }

  /**
   * Check, if messages for given session were read by user.
   *
   * @param messagesReadMap list with room information from Rocket.Chat
   * @param groupId         the Rocket.Chat group id of the session or chat
   * @return true, if messages were read for given thread or no messages in chat room available
   */
  public boolean isMessagesForRocketChatGroupReadByUser(Map<String, Boolean> messagesReadMap,
      String groupId) {
    return messagesReadMap.getOrDefault(groupId, true);
  }

  /**
   * Check if if the last message is available for a Rocket.Chat group
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
   * Get a {@link SessionAttachmentDTO} from the Rocket.Chat room info
   *
   * @param rcUserId the Rocket.Chat user id
   * @param roomsLastMessageDto the Rocket.Chat room info
   * @return a {@link SessionAttachmentDTO} instance
   */
  public SessionAttachmentDTO getAttachmentFromRocketChatMessageIfAvailable(String rcUserId,
      RoomsLastMessageDTO roomsLastMessageDto) {
    if (isNull(roomsLastMessageDto.getFile())) {
      return null;
    }
    return new SessionAttachmentDTO(roomsLastMessageDto.getFile().getType(),
        roomsLastMessageDto.getAttachements()[0].getImagePreview(),
        !rcUserId.equals(roomsLastMessageDto.getUser().getId()));
  }

}
