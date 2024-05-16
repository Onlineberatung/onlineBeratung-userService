package de.caritas.cob.userservice.api.service.sessionlist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LastMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.MessageType;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO.EventTypeEnum;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AvailableLastMessageUpdaterTest {

  private static final String GROUP_ID = "GroupId";

  @InjectMocks private AvailableLastMessageUpdater availableLastMessageUpdater;

  @Mock private SessionListAnalyser sessionListAnalyser;

  @Mock private RocketChatRoomInformation rocketChatRoomInformation;

  @Mock private RoomsLastMessageDTO roomsLastMessageDTO;
  private SessionDTO session;

  @BeforeEach
  public void setup() {
    Map<String, RoomsLastMessageDTO> rooms = new HashMap<>();
    rooms.put(GROUP_ID, roomsLastMessageDTO);
    when(rocketChatRoomInformation.getLastMessagesRoom()).thenReturn(rooms);
    session = new SessionDTO();
    session.setGroupId(GROUP_ID);
  }

  @Test
  public void
      updateSessionWithAvailableLastMessage_Should_notSetVideoCallMessageDto_When_lastMessageHasNoAlias() {

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        session, mock(Consumer.class), this.rocketChatRoomInformation, "");

    assertThat(session.getVideoCallMessageDTO(), nullValue());
  }

  @Test
  public void
      updateSessionWithAvailableLastMessage_Should_setVideoCallMessageDto_When_lastMessageHasAlias() {
    when(this.roomsLastMessageDTO.getAlias())
        .thenReturn(
            new AliasMessageDTO()
                .videoCallMessageDTO(
                    new VideoCallMessageDTO()
                        .eventType(EventTypeEnum.IGNORED_CALL)
                        .initiatorUserName("initiator")
                        .initiatorRcUserId("user id")));

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        session, mock(Consumer.class), this.rocketChatRoomInformation, "");

    assertThat(session.getVideoCallMessageDTO(), notNullValue());
    assertThat(session.getVideoCallMessageDTO().getEventType(), is(EventTypeEnum.IGNORED_CALL));
    assertThat(session.getVideoCallMessageDTO().getInitiatorUserName(), is("initiator"));
    assertThat(session.getVideoCallMessageDTO().getInitiatorRcUserId(), is("user id"));
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_useAnalyser_When_lasMessageIsPresent() {
    when(roomsLastMessageDTO.getMessage()).thenReturn("message");

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        session, mock(Consumer.class), this.rocketChatRoomInformation, "");

    verify(sessionListAnalyser).prepareMessageForSessionList("message", GROUP_ID);
  }

  @Test
  public void
      updateSessionWithAvailableLastMessage_Should_setFurtherStepsMessage_When_lasMessageHasFurtherStepsAlias() {
    when(roomsLastMessageDTO.getAlias())
        .thenReturn(new AliasMessageDTO().messageType(MessageType.FURTHER_STEPS));

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        session, mock(Consumer.class), this.rocketChatRoomInformation, "");

    var expectedLastMessage = new LastMessageDTO();
    expectedLastMessage.setMsg("So geht es weiter");
    assertThat(session.getE2eLastMessage(), is(expectedLastMessage));
    assertThat(session.getLastMessage(), is(expectedLastMessage.getMsg()));
  }

  @Test
  public void updateSessionWithAvailableLastMessage_should_set_rocket_chat_type() {
    givenAnE2eRoomsLastMessage();
    when(sessionListAnalyser.prepareMessageForSessionList("e2e_encrypted_message", GROUP_ID))
        .thenReturn("e2e_encrypted_message");

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        session, mock(Consumer.class), this.rocketChatRoomInformation, "rc4711");

    var expectedLastMessage = new LastMessageDTO();
    expectedLastMessage.setMsg("e2e_encrypted_message");
    expectedLastMessage.setT("e2e");
    assertThat(session.getE2eLastMessage(), is(expectedLastMessage));
    assertThat(session.getLastMessage(), is(expectedLastMessage.getMsg()));
  }

  @Test
  public void
      updateSessionWithAvailableLastMessage_should_set_message_type_for_messages_without_last_message() {
    when(rocketChatRoomInformation.getLastMessagesRoom()).thenReturn(Collections.emptyMap());

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        session, mock(Consumer.class), this.rocketChatRoomInformation, "rc4711");

    var expectedLastMessage = new LastMessageDTO();
    expectedLastMessage.setMsg("So geht es weiter");
    assertThat(session.getE2eLastMessage(), is(expectedLastMessage));
    assertThat(session.getLastMessage(), is("So geht es weiter"));
    assertThat(session.getLastMessageType(), is(MessageType.FURTHER_STEPS));
  }

  @Test
  public void updateChatWithAvailableLastMessage_should_set_rocket_chat_type() {
    var chat = new UserChatDTO();
    chat.setGroupId(GROUP_ID);
    AtomicReference<Date> date = new AtomicReference<>();
    givenAnE2eRoomsLastMessage();
    when(sessionListAnalyser.prepareMessageForSessionList("e2e_encrypted_message", GROUP_ID))
        .thenReturn("e2e_encrypted_message");

    this.availableLastMessageUpdater.updateChatWithAvailableLastMessage(
        chat, date::set, this.rocketChatRoomInformation, "rc4711");

    var expectedLastMessage = new LastMessageDTO();
    expectedLastMessage.setMsg("e2e_encrypted_message");
    expectedLastMessage.setT("e2e");
    assertThat(chat.getE2eLastMessage(), is(expectedLastMessage));
    assertThat(chat.getLastMessage(), is(expectedLastMessage.getMsg()));
    assertThat(chat.getMessageDate(), is(1655730882L));
    assertThat(date.get().getTime(), is(1655730882738L));
  }

  @Test
  public void
      updateChatWithAvailableLastMessage_should_set_fallback_date_if_rc_last_message_is_unavailable() {
    var chat = new UserChatDTO();
    var startDateWithTime = LocalDateTime.of(2022, 5, 22, 13, 37);
    chat.setStartDateWithTime(startDateWithTime);
    AtomicReference<Date> date = new AtomicReference<>();

    this.availableLastMessageUpdater.updateChatWithAvailableLastMessage(
        chat, date::set, this.rocketChatRoomInformation, "rc4711");

    assertThat(chat.getE2eLastMessage(), is(nullValue()));
    assertThat(chat.getLastMessage(), is(nullValue()));
    assertThat(date.get(), is(Timestamp.valueOf(startDateWithTime)));
  }

  private void givenAnE2eRoomsLastMessage() {
    when(roomsLastMessageDTO.getTimestamp()).thenReturn(new Date(1655730882738L));
    when(roomsLastMessageDTO.getMessage()).thenReturn("e2e_encrypted_message");
    when(roomsLastMessageDTO.getType()).thenReturn("e2e");
  }
}
