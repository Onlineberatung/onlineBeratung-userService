package de.caritas.cob.userservice.api.service.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.FieldSetter.setField;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RunWith(MockitoJUnitRunner.class)
public class SecurityHeaderSupplierTest {

  private final String FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY = "csrfHeaderProperty";
  private final String FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY = "csrfCookieProperty";
  private final String CSRF_TOKEN_HEADER_VALUE = "X-CSRF-TOKEN";
  private final String CSRF_TOKEN_COOKIE_VALUE = "CSRF-TOKEN";
  private final String BEARER_TOKEN = "sadifsdfj)(JWifa";

  @InjectMocks
  private SecurityHeaderSupplier securityHeaderSupplier;
  @Mock
  private AuthenticatedUser authenticatedUser;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(securityHeaderSupplier,
        securityHeaderSupplier.getClass().getDeclaredField(FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY),
        CSRF_TOKEN_HEADER_VALUE);
    setField(securityHeaderSupplier,
        securityHeaderSupplier.getClass().getDeclaredField(FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY),
        CSRF_TOKEN_COOKIE_VALUE);
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
}
