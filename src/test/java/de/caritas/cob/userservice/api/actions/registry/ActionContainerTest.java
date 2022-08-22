package de.caritas.cob.userservice.api.actions.registry;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.actions.session.SetRocketChatRoomReadOnlyActionCommand;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionContainerTest {

  @Test
  void addActionToExecute_Should_throwNoSuchElementException_When_actionClassDoesNotExist() {
    Set<ActionCommand<Session>> sessionActionCommand =
        Set.of(new DeactivateSessionActionCommand(mock(SessionService.class)));
    ActionContainer<Session> actionContainer = new ActionContainer<>(sessionActionCommand);

    assertThrows(
        NoSuchElementException.class,
        () -> actionContainer.addActionToExecute(SetRocketChatRoomReadOnlyActionCommand.class));
  }

  @Test
  void addActionToExecute_Should_doNothing_When_noActionToExecuteIsInitialized() {
    Set<ActionCommand<Session>> sessionActionCommand =
        Set.of(new DeactivateSessionActionCommand(mock(SessionService.class)));
    Session session = mock(Session.class);

    new ActionContainer<>(sessionActionCommand).executeActions(session);

    verifyNoInteractions(session);
  }

  @Test
  void addActionToExecute_Should_executeAction_When_actionToExecuteIsInitialized() {
    ActionCommand<Session> sessionActionCommand = mock(DeactivateSessionActionCommand.class);
    Session session = mock(Session.class);

    new ActionContainer<>(Set.of(sessionActionCommand))
        .addActionToExecute(DeactivateSessionActionCommand.class)
        .executeActions(session);

    verify(sessionActionCommand, times(1)).execute(session);
  }
}
