package de.caritas.cob.userservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class SubdomainExtractorTest {

  @Test
  public void local_development_test() {

    SubdomainExtractor extractor = new SubdomainExtractor();
    Optional<String> s = extractor
        .extractSubdomain("tenant1.onlineberatung.local/something");

    assertEquals(s.get(), "tenant1");

  }

}
