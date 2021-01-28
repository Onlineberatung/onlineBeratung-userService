package de.caritas.cob.userservice.api.service.sessionlist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.model.AliasMessageDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.VideoCallMessageDTO;
import de.caritas.cob.userservice.api.model.VideoCallMessageDTO.EventTypeEnum;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsLastMessageDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AvailableLastMessageUpdaterTest {

  private static final String GROUP_ID = "GroupId";

  @InjectMocks
  private AvailableLastMessageUpdater availableLastMessageUpdater;

  @Mock
  private SessionListAnalyser sessionListAnalyser;

  @Mock
  private RocketChatRoomInformation rocketChatRoomInformation;

  @Mock
  private RoomsLastMessageDTO roomsLastMessageDTO;

  @Before
  public void setup() {
    Map<String, RoomsLastMessageDTO> rooms = new HashMap<>();
    rooms.put(GROUP_ID, roomsLastMessageDTO);
    when(rocketChatRoomInformation.getLastMessagesRoom()).thenReturn(rooms);
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_notSetVideoCallMessageDto_When_lastMessageHasNoAlias() {

    SessionDTO sessionDTO = new SessionDTO();

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation, "",
            mock(Consumer.class), sessionDTO, GROUP_ID);

    assertThat(sessionDTO.getVideoCallMessageDTO(), nullValue());
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_setVideoCallMessageDto_When_lastMessageHasAlias() {
    SessionDTO sessionDTO = new SessionDTO();
    when(this.roomsLastMessageDTO.getAlias()).thenReturn(
        new AliasMessageDTO()
            .videoCallMessageDTO(new VideoCallMessageDTO()
                .eventType(EventTypeEnum.IGNORED_CALL)
                .initiatorUserName("initiator")
                .rcUserId("user id")));

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation, "",
            mock(Consumer.class), sessionDTO, GROUP_ID);

    assertThat(sessionDTO.getVideoCallMessageDTO(), notNullValue());
    assertThat(sessionDTO.getVideoCallMessageDTO().getEventType(), is(EventTypeEnum.IGNORED_CALL));
    assertThat(sessionDTO.getVideoCallMessageDTO().getInitiatorUserName(), is("initiator"));
    assertThat(sessionDTO.getVideoCallMessageDTO().getRcUserId(), is("user id"));
  }

}
