package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ROCKETCHAT_SESSION;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ALL;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatMongoDbService;
import de.caritas.cob.userservice.api.adapters.rocketchat.model.RocketchatSession;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service to trigger deletion of inactive sessions and asker accounts. */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloseInactiveRocketchatSessionsService {

  private final @NonNull RocketChatMongoDbService rocketChatMongoDbService;

  private final @NonNull WorkflowErrorMailService workflowErrorMailService;

  public void closeInactiveRocketchatSessions() {

    List<DeletionWorkflowError> workflowErrors = new ArrayList<>();
    try {
      List<RocketchatSession> inactiveSessions = rocketChatMongoDbService.findInactiveSessions();
      if (isNotEmpty(inactiveSessions)) {
        log.info("Found {} inactive rocketchat sessions.", inactiveSessions.size());
      } else {
        log.info("No inactive rocketchat sessions found. Skipping workflow execution.");
      }
      inactiveSessions.stream().forEach(this::closeSession);
    } catch (Exception e) {
      log.error("Error while deleting inactive sessions: {}", e.getMessage());
      workflowErrors.add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ROCKETCHAT_SESSION)
              .deletionTargetType(ALL)
              .reason("Error deleting rocketchat inactive sessions.")
              .timestamp(nowInUtc())
              .build());
    }
    sendWorkflowErrorsMail(workflowErrors);
  }

  private void closeSession(RocketchatSession rocketchatSession) {
    log.debug(
        "Closing rocketchat session with id: {} for rcUserId: {}",
        rocketchatSession.getSessionId());
    rocketchatSession.setClosedAt(Instant.now());
    rocketChatMongoDbService.patchClosedAt(rocketchatSession);
  }

  private void sendWorkflowErrorsMail(List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }
}
