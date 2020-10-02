package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE_TOO_LONG;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_6;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_7;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_WITHOUT_ATTACHMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;

import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.model.SessionAttachmentDTO;
import de.caritas.cob.userservice.api.service.DecryptionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionListHelperTest {

  @InjectMocks
  private SessionListHelper sessionListHelper;
  @Mock
  private DecryptionService decryptionService;

  /*
   * Method: prepareMessageForSessionList
   */
  @Test
  public void prepareMessageForSessionList_Should_TruncateMessage() throws CustomCryptoException {
    when(decryptionService.decrypt(MESSAGE_TOO_LONG, RC_GROUP_ID)).thenReturn(MESSAGE_TOO_LONG);
    String result = sessionListHelper.prepareMessageForSessionList(MESSAGE_TOO_LONG, RC_GROUP_ID);
    assertEquals(SessionListHelper.MAX_MESSAGE_LENGTH_FOR_FRONTEND, result.length());
  }

  @Test
  public void prepareMessageForSessionList_Should_DecryptMessage() throws CustomCryptoException {
    when(decryptionService.decrypt(MESSAGE_TOO_LONG, RC_GROUP_ID)).thenReturn(MESSAGE_TOO_LONG);
    String result = sessionListHelper.prepareMessageForSessionList(MESSAGE_TOO_LONG, RC_GROUP_ID);
    verify(decryptionService, atLeastOnce()).decrypt(MESSAGE_TOO_LONG, RC_GROUP_ID);
  }

  /*
   * Method: isMessagesForRocketChatGroupReadByUser
   */
  @Test
  public void isMessagesForRocketChatGroupReadByUser_Should_ReturnTrue_IfMessageWasRead() {
    assertTrue(sessionListHelper.isMessagesForRocketChatGroupReadByUser(MESSAGES_READ_MAP_WITHOUT_UNREADS, RC_GROUP_ID));
  }

  @Test
  public void isMessagesForRocketChatGroupReadByUser_Should_ReturnFalse_IfMessageWasNotRead() {
    assertFalse(sessionListHelper.isMessagesForRocketChatGroupReadByUser(MESSAGES_READ_MAP_WITH_UNREADS, RC_GROUP_ID));
  }

  @Test
  public void isMessagesForRocketChatGroupReadByUser_Should_ReturnTrue_IfNoMessageReadInfoAvailableInMessageReadMap() {
    assertTrue(sessionListHelper.isMessagesForRocketChatGroupReadByUser(MESSAGES_READ_MAP_WITHOUT_UNREADS, RC_GROUP_ID_6));
  }

  /*
   * Method: isLastMessageForRocketChatGroupIdAvailable
   */
  @Test
  public void isLastMessageForRocketChatGroupIdAvailable_Should_ReturnTrue_When_LastMessageIsAvailableForGroup() {
    assertTrue(sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(ROOMS_LAST_MESSAGE_DTO_MAP, RC_GROUP_ID));
  }

  @Test
  public void isLastMessageForRocketChatGroupIdAvailable_Should_ReturnFalse_When_LastMessageIsNotAvailableForGroup() {
    assertFalse(sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(ROOMS_LAST_MESSAGE_DTO_MAP, RC_GROUP_ID_7));
  }

  /*
  * Method: getAttachmentFromRocketChatMessageIfAvailable
   */
  @Test
  public void getAttachmentFromRocketChatMessageIfAvailable_Should_ReturnNull_When_FileNotAvailable() {
    assertNull(sessionListHelper.getAttachmentFromRocketChatMessageIfAvailable(RC_USER_ID, ROOMS_LAST_MESSAGE_DTO_WITHOUT_ATTACHMENT));
  }

  @Test
  public void getAttachmentFromRocketChatMessageIfAvailable_Should_ReturnSetFileReceivedToFalse_When_CallingUserIsSender() {
    SessionAttachmentDTO result = sessionListHelper.getAttachmentFromRocketChatMessageIfAvailable(ROCKETCHAT_ID_2, ROOMS_LAST_MESSAGE_DTO_2);
    assertFalse(result.isFileReceived());
  }

  @Test
  public void getAttachmentFromRocketChatMessageIfAvailable_Should_ReturnSetFileReceivedToTrue_When_CallingUserIsNotSender() {
    SessionAttachmentDTO result = sessionListHelper.getAttachmentFromRocketChatMessageIfAvailable(ROCKETCHAT_ID, ROOMS_LAST_MESSAGE_DTO_2);
    assertTrue(result.isFileReceived());
  }

}
