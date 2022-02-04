package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ATTACHMENT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FILE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ATTACHMENT_DTO_NOT_RECEIVED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERS_EMPTY_ROOMS_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERS_ROOMS_LIST;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantChatEnricherTest {

  @InjectMocks
  private ConsultantChatEnricher consultantChatEnricher;

  @Mock
  private SessionListAnalyser sessionListAnalyser;

  @Mock
  private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  @Test
  public void updateRequiredConsultantChatValues_Should_SetSubscribedFlagToTrue_WhenConsultantIsAttendeeOfAChat() {
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

    ConsultantSessionResponseDTO result = consultantChatEnricher
        .updateRequiredConsultantChatValues(
            singletonList(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE), RC_TOKEN,
            CONSULTANT).get(0);

    assertTrue(result.getChat().isSubscribed());
  }

  @Test
  public void updateRequiredConsultantChatValues_Should_SetSubscribedFlagToFalse_WhenConsultantIsNotAttendeeOfAChat() {
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

    ConsultantSessionResponseDTO result = consultantChatEnricher
        .updateRequiredConsultantChatValues(
            singletonList(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE), RC_TOKEN,
            CONSULTANT).get(0);

    assertFalse(result.getChat().isSubscribed());
  }

  @Test
  public void updateRequiredConsultantChatValues_Should_ReturnCorrectFileTypeAndImagePreviewForChat() {
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

    ConsultantSessionResponseDTO result = consultantChatEnricher
        .updateRequiredConsultantChatValues(
            singletonList(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE), RC_TOKEN,
            CONSULTANT).get(0);

    assertEquals(FILE_DTO.getType(), result.getChat().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        result.getChat().getAttachment().getImagePreview());

  }

  @Test
  public void updateRequiredConsultantChatValues_Should_ReturnSessionListWithChatMessagesReadFalse_WhenThereAreUnreadChatMessages() {
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

    ConsultantSessionResponseDTO result = consultantChatEnricher
        .updateRequiredConsultantChatValues(
            singletonList(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE), RC_TOKEN,
            CONSULTANT).get(0);

    assertFalse(result.getChat().isMessagesRead());
  }

  @Test
  public void updateRequiredConsultantChatValues_Should_ReturnSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadChatMessages() {
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

    ConsultantSessionResponseDTO result = consultantChatEnricher
        .updateRequiredConsultantChatValues(
            singletonList(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE), RC_TOKEN,
            CONSULTANT).get(0);

    assertTrue(result.getChat().isMessagesRead());
  }

}
