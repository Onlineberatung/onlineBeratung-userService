package de.caritas.cob.userservice.api.workflow.delete.service;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.ActionCommandMockProvider;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteSingleRoomAndSessionAction;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteSessionServiceTest {

  @InjectMocks private DeleteSessionService deleteSessionService;

  @Mock private ActionsRegistry actionsRegistry;

  private final ActionCommandMockProvider commandMockProvider = new ActionCommandMockProvider();

  @Test
  public void performSessionDeletion_Should_notPerformAnyDeletion_When_SessionIsNull() {

    var result = deleteSessionService.performSessionDeletion(null);

    assertThat(result.isEmpty(), is(true));
    verifyNoMoreInteractions(actionsRegistry);
  }

  @Test
  public void performSessionDeletion_Should_performSessionDeletion_When_SessionIsNotNull() {
    Session session = new Session();
    when(this.actionsRegistry.buildContainerForType(SessionDeletionWorkflowDTO.class))
        .thenReturn(this.commandMockProvider.getActionContainer(SessionDeletionWorkflowDTO.class));
    this.deleteSessionService.performSessionDeletion(session);

    verify(this.actionsRegistry, times(1)).buildContainerForType(SessionDeletionWorkflowDTO.class);
    verify(this.commandMockProvider.getActionMock(DeleteSingleRoomAndSessionAction.class), times(1))
        .execute(new SessionDeletionWorkflowDTO(session, emptyList()));
  }
}
