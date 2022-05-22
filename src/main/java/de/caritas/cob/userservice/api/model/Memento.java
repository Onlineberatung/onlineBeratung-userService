package de.caritas.cob.userservice.api.model;

import java.util.Optional;
import java.util.function.Supplier;

public class Memento<T> {

  private T value;
  private final Supplier<T> loadAbsent;

  public Memento(Supplier<T> loadAbsent) {
    this.loadAbsent = loadAbsent;
  }

  public Optional<T> getValue() {
    if (value != null) {
      return Optional.of(value);
    }
    value = loadAbsent.get();
    return Optional.ofNullable(value);
  }
}
