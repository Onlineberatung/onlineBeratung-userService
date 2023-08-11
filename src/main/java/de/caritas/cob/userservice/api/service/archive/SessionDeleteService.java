package de.caritas.cob.userservice.api.service.archive;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.ArchiveOrDeleteSessionStatisticsEvent;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteSingleRoomAndSessionAction;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionDeleteService {

  private final @NotNull ActionsRegistry actionsRegistry;

  private final @NotNull SessionService sessionService;

  private final @NonNull StatisticsService statisticsService;

  public void deleteSession(Long sessionId) {
    log.info("Deleting session with id {}", sessionId);

    var session =
        sessionService
            .getSession(sessionId)
            .orElseThrow(
                () -> new NotFoundException("A session with an id %s does not exist.", sessionId));

    var user = session.getUser();
    if (user.getSessions().size() == 1) {
      actionsRegistry
          .buildContainerForType(User.class)
          .addActionToExecute(DeactivateKeycloakUserActionCommand.class)
          .executeActions(user);
    }

    var deleteSession = new SessionDeletionWorkflowDTO(session, null);
    actionsRegistry
        .buildContainerForType(SessionDeletionWorkflowDTO.class)
        .addActionToExecute(DeleteSingleRoomAndSessionAction.class)
        .executeActions(deleteSession);

    fireArchiveOrDeleteSessionEvent(session);
    log.info("Session with id {} deleted", sessionId);
  }

  private void fireArchiveOrDeleteSessionEvent(Session session) {
    try {
      ArchiveOrDeleteSessionStatisticsEvent archiveOrDeleteSessionStatisticsEvent =
          new ArchiveOrDeleteSessionStatisticsEvent(
              session.getUser(), session.getId(), LocalDateTime.now());
      statisticsService.fireEvent(archiveOrDeleteSessionStatisticsEvent);
    } catch (Exception e) {
      log.error("Could not create session archive statistics event", e);
    }
  }
}
