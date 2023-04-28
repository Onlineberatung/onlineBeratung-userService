package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toDate;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ATTACHMENT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_WITHOUT_FEEDBACK_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FILE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_ONE_FEEDBACK_UNREAD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ATTACHMENT_DTO_NOT_RECEIVED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ATTACHMENT_DTO_RECEIVED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERS_ROOMS_LIST;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.service.session.SessionTopicEnrichmentService;
import java.util.Date;
import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantSessionEnricherTest {

  @InjectMocks private ConsultantSessionEnricher consultantSessionEnricher;

  @Mock private SessionListAnalyser sessionListAnalyser;

  @Mock private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  @Mock private ConsultingTypeManager consultingTypeManager;

  @Mock private SessionTopicEnrichmentService sessionTopicEnrichmentService;

  @After
  public void tearDown() {
    ReflectionTestUtils.setField(consultantSessionEnricher, "topicsFeatureEnabled", false);
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnValidSessionListWithMessagesReadTrue_WhenThereAreNoUnreadMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID))
        .thenReturn(true);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertTrue(result.getSession().getMessagesRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnValidSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID))
        .thenReturn(false);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertFalse(result.getSession().getMessagesRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_EnrichWithTopicsIfTopicFeatureIsEnabled() {
    ReflectionTestUtils.setField(consultantSessionEnricher, "topicsFeatureEnabled", true);
    ReflectionTestUtils.setField(
        consultantSessionEnricher, "sessionTopicEnrichmentService", sessionTopicEnrichmentService);

    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID))
        .thenReturn(false);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertFalse(result.getSession().getMessagesRead());
    verify(sessionTopicEnrichmentService)
        .enrichSessionWithTopicData(CONSULTANT_SESSION_RESPONSE_DTO.getSession());
  }

  @Test
  public void updateRequiredConsultantSessionValues_Should_SetCorrectMessageDate() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertEquals(
        Helper.getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessageDate()),
        result.getSession().getMessageDate());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnFalseAsAttachmentReceivedStatus_WhenCallingConsultantIsSenderOfTheAttachment() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()), Mockito.any()))
        .thenReturn(SESSION_ATTACHMENT_DTO_NOT_RECEIVED);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO_2), RC_TOKEN, CONSULTANT)
            .get(0);

    assertFalse(result.getSession().getAttachment().getFileReceived());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnTrueAsAttachmentReceivedStatus_WhenCallingConsultantIsNotSenderOfTheAttachment() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()), Mockito.any()))
        .thenReturn(SESSION_ATTACHMENT_DTO_RECEIVED);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertTrue(result.getSession().getAttachment().getFileReceived());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_ShouldNot_SetIsFeedbackReadToFalse_WhenNoMessageWasPostedInTheFeedbackRoom() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_ONE_FEEDBACK_UNREAD)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()), Mockito.any()))
        .thenReturn(true);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertFalse(result.getSession().getFeedbackRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_SendListWithMonitoringFalse_When_NoMonitoringSetInConsultingTypeSettings() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_SendListWithMonitoringTrue_When_MonitoringSetInConsultingTypeSettings() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnCorrectFileTypeAndImagePreviewForSession() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            Mockito.eq(CONSULTANT_2.getRocketChatId()), Mockito.any()))
        .thenReturn(SESSION_ATTACHMENT_DTO_NOT_RECEIVED);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertEquals(FILE_DTO.getType(), result.getSession().getAttachment().getFileType());
    assertEquals(
        ATTACHMENT_DTO.getImagePreview(), result.getSession().getAttachment().getImagePreview());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), RC_GROUP_ID))
        .thenReturn(false);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertFalse(result.getSession().getMessagesRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnSessionListWithFeedbackReadTrue_WhenThereAreNoUnreadFeedbackMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()), Mockito.any()))
        .thenReturn(true);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertTrue(result.getSession().getFeedbackRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnMessageDateAsUnixtime0_WhenNoMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(emptyMap())
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertEquals(Long.valueOf(Helper.UNIXTIME_0.getTime()), result.getSession().getMessageDate());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnSessionListWithFeedbackReadTrue_WhenFeedbackGroupIdIsNull() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            Mockito.eq(rocketChatRoomInformation.getReadMessages()), Mockito.any()))
        .thenReturn(true);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO_WITHOUT_FEEDBACK_CHAT),
                RC_TOKEN,
                CONSULTANT)
            .get(0);

    assertTrue(result.getSession().getFeedbackRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnSessionListWithChatMessagesReadFalse_WhenThereAreUnreadSessionMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(Mockito.any(), Mockito.any()))
        .thenReturn(false);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertFalse(result.getSession().getMessagesRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadSessionMessages() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT)
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(Mockito.any(), Mockito.any()))
        .thenReturn(true);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertTrue(result.getSession().getMessagesRead());
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_Should_ReturnMessageDateAsFromCreateDate_When_sessionIsAnonymous() {
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITHOUT_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .groupIdToLastMessageFallbackDate(emptyMap())
            .lastMessagesRoom(ROOMS_LAST_MESSAGE_DTO_MAP)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);
    ConsultantSessionResponseDTO consultantSessionResponseDTO =
        new EasyRandom().nextObject(ConsultantSessionResponseDTO.class);
    consultantSessionResponseDTO.getSession().setRegistrationType("ANONYMOUS");
    String createDate = nowInUtc().toString();
    consultantSessionResponseDTO.getSession().setCreateDate(createDate);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(consultantSessionResponseDTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertThat(result.getLatestMessage(), is(toDate(createDate)));
  }

  @Test
  public void
      updateRequiredConsultantSessionValues_should_use_fallback_date_if_last_message_is_unavailable() {
    var fallbackDates = Map.of(RC_GROUP_ID, new Date(1655730882738L));
    RocketChatRoomInformation rocketChatRoomInformation =
        RocketChatRoomInformation.builder()
            .readMessages(MESSAGES_READ_MAP_WITH_UNREADS)
            .roomsForUpdate(ROOMS_UPDATE_DTO_LIST)
            .lastMessagesRoom(emptyMap())
            .groupIdToLastMessageFallbackDate(fallbackDates)
            .userRooms(USERS_ROOMS_LIST)
            .build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(Mockito.any()))
        .thenReturn(rocketChatRoomInformation);

    ConsultantSessionResponseDTO result =
        consultantSessionEnricher
            .updateRequiredConsultantSessionValues(
                singletonList(CONSULTANT_SESSION_RESPONSE_DTO), RC_TOKEN, CONSULTANT)
            .get(0);

    assertEquals(Long.valueOf(1655730882L), result.getSession().getMessageDate());
  }
}
