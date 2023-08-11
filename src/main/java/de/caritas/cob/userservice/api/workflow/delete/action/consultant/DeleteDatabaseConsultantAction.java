package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Deletes a {@link Consultant} in database. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteDatabaseConsultantAction
    implements ActionCommand<ConsultantDeletionWorkflowDTO> {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull SessionRepository sessionRepository;

  /**
   * Deletes the given {@link Consultant} in database.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO} with the {@link Consultant} to
   *     delete
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      this.sessionRepository
          .findByConsultantAndStatusIn(
              actionTarget.getConsultant(),
              Lists.newArrayList(SessionStatus.NEW, SessionStatus.INITIAL))
          .stream()
          .forEach(this::unassignConsultantFromSession);
    } catch (Exception e) {
      handleExceptionWithMessage(
          actionTarget,
          e,
          "Unable to unassign consultant from his sessions with state NEW or INITIAL");
      return;
    }

    try {
      this.consultantRepository.delete(actionTarget.getConsultant());
    } catch (Exception e) {
      handleExceptionWithMessage(actionTarget, e, "Unable to delete consultant in database");
    }
  }

  private static void handleExceptionWithMessage(
      ConsultantDeletionWorkflowDTO actionTarget, Exception e, String message) {
    log.error("UserService delete workflow error: ", e);
    actionTarget
        .getDeletionWorkflowErrors()
        .add(
            DeletionWorkflowError.builder()
                .deletionSourceType(CONSULTANT)
                .deletionTargetType(DeletionTargetType.DATABASE)
                .identifier(actionTarget.getConsultant().getId())
                .reason(message)
                .timestamp(nowInUtc())
                .build());
  }

  private void unassignConsultantFromSession(Session session) {
    session.setConsultant(null);
    sessionRepository.save(session);
  }
}
