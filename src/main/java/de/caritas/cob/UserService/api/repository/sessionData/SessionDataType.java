package de.caritas.cob.UserService.api.repository.sessionData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Session data types
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum SessionDataType {
  REGISTRATION(0);
  private final int value;
}
