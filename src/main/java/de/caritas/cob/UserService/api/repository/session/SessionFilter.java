package de.caritas.cob.UserService.api.repository.session;

import java.util.Arrays;
import java.util.Optional;

public enum SessionFilter {
  ALL("all"), FEEDBACK("feedback");
  private String value;

  private SessionFilter(String value) {
    this.value = value;
  }

  public static Optional<SessionFilter> getByValue(String value) {
    return Arrays.stream(values()).filter(sessionFilter -> sessionFilter.value.equals(value))
        .findFirst();
  }
}
