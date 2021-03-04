package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FIRST;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.KEYCLOAK;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to delete a user account in keycloak.
 */
@Component
@RequiredArgsConstructor
public class DeleteKeycloakUserAction implements DeleteAskerAction, DeleteConsultantAction {

  private static final String ERROR_REASON = "Unable to delete keycloak user account";

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  /**
   * Deletes the given {@link User} in keycloak.
   *
   * @param user the {@link User} to delete
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(User user) {
    try {
      this.keycloakAdminClientService.deleteUser(user.getUserId());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(KEYCLOAK)
              .identifier(user.getUserId())
              .reason(ERROR_REASON)
              .timestamp(nowInUtc())
              .build()
      );
    }
    return emptyList();
  }

  /**
   * Deletes the given {@link Consultant} in keycloak.
   *
   * @param consultant the {@link Consultant} to delete
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(Consultant consultant) {
    try {
      this.keycloakAdminClientService.deleteUser(consultant.getId());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(KEYCLOAK)
              .identifier(consultant.getId())
              .reason(ERROR_REASON)
              .timestamp(nowInUtc())
              .build()
      );
    }
    return emptyList();
  }

  /**
   * Provides the execution order.
   *
   * @return the value for the execution order
   */
  @Override
  public int getOrder() {
    return FIRST.getOrder();
  }

}
