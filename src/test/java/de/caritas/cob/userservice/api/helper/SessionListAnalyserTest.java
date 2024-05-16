package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGE_TOO_LONG;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_6;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_7;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_WITHOUT_ATTACHMENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionAttachmentDTO;
import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.service.DecryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionListAnalyserTest {

  @InjectMocks private SessionListAnalyser sessionListAnalyser;
  @Mock private DecryptionService decryptionService;

  @Test
  public void prepareMessageForSessionList_Should_DecryptMessage() throws CustomCryptoException {
    when(decryptionService.decrypt(MESSAGE_TOO_LONG, RC_GROUP_ID)).thenReturn(MESSAGE_TOO_LONG);
    String result = sessionListAnalyser.prepareMessageForSessionList(MESSAGE_TOO_LONG, RC_GROUP_ID);
    verify(decryptionService, atLeastOnce()).decrypt(MESSAGE_TOO_LONG, RC_GROUP_ID);
  }

  /*
   * Method: isMessagesForRocketChatGroupReadByUser
   */
  @Test
  public void isMessagesForRocketChatGroupReadByUser_Should_ReturnTrue_IfMessageWasRead() {
    assertTrue(
        sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            MESSAGES_READ_MAP_WITHOUT_UNREADS, RC_GROUP_ID));
  }

  @Test
  public void isMessagesForRocketChatGroupReadByUser_Should_ReturnFalse_IfMessageWasNotRead() {
    assertFalse(
        sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            MESSAGES_READ_MAP_WITH_UNREADS, RC_GROUP_ID));
  }

  @Test
  public void
      isMessagesForRocketChatGroupReadByUser_Should_ReturnTrue_IfNoMessageReadInfoAvailableInMessageReadMap() {
    assertTrue(
        sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            MESSAGES_READ_MAP_WITHOUT_UNREADS, RC_GROUP_ID_6));
  }

  /*
   * Method: isLastMessageForRocketChatGroupIdAvailable
   */
  @Test
  public void
      isLastMessageForRocketChatGroupIdAvailable_Should_ReturnTrue_When_LastMessageIsAvailableForGroup() {
    assertTrue(
        sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
            ROOMS_LAST_MESSAGE_DTO_MAP, RC_GROUP_ID));
  }

  @Test
  public void
      isLastMessageForRocketChatGroupIdAvailable_Should_ReturnFalse_When_LastMessageIsNotAvailableForGroup() {
    assertFalse(
        sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
            ROOMS_LAST_MESSAGE_DTO_MAP, RC_GROUP_ID_7));
  }

  /*
   * Method: getAttachmentFromRocketChatMessageIfAvailable
   */
  @Test
  public void
      getAttachmentFromRocketChatMessageIfAvailable_Should_ReturnNull_When_FileNotAvailable() {
    assertNull(
        sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            RC_USER_ID, ROOMS_LAST_MESSAGE_DTO_WITHOUT_ATTACHMENT));
  }

  @Test
  public void
      getAttachmentFromRocketChatMessageIfAvailable_Should_ReturnSetFileReceivedToFalse_When_CallingUserIsSender() {
    SessionAttachmentDTO result =
        sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            ROCKETCHAT_ID_2, ROOMS_LAST_MESSAGE_DTO_2);
    assertFalse(result.getFileReceived());
  }

  @Test
  public void
      getAttachmentFromRocketChatMessageIfAvailable_Should_ReturnSetFileReceivedToTrue_When_CallingUserIsNotSender() {
    SessionAttachmentDTO result =
        sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            ROCKETCHAT_ID, ROOMS_LAST_MESSAGE_DTO_2);
    assertTrue(result.getFileReceived());
  }
}
