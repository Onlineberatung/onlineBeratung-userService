package de.caritas.cob.userservice.api.actions.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class ActionsRegistryTest {

  @InjectMocks private ActionsRegistry actionsRegistry;

  @Mock private ApplicationContext applicationContext;

  @Test
  void buildContainerForType_Should_useApplicationContextForActionCommand_When_typeIsUser() {
    this.actionsRegistry.buildContainerForType(User.class);

    verify(this.applicationContext, times(1)).getBeansOfType(ActionCommand.class);
  }

  @Test
  void buildContainerForType_Should_useApplicationContextForActionCommand_When_typeIsSession() {
    this.actionsRegistry.buildContainerForType(Session.class);

    verify(this.applicationContext, times(1)).getBeansOfType(ActionCommand.class);
  }

  @Test
  void buildContainerForType_Should_executeActionOnContainer_When_actionIsAvailableInContainer() {
    ActionCommand<Session> sessionActionCommand = mock(DeactivateSessionActionCommand.class);
    when(this.applicationContext.getBeansOfType(any()))
        .thenReturn(Map.of("", sessionActionCommand));
    Session session = mock(Session.class);

    this.actionsRegistry
        .buildContainerForType(Session.class)
        .addActionToExecute(DeactivateSessionActionCommand.class)
        .executeActions(session);

    verify(sessionActionCommand, times(1)).execute(session);
  }
}
