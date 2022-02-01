package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetMessagesStreamException;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.model.AliasMessageDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.VideoCallMessageDTO;
import de.caritas.cob.userservice.api.model.VideoCallMessageDTO.EventTypeEnum;
import de.caritas.cob.userservice.api.model.rocketchat.RocketChatUserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.service.message.MessageServiceProvider;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessagesDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.UserDTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AvailableLastMessageUpdaterTest {

  private static final String GROUP_ID = "GroupId";
  private static final String ROCKET_SYSTEM_USER_ID = "D6A2C592-FA4A-475D-9404-FA848D75E5E2";

  @InjectMocks
  private AvailableLastMessageUpdater availableLastMessageUpdater;

  @Mock
  private SessionListAnalyser sessionListAnalyser;

  @Mock
  private MessageServiceProvider messageServiceProvider;

  @Mock
  private RocketChatRoomInformation rocketChatRoomInformation;

  @Mock
  private RoomsLastMessageDTO roomsLastMessageDTO;

  @Before
  public void setup() {
    Map<String, RoomsLastMessageDTO> rooms = new HashMap<>();
    rooms.put(GROUP_ID, roomsLastMessageDTO);
    when(rocketChatRoomInformation.getLastMessagesRoom()).thenReturn(rooms);
    ReflectionTestUtils.setField(availableLastMessageUpdater, "rocketChatSystemUserId",
        ROCKET_SYSTEM_USER_ID);
  }

  @Test
  public void updateSessionWithAvailableLastMessage_should_load_additional_messages_if_last_message_is_system_alias_message()
      throws RocketChatGetMessagesStreamException {
    SessionDTO session = new SessionDTO();
    session.setGroupId(GROUP_ID);
    RoomsLastMessageDTO lastMessage = createSystemUserMessageWithAlias();
    Map<String, RoomsLastMessageDTO> rooms = new HashMap<>();
    rooms.put(GROUP_ID, lastMessage);
    when(rocketChatRoomInformation.getLastMessagesRoom()).thenReturn(rooms);
    Collection<MessagesDTO> messages = createMessageStreamWithSystemAndUserMessage(
        "encrypted message");
    when(messageServiceProvider.getMessages(RC_CREDENTIALS, GROUP_ID)).thenReturn(messages);
    when(sessionListAnalyser.prepareMessageForSessionList("encrypted message", GROUP_ID))
        .thenReturn("Hello");

    this.availableLastMessageUpdater.updateSessionWithAvailableLastMessage(
        rocketChatRoomInformation, mock(Consumer.class), session, RC_CREDENTIALS);

    assertThat(session.getLastMessage(), is("Hello"));
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_notSetVideoCallMessageDto_When_lastMessageHasNoAlias() {

    SessionDTO sessionDTO = new SessionDTO();
    sessionDTO.setGroupId(GROUP_ID);

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation,
            mock(Consumer.class), sessionDTO, RC_CREDENTIALS);

    assertThat(sessionDTO.getVideoCallMessageDTO(), nullValue());
  }

  @Test
  public void updateSessionWithAvailableLastMessage_Should_setVideoCallMessageDto_When_lastMessageHasAlias() {
    SessionDTO sessionDTO = new SessionDTO();
    sessionDTO.setGroupId(GROUP_ID);
    when(this.roomsLastMessageDTO.getAlias()).thenReturn(
        new AliasMessageDTO()
            .videoCallMessageDTO(new VideoCallMessageDTO()
                .eventType(EventTypeEnum.IGNORED_CALL)
                .initiatorUserName("initiator")
                .initiatorRcUserId("user id")));

    this.availableLastMessageUpdater
        .updateSessionWithAvailableLastMessage(this.rocketChatRoomInformation,
            mock(Consumer.class), sessionDTO, RC_CREDENTIALS);

    assertThat(sessionDTO.getVideoCallMessageDTO(), notNullValue());
    assertThat(sessionDTO.getVideoCallMessageDTO().getEventType(), is(EventTypeEnum.IGNORED_CALL));
    assertThat(sessionDTO.getVideoCallMessageDTO().getInitiatorUserName(), is("initiator"));
    assertThat(sessionDTO.getVideoCallMessageDTO().getInitiatorRcUserId(), is("user id"));
  }

  private List<MessagesDTO> createMessageStreamWithSystemAndUserMessage(String encryptedMessage) {
    MessagesDTO emptyAliasMessageBySystem = new MessagesDTO();
    emptyAliasMessageBySystem.setMsg("");
    emptyAliasMessageBySystem.setAlias(
        new de.caritas.cob.userservice.messageservice.generated.web.model.AliasMessageDTO());
    UserDTO system = new UserDTO();
    system.setId(ROCKET_SYSTEM_USER_ID);
    emptyAliasMessageBySystem.setU(system);

    MessagesDTO firstMessageByUser = new MessagesDTO();
    firstMessageByUser.setMsg(encryptedMessage);
    UserDTO user = new UserDTO();
    user.setId("userId");
    firstMessageByUser.setU(user);
    return asList(firstMessageByUser, emptyAliasMessageBySystem);
  }

  private RoomsLastMessageDTO createSystemUserMessageWithAlias() {
    RoomsLastMessageDTO lastMessage = new RoomsLastMessageDTO();
    lastMessage.setAlias(new AliasMessageDTO());
    RocketChatUserDTO rocketChatSystemUser = new RocketChatUserDTO();
    rocketChatSystemUser.setId(ROCKET_SYSTEM_USER_ID);
    lastMessage.setUser(rocketChatSystemUser);
    return lastMessage;
  }
}
