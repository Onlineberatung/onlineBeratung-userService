package de.caritas.cob.userservice.api.model;

import java.util.Optional;
import java.util.function.Supplier;

public class Memento<T> implements Supplier<Optional<T>> {

  private final Supplier<T> loadAbsent;

  private T value;

  public Memento(Supplier<T> loadAbsent) {
    this.loadAbsent = loadAbsent;
  }

  @Override
  public Optional<T> get() {
    if (value != null) {
      return Optional.of(value);
    }
    value = loadAbsent.get();
    return Optional.ofNullable(value);
  }
}
