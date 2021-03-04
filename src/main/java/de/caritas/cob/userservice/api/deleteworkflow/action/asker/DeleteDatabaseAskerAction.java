package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FOURTH;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to delete a {@link User} in database.
 */
@Component
@RequiredArgsConstructor
public class DeleteDatabaseAskerAction implements DeleteAskerAction {

  private final @NonNull UserRepository userRepository;

  /**
   * Deletes the given {@link User} in database.
   *
   * @param user the {@link User} to delete
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(User user) {
    try {
      this.userRepository.delete(user);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(user.getUserId())
              .reason("Unable to delete user")
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
    return FOURTH.getOrder();
  }

}
