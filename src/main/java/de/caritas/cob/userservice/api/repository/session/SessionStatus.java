package de.caritas.cob.userservice.api.repository.session;

import java.util.Arrays;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Session status
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum SessionStatus {
  INITIAL(0), NEW(1), IN_PROGRESS(2);
  private final int value;

  public static Optional<SessionStatus> valueOf(int value) {
    return Arrays.stream(values()).filter(legNo -> legNo.value == value).findFirst();
  }
}
