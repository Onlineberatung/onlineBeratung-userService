package de.caritas.cob.userservice.api.service.archive;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.registry.ActionContainer;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.ArchiveOrDeleteSessionStatisticsEvent;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteSingleRoomAndSessionAction;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import java.util.Optional;
import java.util.Set;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionDeleteServiceTest {

  private final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks SessionDeleteService sessionDeleteService;

  @Mock ActionsRegistry actionsRegistry;

  @Mock StatisticsService statisticsService;

  @Mock SessionService sessionService;

  @Test
  void deleteSessionAndInactiveUser_Should_ReturnOK_When_SessionIdIsKnown() {
    var sessionId = givenAPresentSession(false);
    var actionContainer = givenActionRegistryDeletesSession();

    sessionDeleteService.deleteSession(sessionId);

    verify(sessionService).getSession(sessionId);
    verify(actionContainer).executeActions(any(SessionDeletionWorkflowDTO.class));
    verify(actionsRegistry).buildContainerForType(SessionDeletionWorkflowDTO.class);
    verify(statisticsService).fireEvent(any(ArchiveOrDeleteSessionStatisticsEvent.class));
    verifyNoMoreInteractions(actionsRegistry);
  }

  @Test
  void deleteSessionAndInactiveUser_Should_DeactivateKeycloakUser_When_OnlySession()
      throws Exception {
    var sessionId = givenAPresentSession(true);
    var actionContainerDelete = givenActionRegistryDeletesSession();
    var actionContainerDeactivate = givenActionRegistryDeactivatesKeycloakUser();

    sessionDeleteService.deleteSession(sessionId);

    verify(sessionService).getSession(sessionId);
    verify(actionContainerDelete).executeActions(any(SessionDeletionWorkflowDTO.class));
    verify(actionContainerDeactivate).executeActions(any(User.class));
    verify(actionsRegistry).buildContainerForType(SessionDeletionWorkflowDTO.class);
    verify(actionsRegistry).buildContainerForType(User.class);
    verify(statisticsService).fireEvent(any(ArchiveOrDeleteSessionStatisticsEvent.class));
  }

  private long givenAPresentSession(boolean isOnlySession) {
    var sessionId = easyRandom.nextLong();
    var session = easyRandom.nextObject(Session.class);
    if (isOnlySession) {
      session.getUser().setSessions(Set.of(session));
    }

    when(sessionService.getSession(sessionId)).thenReturn(Optional.of(session));

    return sessionId;
  }

  private ActionContainer<SessionDeletionWorkflowDTO> givenActionRegistryDeletesSession() {
    @SuppressWarnings("unchecked")
    var actionContainer = (ActionContainer<SessionDeletionWorkflowDTO>) mock(ActionContainer.class);
    when(actionContainer.addActionToExecute(DeleteSingleRoomAndSessionAction.class))
        .thenReturn(actionContainer);
    when(actionsRegistry.buildContainerForType(SessionDeletionWorkflowDTO.class))
        .thenReturn(actionContainer);

    return actionContainer;
  }

  private ActionContainer<User> givenActionRegistryDeactivatesKeycloakUser() {
    @SuppressWarnings("unchecked")
    var actionContainer = (ActionContainer<User>) mock(ActionContainer.class);
    when(actionContainer.addActionToExecute(DeactivateKeycloakUserActionCommand.class))
        .thenReturn(actionContainer);
    when(actionsRegistry.buildContainerForType(User.class)).thenReturn(actionContainer);

    return actionContainer;
  }
}
