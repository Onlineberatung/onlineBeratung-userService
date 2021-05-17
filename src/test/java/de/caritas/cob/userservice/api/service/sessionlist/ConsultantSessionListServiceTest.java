package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.testHelper.TestConstants.ATTACHMENT_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.COUNT_10;
import static de.caritas.cob.userservice.testHelper.TestConstants.FILE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_ONE_FEEDBACK_UNREAD;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.testHelper.TestConstants.OFFSET_0;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ATTACHMENT_DTO_NOT_RECEIVED;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ATTACHMENT_DTO_RECEIVED;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_NEW;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERS_EMPTY_ROOMS_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERS_ROOMS_LIST;
import static java.util.Objects.nonNull;
import static org.jsoup.helper.Validate.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantSessionListServiceTest {

  @InjectMocks
  private ConsultantSessionListService consultantSessionListService;
  @Mock
  private SessionService sessionService;
  @Mock
  private ChatService chatService;
  @Mock
  private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private SessionListAnalyser sessionListAnalyser;

  /**
   * Method: retrieveSessionsForAuthenticatedConsultant
   */

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnValidSessionListWithMessagesReadTrue_WhenThereAreNoUnreadMessages() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID)).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnValidSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID)).thenReturn(false);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID_2)).thenReturn(false);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID_3)).thenReturn(false);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.get(0).getSession().getMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_SetCorrectMessageDate() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);

     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_LAST_MESSAGE_DTO_MAP.get(result.get(0).getSession().getGroupId()).getTimestamp()),
        result.get(0).getSession().getMessageDate());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnFalseAsAttachmentReceivedStatus_WhenCallingConsultantIsSenderOfTheAttachment() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()),
            Mockito.any())).thenReturn(SESSION_ATTACHMENT_DTO_NOT_RECEIVED);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT_2,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.get(1).getSession().getAttachment().getFileReceived());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnTrueAsAttachmentReceivedStatus_WhenCallingConsultantIsNotSenderOfTheAttachment() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()),
            Mockito.any())).thenReturn(SESSION_ATTACHMENT_DTO_RECEIVED);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT_2,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getAttachment().getFileReceived());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_SetSubscribedFlagToTrue_WhenConsultantIsAttendeeOfAChat() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(Collections.emptyList());
    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertFalse(result.isEmpty());
    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : result) {
      assertTrue(consultantSessionResponseDTO.getChat().isSubscribed());
    }

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_SetSubscribedFlagToFalse_WhenConsultantIsNotAttendeeOfAChat() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(Collections.emptyList());
    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_EMPTY_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertFalse(result.isEmpty());
    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : result) {
      assertFalse(consultantSessionResponseDTO.getChat().isSubscribed());
    }

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnOnlySessions_WhenQueryParameterSessionStatusIsNew() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_EMPTY_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.isEmpty());
    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.size());
    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : result) {
      assertNull(consultantSessionResponseDTO.getChat());
    }
    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : result) {
      assertNotNull(consultantSessionResponseDTO.getSession());
    }
    verify(chatService, never()).getChatsForConsultant(CONSULTANT);

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_ShouldNot_SendChatsInEnquiryList() {

    when(sessionService.getEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_EMPTY_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertNull(result.get(0).getChat());
    verify(chatService, never()).getChatsForConsultant(Mockito.any());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_ShouldNot_SetIsFeedbackReadToFalse_WhenNoMessageWasPostedInTheFeedbackRoom() {

    when(sessionService.getEnquiriesForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_2);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_ONE_FEEDBACK_UNREAD)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_SendListWithMonitoringFalse_When_NoMonitoringSetInConsultingTypeSettings() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.get(0).getSession().getMonitoring());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_SendListWithMonitoringTrue_When_MonitoringSetInConsultingTypeSettings() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getMonitoring());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectFileTypeAndImagePreviewForSession() {

    when(sessionService.getEnquiriesForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()),
            Mockito.any())).thenReturn(SESSION_ATTACHMENT_DTO_NOT_RECEIVED);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertEquals(FILE_DTO.getType(),
        result.get(0).getSession().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        result.get(0).getSession().getAttachment().getImagePreview());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectFileTypeAndImagePreviewForChat() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any())).thenReturn(Collections.emptyList());
    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
    when(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()),
            Mockito.any())).thenReturn(SESSION_ATTACHMENT_DTO_NOT_RECEIVED);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertEquals(FILE_DTO.getType(),
        result.get(0).getChat().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        result.get(0).getChat().getAttachment().getImagePreview());

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_MergeSessionsAndChats() {

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertNotNull(result);
    assertEquals(result.size(),
        CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE.size()
            + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size());

    for (ConsultantSessionResponseDTO dto : CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE) {
      boolean containsChat = false;
      for (ConsultantSessionResponseDTO chat : result) {
        if (nonNull(dto.getChat())
            && dto.getChat().equals(chat.getChat())) {
          containsChat = true;
          break;
        }
      }
      if (!containsChat) {
        fail("ResponseList does not contain all expected chats");
      }
    }

    for (ConsultantSessionResponseDTO dto : CONSULTANT_SESSION_RESPONSE_DTO_LIST) {
      boolean containsSession = false;
      for (ConsultantSessionResponseDTO session : result) {
        if (nonNull(dto.getSession())
            && dto.getSession().equals(session.getSession())) {
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
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages() {

    when(sessionService.getEnquiriesForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID)).thenReturn(false);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.isEmpty());
    assertFalse(result.get(0).getSession().getMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenThereAreNoUnreadFeedbackMessages() {

    when(sessionService.getEnquiriesForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnMessageDateAsUnixtime0_WhenNoMessages() {

    when(sessionService.getEnquiriesForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertEquals(Long.valueOf(Helper.UNIXTIME_0.getTime()),
        result.get(0).getSession().getMessageDate());

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenFeedbackGroupIdIsNull() {

    when(sessionService.getEnquiriesForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadFalse_WhenThereAreUnreadChatMessages() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(Collections.emptyList());
    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertFalse(result.get(0).getChat().isMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadChatMessages() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(Collections.emptyList());
    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertTrue(result.get(0).getChat().isMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadFalse_WhenThereAreUnreadSessionMessages() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        Mockito.any(), Mockito.any())).thenReturn(false);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertFalse(result.get(0).getSession().getMessagesRead());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadSessionMessages() {

    when(sessionService.getActiveSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        Mockito.any(), Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertTrue(result.get(0).getSession().getMessagesRead());
  }

  /**
   * Method: retrieveTeamSessionsForAuthenticatedConsultant
   */

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithMessagesReadTrue_WhenThereAreNoUnreadMessages() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID)).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.isEmpty());
    assertTrue(result.get(0).getSession().getMessagesRead());
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            RC_GROUP_ID)).thenReturn(false);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.isEmpty());
    assertFalse(result.get(0).getSession().getMessagesRead());
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenThereAreNoUnreadFeedbackMessages() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenFeedbackGroupIdIsNull() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertTrue(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnMessageDateAsUnixtime0_WhenNoMessages() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertEquals(Long.valueOf(Helper.UNIXTIME_0.getTime()),
        result.get(0).getSession().getMessageDate());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_SetCorrectMessageDate() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .isLastMessageForRocketChatGroupIdAvailable(
            Mockito.any(),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        result.get(0).getSession().getMessageDate());
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnFilteredSessionList_WhenFeedbackFilter() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_ONE_FEEDBACK_UNREAD)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    SessionListQueryParameter sessionListQueryParameter =
        SessionListQueryParameter.builder()
            .sessionStatus(SESSION_STATUS_NEW)
            .offset(OFFSET_0)
            .count(COUNT_10)
            .sessionFilter(SessionFilter.FEEDBACK)
            .build();

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, sessionListQueryParameter);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_ShouldNot_SetIsFeedbackReadToFalse_WhenNoMessageWasPostedInTheFeedbackRoom() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_ONE_FEEDBACK_UNREAD)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
     
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()),
            Mockito.any())).thenReturn(true);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.get(0).getSession().getFeedbackRead());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectFileTypeAndImagePreviewForSession() {

    when(sessionService.getTeamSessionsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.anyInt()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);
    when(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()),
            Mockito.any())).thenReturn(SESSION_ATTACHMENT_DTO_NOT_RECEIVED);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService
            .retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
                RC_TOKEN, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertEquals(FILE_DTO.getType(),
        result.get(0).getSession().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        result.get(0).getSession().getAttachment().getImagePreview());
  }

  private SessionListQueryParameter createStandardSessionListQueryParameterObject(
      int sessionStatus) {
    return SessionListQueryParameter.builder()
        .sessionStatus(sessionStatus)
        .offset(OFFSET_0)
        .count(COUNT_10)
        .sessionFilter(SessionFilter.ALL)
        .build();
  }

}
