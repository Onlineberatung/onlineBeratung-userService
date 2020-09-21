package de.caritas.cob.userservice.api.helper;

import java.util.function.Consumer;

public class ThrowingConsumerWrapper {

  public static <T> Consumer<T> throwingConsumerWrapper(
      ThrowingConsumer<T, Exception> throwingConsumer) {

    return i -> {
      try {
        throwingConsumer.accept(i);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    };
  }

}
