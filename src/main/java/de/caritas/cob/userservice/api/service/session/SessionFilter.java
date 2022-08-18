package de.caritas.cob.userservice.api.service.session;

import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SessionFilter {
  ALL("all"),
  FEEDBACK("feedback");

  private final String value;

  public static Optional<SessionFilter> getByValue(String value) {
    return Arrays.stream(values())
        .filter(sessionFilter -> sessionFilter.value.equals(value))
        .findFirst();
  }
}
