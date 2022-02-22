package de.caritas.cob.userservice.api.repository.session;

import java.util.Arrays;
import java.util.Optional;

public enum SessionFilter {
  ALL("all"), FEEDBACK("feedback");
  private final String value;

  SessionFilter(String value) {
    this.value = value;
  }

  public static Optional<SessionFilter> getByValue(String value) {
    return Arrays.stream(values()).filter(sessionFilter -> sessionFilter.value.equals(value))
        .findFirst();
  }
}
