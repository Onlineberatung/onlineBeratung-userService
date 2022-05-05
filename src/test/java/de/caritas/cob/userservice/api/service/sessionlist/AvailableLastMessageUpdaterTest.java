package de.caritas.cob.userservice.api.service.sessionlist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.MessageType;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO.EventTypeEnum;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
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
    var sessionDTO = new SessionDTO();

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation, "",
            mock(Consumer.class), sessionDTO, GROUP_ID);

    assertThat(sessionDTO.getVideoCallMessageDTO(), nullValue());
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_setVideoCallMessageDto_When_lastMessageHasAlias() {
    var sessionDTO = new SessionDTO();
    when(this.roomsLastMessageDTO.getAlias()).thenReturn(
        new AliasMessageDTO()
            .videoCallMessageDTO(new VideoCallMessageDTO()
                .eventType(EventTypeEnum.IGNORED_CALL)
                .initiatorUserName("initiator")
                .initiatorRcUserId("user id")));

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation, "",
            mock(Consumer.class), sessionDTO, GROUP_ID);

    assertThat(sessionDTO.getVideoCallMessageDTO(), notNullValue());
    assertThat(sessionDTO.getVideoCallMessageDTO().getEventType(), is(EventTypeEnum.IGNORED_CALL));
    assertThat(sessionDTO.getVideoCallMessageDTO().getInitiatorUserName(), is("initiator"));
    assertThat(sessionDTO.getVideoCallMessageDTO().getInitiatorRcUserId(), is("user id"));
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_useAnalyser_When_lasMessageIsPresent() {
    when(roomsLastMessageDTO.getMessage()).thenReturn("message");

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation, "",
            mock(Consumer.class), new SessionDTO(), GROUP_ID);

    verify(sessionListAnalyser).prepareMessageForSessionList("message", GROUP_ID);
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_setFurtherStepsMessage_When_lasMessageHasFurtherStepsAlias() {
    var session = new SessionDTO();
    when(roomsLastMessageDTO.getAlias())
        .thenReturn(new AliasMessageDTO().messageType(MessageType.FURTHER_STEPS));

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation, "",
            mock(Consumer.class), session, GROUP_ID);

    assertThat(session.getLastMessage(), is("So geht es weiter"));
  }

}
