package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ATTACHMENT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FILE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_3;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_4;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_5;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT_FOR_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ATTACHMENT_DTO_RECEIVED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERS_EMPTY_ROOMS_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERS_ROOMS_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_CHAT_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static org.jsoup.helper.Validate.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserSessionListServiceTest {

  @InjectMocks private UserSessionListService userSessionListService;
  @Mock private SessionService sessionService;
  @Mock private ChatService chatService;
  @Mock private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  @Mock private SessionListAnalyser sessionListAnalyser;

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithSessionMessagesReadTrue_WhenThereAreNoUnreadMessages() {

    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .lastMessagesRoom(emptyMap())
            .groupIdToLastMessageFallbackDate(emptyMap())
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID))
        .thenReturn(true);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_2))
        .thenReturn(true);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_3))
        .thenReturn(true);

    assertTrue(
        userSessionListService
            .retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS)
            .get(0)
            .getSession()
            .getMessagesRead());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadMessages() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .lastMessagesRoom(emptyMap())
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_4))
        .thenReturn(true);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_5))
        .thenReturn(true);

    assertTrue(
        userSessionListService
            .retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS)
            .get(0)
            .getChat()
            .isMessagesRead());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithSessionMessagesReadFalse_WhenThereAreUnreadMessages() {

    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .lastMessagesRoom(emptyMap())
            .groupIdToLastMessageFallbackDate(emptyMap())
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID))
        .thenReturn(false);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_2))
        .thenReturn(false);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_3))
        .thenReturn(false);

    assertFalse(
        userSessionListService
            .retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS)
            .get(0)
            .getSession()
            .getMessagesRead());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithChatMessagesReadFalse_WhenThereAreUnreadMessages() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .lastMessagesRoom(emptyMap())
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_4))
        .thenReturn(false);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID_5))
        .thenReturn(false);

    assertFalse(
        userSessionListService
            .retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS)
            .get(0)
            .getChat()
            .isMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedUser_Should_SetCorrectChatMessageDate() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(
        Helper.getUnixTimestampFromDate(
            ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        result.get(0).getChat().getMessageDate());
  }

  @Test
  public void retrieveSessionsForAuthenticatedUser_Should_SetCorrectSessionMessageDate() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(
        Helper.getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessageDate()),
        result.get(0).getSession().getMessageDate());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnCorrectFileTypeAndImagePreviewForSession() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(RC_CREDENTIALS.getRocketChatUserId()), Mockito.any()))
        .thenReturn(SESSION_ATTACHMENT_DTO_RECEIVED);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(FILE_DTO.getType(), result.get(0).getSession().getAttachment().getFileType());
    assertEquals(
        ATTACHMENT_DTO.getImagePreview(),
        result.get(0).getSession().getAttachment().getImagePreview());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnCorrectFileTypeAndImagePreviewForChat() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT_FOR_CHAT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(RC_CREDENTIALS.getRocketChatUserId()), Mockito.any()))
        .thenReturn(SESSION_ATTACHMENT_DTO_RECEIVED);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(FILE_DTO.getType(), result.get(0).getChat().getAttachment().getFileType());
    assertEquals(
        ATTACHMENT_DTO.getImagePreview(),
        result.get(0).getChat().getAttachment().getImagePreview());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_ReturnEmptyList_WhenSessionAndChatListAreEmpty() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(Collections.emptyList());

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void retrieveSessionsForAuthenticatedUser_Should_MergeSessionsAndChats() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertNotNull(result);
    assertEquals(
        result.size(), USER_CHAT_RESPONSE_DTO_LIST.size() + USER_SESSION_RESPONSE_DTO_LIST.size());

    for (UserSessionResponseDTO userSessionResponseDTO : USER_CHAT_RESPONSE_DTO_LIST) {
      boolean containsChat = false;
      for (UserSessionResponseDTO dto : result) {
        if (nonNull(dto.getChat()) && dto.getChat().equals(userSessionResponseDTO.getChat())) {
          containsChat = true;
          break;
        }
      }
      if (!containsChat) {
        fail("ResponseList does not contain all expected chats");
      }
    }

    for (UserSessionResponseDTO userSessionResponseDTO : USER_SESSION_RESPONSE_DTO_LIST) {
      boolean containsSession = false;
      for (UserSessionResponseDTO dto : result) {
        if (nonNull(dto.getSession())
            && dto.getSession().equals(userSessionResponseDTO.getSession())) {
          containsSession = true;
          break;
        }
      }
      if (!containsSession) {
        fail("ResponseList does not contain all expected sessions");
      }
    }
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_SetSubscribedFlagToTrue_WhenUserIsAttendeeOfAChat() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO userSessionResponseDTO : result) {
      assertTrue(userSessionResponseDTO.getChat().isSubscribed());
    }
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedUser_Should_SetSubscribedFlagToFalse_WhenUserIsNotAttendeeOfAChat() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_EMPTY_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);

    List<UserSessionResponseDTO> result =
        userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO userSessionResponseDTO : result) {
      assertFalse(userSessionResponseDTO.getChat().isSubscribed());
    }
  }
}
