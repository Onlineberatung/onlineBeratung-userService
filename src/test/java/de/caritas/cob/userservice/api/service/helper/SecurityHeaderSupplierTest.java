package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;

@RunWith(MockitoJUnitRunner.class)
public class SecurityHeaderSupplierTest {

  private final String FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY = "csrfHeaderProperty";
  private final String FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY = "csrfCookieProperty";
  private final String CSRF_TOKEN_HEADER_VALUE = "X-CSRF-TOKEN";
  private final String CSRF_TOKEN_COOKIE_VALUE = "CSRF-TOKEN";
  private final String RC_GROUP_ID = "fR2Rz7dmWmHdXE8u2";
  private final String BEARER_TOKEN = "sadifsdfj)(JWifa";

  @InjectMocks
  private SecurityHeaderSupplier securityHeaderSupplier;
  @Mock
  private AuthenticatedUser authenticatedUser;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    // serviceHelper = new ServiceHelper();
    FieldSetter.setField(securityHeaderSupplier,
        securityHeaderSupplier.getClass().getDeclaredField(FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY),
        CSRF_TOKEN_HEADER_VALUE);
    FieldSetter.setField(securityHeaderSupplier,
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
    assertTrue(result.get("Cookie").toString().startsWith("[" + CSRF_TOKEN_COOKIE_VALUE + "="));

  }

  @Test
  public void getCsrfHttpHeaders_Should_Return_HeaderWithPropertyNameFromProperties() {

    HttpHeaders result = securityHeaderSupplier.getCsrfHttpHeaders();
    assertNotNull(result.get(CSRF_TOKEN_HEADER_VALUE));

  }

  @Test
  public void getRocketChatAndCsrfHttpHeaders_Should_ReturnHeaderWithRocketChatUserProperties() {
    when(authenticatedUser.getAccessToken()).thenReturn(BEARER_TOKEN);

    HttpHeaders result = securityHeaderSupplier
        .getRocketChatAndCsrfHttpHeaders(RC_CREDENTIALS, RC_GROUP_ID);
    assertEquals("[" + RC_USER_ID + "]", result.get("rcUserId").toString());
    assertEquals("[" + RC_TOKEN + "]", result.get("rcToken").toString());
    assertEquals("[" + RC_GROUP_ID + "]", result.get("rcGroupId").toString());
  }

  @Test
  public void getRocketChatAndCsrfHttpHeaders_Should_ReturnHeaderWithKeycloakAuthToken() {
    when(authenticatedUser.getAccessToken()).thenReturn(BEARER_TOKEN);

    HttpHeaders result = securityHeaderSupplier
        .getRocketChatAndCsrfHttpHeaders(RC_CREDENTIALS, RC_GROUP_ID);
    assertEquals("[Bearer " + BEARER_TOKEN + "]", result.get("Authorization").toString());
  }
}
