package de.caritas.cob.userservice.api.service.httpheader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
public class SecurityHeaderSupplierTest {

  private final String CSRF_TOKEN_HEADER_VALUE = "X-CSRF-TOKEN";
  private final String CSRF_TOKEN_COOKIE_VALUE = "CSRF-TOKEN";
  private final String BEARER_TOKEN = "sadifsdfj)(JWifa";

  @InjectMocks private SecurityHeaderSupplier securityHeaderSupplier;
  @Mock private AuthenticatedUser authenticatedUser;

  @BeforeEach
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(securityHeaderSupplier, "csrfHeaderProperty", CSRF_TOKEN_HEADER_VALUE);
    setField(securityHeaderSupplier, "csrfCookieProperty", CSRF_TOKEN_COOKIE_VALUE);
  }

  @Test
  public void getCsrfHttpHeaders_Should_Return_HeaderWithCorrectContentType() {
    HttpHeaders result = securityHeaderSupplier.getCsrfHttpHeaders();
    assertEquals(MediaType.APPLICATION_JSON, result.getContentType());
  }

  @Test
  public void getCsrfHttpHeaders_Should_Return_HeaderWithCookiePropertyNameFromProperties() {
    HttpHeaders result = securityHeaderSupplier.getCsrfHttpHeaders();
    assertThat(result.get("Cookie").get(0), startsWith(CSRF_TOKEN_COOKIE_VALUE + "="));
  }

  @Test
  public void getCsrfHttpHeaders_Should_Return_HeaderWithPropertyNameFromProperties() {
    HttpHeaders result = securityHeaderSupplier.getCsrfHttpHeaders();
    assertNotNull(result.get(CSRF_TOKEN_HEADER_VALUE));
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_ReturnHeaderWithKeycloakAuthorizationHeader() {
    when(authenticatedUser.getAccessToken()).thenReturn(BEARER_TOKEN);

    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();

    assertThat("Bearer " + BEARER_TOKEN, is(result.get("Authorization").get(0)));
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_ReturnHeaderWithValidCsrfHeaderProperties() {
    when(authenticatedUser.getAccessToken()).thenReturn(BEARER_TOKEN);

    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();

    assertThat(result.get("Cookie").get(0), startsWith(CSRF_TOKEN_COOKIE_VALUE + "="));
    assertNotNull(result.get(CSRF_TOKEN_HEADER_VALUE));
  }

  @Test
  public void
      getTechnicalKeycloakAndCsrfHttpHeaders_Should_ReturnHeaderWithKeycloakAuthorizationHeader() {
    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders("token");

    assertThat(result.get("Authorization").get(0), is("Bearer " + "token"));
    assertNotNull(result.get(CSRF_TOKEN_HEADER_VALUE));
  }
}
