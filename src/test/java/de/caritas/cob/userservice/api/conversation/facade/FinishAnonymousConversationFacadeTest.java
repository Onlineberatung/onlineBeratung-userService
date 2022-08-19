package de.caritas.cob.userservice.api.conversation.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.ActionCommandMockProvider;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.actions.session.PostConversationFinishedAliasMessageActionCommand;
import de.caritas.cob.userservice.api.actions.session.SendFinishedAnonymousConversationEventActionCommand;
import de.caritas.cob.userservice.api.actions.session.SetRocketChatRoomReadOnlyActionCommand;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinishAnonymousConversationFacadeTest {

  @InjectMocks private FinishAnonymousConversationFacade finishAnonymousConversationFacade;

  @Mock private SessionService sessionService;

  @Mock private ActionsRegistry actionsRegistry;

  private final ActionCommandMockProvider actionCommandMockProvider =
      new ActionCommandMockProvider();

  @Test
  void finishConversation_Should_throwNotFoundException_When_sessionDoesNotExist() {
    when(this.sessionService.getSession(any())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> this.finishAnonymousConversationFacade.finishConversation(1L));
  }

  @Test
  void finishConversation_Should_triggerExpectedActions_When_sessionExists() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionService.getSession(any())).thenReturn(Optional.of(session));
    when(this.actionsRegistry.buildContainerForType(Session.class))
        .thenReturn(this.actionCommandMockProvider.getActionContainer(Session.class));
    when(this.actionsRegistry.buildContainerForType(User.class))
        .thenReturn(this.actionCommandMockProvider.getActionContainer(User.class));

    this.finishAnonymousConversationFacade.finishConversation(session.getId());

    verify(
            this.actionCommandMockProvider.getActionMock(
                SendFinishedAnonymousConversationEventActionCommand.class),
            times(1))
        .execute(session);
    verify(
            this.actionCommandMockProvider.getActionMock(DeactivateSessionActionCommand.class),
            times(1))
        .execute(session);
    verify(
            this.actionCommandMockProvider.getActionMock(
                SetRocketChatRoomReadOnlyActionCommand.class),
            times(1))
        .execute(session);
    verify(
            this.actionCommandMockProvider.getActionMock(DeactivateKeycloakUserActionCommand.class),
            times(1))
        .execute(session.getUser());
    verify(
            this.actionCommandMockProvider.getActionMock(
                PostConversationFinishedAliasMessageActionCommand.class),
            times(1))
        .execute(session);
  }
}
