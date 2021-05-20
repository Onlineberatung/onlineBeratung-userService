package de.caritas.cob.userservice.api.deactivateworkflow.model;

import java.time.LocalDateTime;
import lombok.Builder;

public class DeactivateWorkflowError extends
    WorkflowError<DeactivateSourceType, DeactivateTargetType> {

  @Builder
  public DeactivateWorkflowError(
      DeactivateSourceType sourceType,
      DeactivateTargetType targetType,
      String identifier,
      String reason,
      LocalDateTime timestamp) {
    super(sourceType, targetType, identifier, reason, timestamp);
  }

  public static class DeactivateWorkflowErrorBuilder extends
      WorkflowErrorBuilder<DeactivateSourceType, DeactivateTargetType> {
    DeactivateWorkflowErrorBuilder() {
      super();
    }
  }
}
