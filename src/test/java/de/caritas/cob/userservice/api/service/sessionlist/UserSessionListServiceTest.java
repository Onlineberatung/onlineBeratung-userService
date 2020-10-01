package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITHOUT_UNREADS;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_READ_MAP_WITH_UNREADS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_4;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_5;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_CHAT_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.getsessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListHelper;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionListServiceTest {

  @InjectMocks
  private UserSessionListService userSessionListService;
  @Mock
  private SessionService sessionService;
  @Mock
  private ChatService chatService;
  @Mock
  private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  @Mock
  private SessionListHelper sessionListHelper;

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithSessionMessagesReadTrue_WhenThereAreNoUnreadMessages()
      throws Exception {

    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation = RocketChatRoomInformation.builder()
        .messagesReadMap(MESSAGES_READ_MAP_WITHOUT_UNREADS).build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID)).thenReturn(true);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_2)).thenReturn(true);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_3)).thenReturn(true);

    assertTrue(
        userSessionListService.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS).get(0)
            .getSession().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadMessages()
      throws Exception {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation = RocketChatRoomInformation.builder()
        .messagesReadMap(MESSAGES_READ_MAP_WITHOUT_UNREADS).build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_4)).thenReturn(true);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_5)).thenReturn(true);

    assertTrue(userSessionListService.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS).
        get(0).getChat().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithSessionMessagesReadFalse_WhenThereAreUnreadMessages()
      throws Exception {

    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation = RocketChatRoomInformation.builder()
        .messagesReadMap(MESSAGES_READ_MAP_WITH_UNREADS).build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID)).thenReturn(false);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_2)).thenReturn(false);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_3)).thenReturn(false);

    assertFalse(
        userSessionListService.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS).get(0)
            .getSession().isMessagesRead());

  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithChatMessagesReadFalse_WhenThereAreUnreadMessages()
      throws Exception {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation = RocketChatRoomInformation.builder()
        .messagesReadMap(MESSAGES_READ_MAP_WITH_UNREADS).build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_4)).thenReturn(false);
    when(sessionListHelper
        .isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
            RC_GROUP_ID_5)).thenReturn(false);

    assertFalse(userSessionListService.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS).
        get(0).getChat().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_SetCorrectChatMessageDate()
      throws Exception {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(Collections.emptyList());
    RocketChatRoomInformation rocketChatRoomInformation = RocketChatRoomInformation.builder()
        .roomsUpdateList(ROOMS_UPDATE_DTO_LIST).build();
    when(rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS))
        .thenReturn(rocketChatRoomInformation);
    when(sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
        Mockito.any(), Mockito.any())).thenReturn(true);

    List<UserSessionResponseDTO> result =
        userSessionListService.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        result.get(0).getChat().getMessageDate());
  }


}
