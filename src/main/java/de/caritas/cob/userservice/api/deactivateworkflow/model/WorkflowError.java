package de.caritas.cob.userservice.api.deactivateworkflow.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Generic class to collect workflow errors.
 * @param <S> defines the error's source type
 * @param <T> defines the error's target type
 */
@Data
@Builder
public class WorkflowError<S, T> {

  protected S sourceType;
  protected T targetType;
  protected String identifier;
  protected String reason;
  protected LocalDateTime timestamp;

}
