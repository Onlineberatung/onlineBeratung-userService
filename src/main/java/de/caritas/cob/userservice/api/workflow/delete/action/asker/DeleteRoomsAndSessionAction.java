package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionDataRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
abstract class DeleteRoomsAndSessionAction {

  protected final @NonNull SessionRepository sessionRepository;
  protected final @NonNull SessionDataRepository sessionDataRepository;
  protected final @NonNull RocketChatService rocketChatService;

  void deleteRocketChatGroup(String rcGroupId, List<DeletionWorkflowError> workflowErrors) {
    if (isNotBlank(rcGroupId)) {
      try {
        this.rocketChatService.deleteGroupAsTechnicalUser(rcGroupId);
      } catch (RocketChatDeleteGroupException e) {
        log.error("UserService delete workflow error: ", e);
        workflowErrors.add(
            DeletionWorkflowError.builder()
                .deletionSourceType(ASKER)
                .deletionTargetType(DeletionTargetType.ROCKET_CHAT)
                .identifier(rcGroupId)
                .reason("Deletion of Rocket.Chat group failed")
                .timestamp(nowInUtc())
                .build());
      }
    }
  }

  void deleteSessionData(Session session, List<DeletionWorkflowError> workflowErrors) {
    try {
      var sessionData = this.sessionDataRepository.findBySessionId(session.getId());
      this.sessionDataRepository.deleteAll(sessionData);
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      workflowErrors.add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DeletionTargetType.DATABASE)
              .identifier(String.valueOf(session.getId()))
              .reason("Unable to delete session data from session")
              .timestamp(nowInUtc())
              .build());
    }
  }

  protected void deleteSession(Session session, List<DeletionWorkflowError> workflowErrors) {
    try {
      this.sessionRepository.delete(session);
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      workflowErrors.add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DeletionTargetType.DATABASE)
              .identifier(String.valueOf(session.getId()))
              .reason("Unable to delete session")
              .timestamp(nowInUtc())
              .build());
    }
  }

  void performSessionDeletion(Session session, List<DeletionWorkflowError> workflowErrors) {

    deleteRocketChatGroup(session.getGroupId(), workflowErrors);
    deleteRocketChatGroup(session.getFeedbackGroupId(), workflowErrors);
    deleteSessionData(session, workflowErrors);
    deleteSession(session, workflowErrors);
  }
}
