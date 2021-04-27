package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FOURTH;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
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
      deleteUserInRocketChat(user.getRcUserId());
    } catch (Exception e) {
      return buildErrorsForSourceType(ASKER, user.getRcUserId(), e);
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
      deleteUserInRocketChat(consultant.getRocketChatId());
    } catch (Exception e) {
      return buildErrorsForSourceType(CONSULTANT, consultant.getRocketChatId(), e);
    }
    return emptyList();
  }

  private void deleteUserInRocketChat(String rcUserId) throws RocketChatDeleteUserException {
    if (isNotBlank(rcUserId)) {
      this.rocketChatService.deleteUser(rcUserId);
    }
  }

  private List<DeletionWorkflowError> buildErrorsForSourceType(
      DeletionSourceType deletionSourceType, String rcUserId, Exception e) {
    LogService.logDeleteWorkflowError(e);
    return singletonList(
        DeletionWorkflowError.builder()
            .deletionSourceType(deletionSourceType)
            .deletionTargetType(ROCKET_CHAT)
            .identifier(rcUserId)
            .reason(ERROR_REASON)
            .timestamp(nowInUtc())
            .build()
    );
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
