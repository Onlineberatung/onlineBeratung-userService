package de.caritas.cob.userservice.api.deactivateworkflow;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateSourceType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateTargetType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateWorkflowError;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.RequiredArgsConstructor;

/**
 * Abstract class for deactivate workflow actions.
 */
@RequiredArgsConstructor
public abstract class AbstractDeactivateAction<T> implements
    WorkflowAction<T, DeactivateWorkflowError> {

  protected DeactivateWorkflowError handleException(DeactivateTargetType deactivateTargetType,
      String identifier,
      String reason,
      Exception e) {
    LogService.logDeactivateWorkflowError(e);
    return DeactivateWorkflowError.builder()
        .sourceType(DeactivateSourceType.ASKER)
        .targetType(deactivateTargetType)
        .identifier(identifier)
        .reason(reason)
        .timestamp(nowInUtc())
        .build();
  }

}
