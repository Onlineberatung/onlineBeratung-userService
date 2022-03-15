package de.caritas.cob.userservice.api.actions.session;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class SetRocketChatRoomReadOnlyActionCommandTest {

  @InjectMocks
  private SetRocketChatRoomReadOnlyActionCommand actionCommand;

  @Mock
  private RocketChatService rocketChatService;

  private static final Logger LOGGER = mock(Logger.class);

  @BeforeAll
  public static void setup() {
    setInternalState(SetRocketChatRoomReadOnlyActionCommand.class, "log", LOGGER);
  }

  @ParameterizedTest
  @MethodSource("sessionsWithoutInteractionsExpected")
  void execute_Should_doNothing_When_sessionIsNullOrWithoutRcRooms(Session session) {
    this.actionCommand.execute(session);

    verifyNoMoreInteractions(this.rocketChatService);
  }

  private static List<Session> sessionsWithoutInteractionsExpected() {
    return asList(null, new Session());
  }

  @Test
  void execute_Should_useRocketChatServiceTwice_When_sessionHasGroupIdAndFeedbackGroupId()
      throws Exception {
    Session session = new Session();
    session.setGroupId("group id");
    session.setFeedbackGroupId("feedback group id");

    this.actionCommand.execute(session);

    verify(this.rocketChatService, times(1)).setRoomReadOnly("group id");
    verify(this.rocketChatService, times(1)).setRoomReadOnly("feedback group id");
  }

  @Test
  void execute_Should_logError_When_rocketChatCallFails() throws Exception {
    Session session = new Session();
    session.setGroupId("group id");
    doThrow(new RuntimeException("")).when(this.rocketChatService).setRoomReadOnly(anyString());

    this.actionCommand.execute(session);

    verify(LOGGER).error(anyString(), anyString());
  }

}
