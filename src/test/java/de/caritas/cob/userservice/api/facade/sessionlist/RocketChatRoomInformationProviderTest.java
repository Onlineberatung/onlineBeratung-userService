package de.caritas.cob.userservice.api.facade.sessionlist;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_WITH_EMPTY_USER_VALUES;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_LAST_MESSAGE_DTO_MAP;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_ONE_FEEDBACK_UNREAD;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERS_ROOMS_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.service.RocketChatService;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatRoomInformationProviderTest {

  @InjectMocks
  private RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  @Mock
  private RocketChatService rocketChatService;

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

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(RC_CREDENTIALS_WITH_EMPTY_USER_VALUES);

    assertEquals(Objects.nonNull(rocketChatRoomInformation), true);
    assertEquals(CollectionUtils.sizeIsEmpty(rocketChatRoomInformation.getReadMessages()), true);
    assertEquals(CollectionUtils.sizeIsEmpty(rocketChatRoomInformation.getLastMessagesRoom()),
        true);
    assertEquals(CollectionUtils.sizeIsEmpty(rocketChatRoomInformation.getRoomsForUpdate()), true);
  }

}
