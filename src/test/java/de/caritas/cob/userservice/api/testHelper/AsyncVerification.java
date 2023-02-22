package de.caritas.cob.userservice.api.testHelper;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Test methods to provide asynchronus method testing. */
public class AsyncVerification {

  private static final int MAX_TIMEOUT = 8;

  /**
   * Verifies if the passed verify function has been called during the given max timeout range.
   *
   * @param verificationFunction the verify function to be checked
   */
  public static void verifyAsync(Consumer<Void> verificationFunction) {
    await().atMost(MAX_TIMEOUT, SECONDS).until(verification(verificationFunction));
  }

  private static Callable<Boolean> verification(Consumer<Void> verificationFunction) {
    return () -> {
      verificationFunction.accept(null);
      return true;
    };
  }
}
