package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FIFTH;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ANONYMOUS_REGISTRY_IDS;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUsernameRegistry;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link UserAgency} in database.
 */
@Component
@RequiredArgsConstructor
public class DeleteAnonymousRegistryIdAction implements DeleteAskerAction {

  private final @NonNull AnonymousUsernameRegistry anonymousUsernameRegistry;

  /**
   * Deletes a registry id by username.
   *
   * @param userAccount the {@link User}
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(User userAccount) {
    try {
      anonymousUsernameRegistry.removeRegistryIdByUsername(userAccount.getUsername());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(ANONYMOUS_REGISTRY_IDS)
              .identifier(userAccount.getUserId())
              .reason("Could not delete registry id for anonymous users by username")
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
    return FIFTH.getOrder();
  }
}
