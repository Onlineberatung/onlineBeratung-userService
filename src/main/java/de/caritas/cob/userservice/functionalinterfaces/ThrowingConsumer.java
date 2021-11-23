package de.caritas.cob.userservice.functionalinterfaces;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

  void accept(T t) throws E;
}
