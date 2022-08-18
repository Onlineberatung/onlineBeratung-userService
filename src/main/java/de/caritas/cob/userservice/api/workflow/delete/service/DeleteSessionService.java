package de.caritas.cob.userservice.api.workflow.delete.service;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteSingleRoomAndSessionAction;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service to trigger deletion of user sessions. */
@Service
@RequiredArgsConstructor
public class DeleteSessionService {

  private final @NonNull ActionsRegistry actionsRegistry;

  /**
   * Deletes the given session with the related Rocket.Chat room.
   *
   * @param session the {@link Session} to delete
   * @return a {@link List} of {@link DeletionWorkflowError}
   */
  public List<DeletionWorkflowError> performSessionDeletion(Session session) {

    if (isNull(session)) {
      return Collections.emptyList();
    }

    var deletionWorkflowDTO = new SessionDeletionWorkflowDTO(session, new ArrayList<>());

    this.actionsRegistry
        .buildContainerForType(SessionDeletionWorkflowDTO.class)
        .addActionToExecute(DeleteSingleRoomAndSessionAction.class)
        .executeActions(deletionWorkflowDTO);

    return deletionWorkflowDTO.getDeletionWorkflowErrors();
  }
}
