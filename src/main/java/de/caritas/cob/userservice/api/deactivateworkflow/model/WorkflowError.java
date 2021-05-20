package de.caritas.cob.userservice.api.deactivateworkflow.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Generic class to collect workflow errors.
 * @param <SourceT> defines the error's source type
 * @param <TargetT> defines the error's target type
 */
@Data
@Builder
public class WorkflowError<SourceT, TargetT> {

  protected SourceT sourceType;
  protected TargetT targetType;
  protected String identifier;
  protected String reason;
  protected LocalDateTime timestamp;

}
