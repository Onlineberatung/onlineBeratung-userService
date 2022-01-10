package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Action to delete a user account in Rocker.Chat.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class DeleteRocketChatUserAction {

  private static final String ERROR_REASON = "Unable to delete Rocket.Chat user account";

  private final @NonNull RocketChatService rocketChatService;

  protected void deleteUserInRocketChat(String rcUserId) throws RocketChatDeleteUserException {
    if (isNotBlank(rcUserId)) {
      this.rocketChatService.deleteUser(rcUserId);
    }
  }

  protected void appendErrorsForSourceType(List<DeletionWorkflowError> workflowErrors,
      DeletionSourceType deletionSourceType, String rcUserId, Exception e) {
    log.error("UserService delete workflow error: ", e);
    workflowErrors.add(
        DeletionWorkflowError.builder()
            .deletionSourceType(deletionSourceType)
            .deletionTargetType(ROCKET_CHAT)
            .identifier(rcUserId)
            .reason(ERROR_REASON)
            .timestamp(nowInUtc())
            .build()
    );
  }

}
