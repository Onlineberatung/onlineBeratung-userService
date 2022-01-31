package de.caritas.cob.userservice.api.service.archive;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

  void accept(T t) throws E;
}
