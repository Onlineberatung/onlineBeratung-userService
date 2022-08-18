package de.caritas.cob.userservice.api.adapters.web.controller.interceptor;

import static java.util.Optional.of;

import java.net.URISyntaxException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubdomainExtractorTest {

  private static final String MUCOVISCIDOSE = "mucoviscidose";
  private static final String ONLINBEBERATUNG_DE = ".onlineberatung.de";

  @InjectMocks SubdomainExtractor subdomainExtractor;

  @Test
  void resolveSubdomain_Should_resolveSubdomain() throws URISyntaxException {
    // given
    String url = MUCOVISCIDOSE + ONLINBEBERATUNG_DE;
    // when, then
    AssertionsForClassTypes.assertThat(subdomainExtractor.getSubdomain(url))
        .isEqualTo(of("mucoviscidose"));
  }

  @Test
  void resolveSubdomain_Should_resolveSubdomainForCompoundSubdomain() throws URISyntaxException {
    // given
    String url = "compound.subdomain" + ONLINBEBERATUNG_DE;
    // when, then
    AssertionsForClassTypes.assertThat(subdomainExtractor.getSubdomain(url))
        .isEqualTo(of("compound"));
  }
}
