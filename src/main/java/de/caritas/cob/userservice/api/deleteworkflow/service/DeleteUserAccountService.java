package de.caritas.cob.userservice.api.deleteworkflow.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deleteworkflow.action.registry.DeleteActionsRegistry;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of user accounts.
 */
@Service
@RequiredArgsConstructor
public class DeleteUserAccountService {

  private final @NonNull UserRepository userRepository;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull DeleteActionsRegistry deleteActionsRegistry;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;

  /**
   * Deletes all user accounts marked as deleted in database.
   */
  public void deleteUserAccounts() {
    List<DeletionWorkflowError> workflowErrors = deleteAskersAndCollectPossibleErrors();
    workflowErrors.addAll(deleteConsultantsAndCollectPossibleErrors());

    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }

  private List<DeletionWorkflowError> deleteAskersAndCollectPossibleErrors() {
    return this.userRepository.findAllByDeleteDateNotNull().stream()
        .map(this::performUserDeletion)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  List<DeletionWorkflowError> performUserDeletion(User user) {
    return this.deleteActionsRegistry.getAskerDeleteActions().stream()
        .map(deleteAskerAction -> deleteAskerAction.execute(user))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<DeletionWorkflowError> deleteConsultantsAndCollectPossibleErrors() {
    return this.consultantRepository.findAllByDeleteDateNotNull().stream()
        .map(this::performConsultantDeletion)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<DeletionWorkflowError> performConsultantDeletion(Consultant consultant) {
    return this.deleteActionsRegistry.getConsultantDeleteActions().stream()
        .map(deleteConsultantAction -> deleteConsultantAction.execute(consultant))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

}
