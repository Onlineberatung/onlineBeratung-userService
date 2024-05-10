package de.caritas.cob.userservice.api.workflow.delete.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service class to log all deletion workflow errors. */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowErrorLogService {

  public void logWorkflowErrors(List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      workflowErrors.forEach(
          workflowError ->
              log.warn(
                  "Errors during deletion workflow:"
                      + " SourceType = {}; "
                      + "TargetType = {}; "
                      + "Identifier = {}; "
                      + "Reason = {}; "
                      + "Timestamp = {}.",
                  workflowError.getDeletionSourceType(),
                  workflowError.getDeletionTargetType(),
                  workflowError.getIdentifier(),
                  workflowError.getReason(),
                  workflowError.getTimestamp()));
    }
  }
}
