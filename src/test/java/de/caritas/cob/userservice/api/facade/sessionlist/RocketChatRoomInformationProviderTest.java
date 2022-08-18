package de.caritas.cob.userservice.api.facade.sessionlist;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_WITH_EMPTY_USER_VALUES;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_3;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_3;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_ONE_FEEDBACK_UNREAD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERS_ROOMS_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatRoomInformationProviderTest {

  @InjectMocks private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  @Mock private RocketChatService rocketChatService;

  @Test
  public void retrieveRocketChatInformation_Should_Return_CorrectMessagesReadMap() {

    when(rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_ONE_FEEDBACK_UNREAD);
    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS);

    assertTrue(rocketChatRoomInformation.getReadMessages().get(RC_GROUP_ID));
    assertTrue(rocketChatRoomInformation.getReadMessages().get(RC_GROUP_ID_2));
    assertTrue(rocketChatRoomInformation.getReadMessages().get(RC_GROUP_ID_3));
    assertFalse(rocketChatRoomInformation.getReadMessages().get(RC_FEEDBACK_GROUP_ID));
    assertTrue(rocketChatRoomInformation.getReadMessages().get(RC_FEEDBACK_GROUP_ID_2));
    assertTrue(rocketChatRoomInformation.getReadMessages().get(RC_FEEDBACK_GROUP_ID_3));
  }

  @Test
  public void retrieveRocketChatInformation_Should_Return_RocketChatRoomsUpdateList() {

    when(rocketChatService.getRoomsOfUser(RC_CREDENTIALS)).thenReturn(ROOMS_UPDATE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS);
    assertEquals(ROOMS_UPDATE_DTO_LIST, rocketChatRoomInformation.getRoomsForUpdate());
  }

  @Test
  public void retrieveRocketChatInformation_Should_Return_CorrectRocketChatUserRoomList() {

    when(rocketChatService.getRoomsOfUser(RC_CREDENTIALS)).thenReturn(ROOMS_UPDATE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS);
    assertEquals(USERS_ROOMS_LIST, rocketChatRoomInformation.getUserRooms());
  }

  @Test
  public void retrieveRocketChatInformation_Should_Return_CorrectRocketChatLastMessageRoom() {

    when(rocketChatService.getRoomsOfUser(RC_CREDENTIALS)).thenReturn(ROOMS_UPDATE_DTO_LIST);
    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS);
    assertEquals(ROOMS_LAST_MESSAGE_DTO_MAP, rocketChatRoomInformation.getLastMessagesRoom());
  }

  @Test
  public void retrieveRocketChatInformation_Should_Return_EmptyObject_When_RcUserIdNotSet() {

    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(
            RC_CREDENTIALS_WITH_EMPTY_USER_VALUES);

    assertTrue(Objects.nonNull(rocketChatRoomInformation));
    assertTrue(CollectionUtils.sizeIsEmpty(rocketChatRoomInformation.getReadMessages()));
    assertTrue(CollectionUtils.sizeIsEmpty(rocketChatRoomInformation.getLastMessagesRoom()));
    assertTrue(CollectionUtils.sizeIsEmpty(rocketChatRoomInformation.getRoomsForUpdate()));
  }

  @Test
  public void should_collect_fallback_date_for_rooms_without_last_message() {
    var fallbackDate = new Date(1655730882738L);
    var roomUpdateWithoutLastMessage =
        new RoomsUpdateDTO(
            "4711aaGc",
            "room without last message",
            "fname13",
            "e2e",
            USER_DTO_3,
            true,
            false,
            new Date(),
            null,
            fallbackDate);
    var rooms = new ArrayList<>(ROOMS_UPDATE_DTO_LIST);
    rooms.add(roomUpdateWithoutLastMessage);
    when(rocketChatService.getRoomsOfUser(RC_CREDENTIALS)).thenReturn(rooms);

    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS);

    assertNotNull(rocketChatRoomInformation.getGroupIdToLastMessageFallbackDate());
    var fallbackDateOfRoom =
        rocketChatRoomInformation.getGroupIdToLastMessageFallbackDate().get("4711aaGc");
    assertEquals(fallbackDate, fallbackDateOfRoom);
  }

  @Test
  public void should_not_fail_on_rooms_without_fallback_date() {
    var roomUpdateWithoutLastMessage =
        new RoomsUpdateDTO(
            "4711aaGc",
            "room without last message and date",
            "fname13",
            "e2e",
            USER_DTO_3,
            true,
            false,
            new Date(),
            null,
            null);
    var rooms = new ArrayList<>(ROOMS_UPDATE_DTO_LIST);
    rooms.add(roomUpdateWithoutLastMessage);
    when(rocketChatService.getRoomsOfUser(RC_CREDENTIALS)).thenReturn(rooms);

    RocketChatRoomInformation rocketChatRoomInformation =
        rocketChatRoomInformationProvider.retrieveRocketChatInformation(RC_CREDENTIALS);

    assertNotNull(rocketChatRoomInformation.getGroupIdToLastMessageFallbackDate());
    assertTrue(rocketChatRoomInformation.getGroupIdToLastMessageFallbackDate().isEmpty());
  }
}
