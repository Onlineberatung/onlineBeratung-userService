package de.caritas.cob.userservice.api.deactivateworkflow.session;

import de.caritas.cob.userservice.api.deactivateworkflow.AbstractDeactivateAction;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateTargetType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateWorkflowError;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deactivate action for a session {@link User}.
 */
@Component
@RequiredArgsConstructor
public class DeactivateSessionAction extends AbstractDeactivateAction<Session> {

  private final @NonNull SessionService sessionService;

  /**
   * Sets a session to done and deactivates the corresponding user.
   *
   * @param session the session to change and get user from.
   * @return a generated {@link List} containing possible {@link DeactivateWorkflowError}
   */
  @Override
  public List<DeactivateWorkflowError> execute(Session session) {
    var workflowErrors = new ArrayList<DeactivateWorkflowError>();

    try {
      session.setStatus(SessionStatus.DONE);
      sessionService.saveSession(session);
    } catch (Exception e) {
      workflowErrors.add(handleException(DeactivateTargetType.DATABASE,
          Long.toString(session.getId()), "Modification of session failed", e));
    }

    return workflowErrors;
  }
}
