package de.caritas.cob.userservice.api.workflow.delete.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeletionWorkflowError {

  private DeletionSourceType deletionSourceType;
  private DeletionTargetType deletionTargetType;
  private String identifier;
  private String reason;
  private LocalDateTime timestamp;
}
