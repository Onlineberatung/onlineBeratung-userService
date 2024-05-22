package de.caritas.cob.userservice.api.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthenticatedUserTest {

  @Test
  public void AuthenticatedUser_Should_ThrowNullPointerExceptionWhenArgumentsAreNull()
      throws Exception {
    assertThrows(
        NullPointerException.class,
        () -> {
          new AuthenticatedUser(null, null, null, null, null);
        });
  }

  @Test
  public void AuthenticatedUser_Should_ThrowNullPointerExceptionWhenUserIdIsNull()
      throws Exception {
    assertThrows(
        NullPointerException.class,
        () -> {
          AuthenticatedUser authenticatedUser = new AuthenticatedUser();
          authenticatedUser.setUserId(null);
        });
  }

  @Test
  public void AuthenticatedUser_Should_ThrowNullPointerExceptionWhenUsernameIsNull()
      throws Exception {
    assertThrows(
        NullPointerException.class,
        () -> {
          AuthenticatedUser authenticatedUser = new AuthenticatedUser();
          authenticatedUser.setUsername(null);
        });
  }

  @Test
  public void AuthenticatedUser_Should_ThrowNullPointerExceptionWhenAccessTokenIsNull()
      throws Exception {
    assertThrows(
        NullPointerException.class,
        () -> {
          AuthenticatedUser authenticatedUser = new AuthenticatedUser();
          authenticatedUser.setAccessToken(null);
        });
  }
}
