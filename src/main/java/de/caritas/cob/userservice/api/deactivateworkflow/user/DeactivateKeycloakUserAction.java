package de.caritas.cob.userservice.api.deactivateworkflow.user;

import de.caritas.cob.userservice.api.deactivateworkflow.AbstractDeactivateAction;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateTargetType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deactivates a user in Keycloak.
 */
@Component
@RequiredArgsConstructor
public class DeactivateKeycloakUserAction extends AbstractDeactivateAction<User> {

  /**
   * Deactivates a user in Keycloak.
   *
   * @param user the user to deactivate in Keycloak.
   * @return a generated {@link List} containing possible {@link DeactivateWorkflowError}
   */
  @Override
  public List<DeactivateWorkflowError> execute(User user) {
    var workflowErrors = new ArrayList<DeactivateWorkflowError>();

    try {
      LogService.logDebug("Nothing to do in DeactivateKeycloakUserAction");
    } catch (Exception e) {
      workflowErrors.add(super.handleException(DeactivateTargetType.KEYCLOAK,
          user.getUserId(), "Deactivation of user in Keycloak failed", e));
    }

    return workflowErrors;
  }
}
