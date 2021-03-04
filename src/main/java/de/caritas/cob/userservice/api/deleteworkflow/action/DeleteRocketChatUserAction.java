package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FIFTH;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to delete a user account in Rocker.Chat.
 */
@Component
@RequiredArgsConstructor
public class DeleteRocketChatUserAction implements DeleteAskerAction, DeleteConsultantAction {

  private static final String ERROR_REASON = "Unable to delete Rocket.Chat user account";

  private final @NonNull RocketChatService rocketChatService;

  /**
   * Deletes the given {@link User} related Rocket.Chat account.
   *
   * @param user the {@link User}
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(User user) {
    try {
      this.rocketChatService.deleteUser(user.getRcUserId());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
        DeletionWorkflowError.builder()
            .deletionSourceType(ASKER)
            .deletionTargetType(ROCKET_CHAT)
            .identifier(user.getRcUserId())
            .reason(ERROR_REASON)
            .timestamp(nowInUtc())
            .build()
      );
    }
    return emptyList();
  }

  /**
   * Deletes the given {@link Consultant} related Rocket.Chat account.
   *
   * @param consultant the {@link Consultant}
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(Consultant consultant) {
    try {
      this.rocketChatService.deleteUser(consultant.getRocketChatId());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(ROCKET_CHAT)
              .identifier(consultant.getRocketChatId())
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
    return FIFTH.getOrder();
  }
}
