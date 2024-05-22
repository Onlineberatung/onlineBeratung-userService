package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UsernameTranscoderTest {

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  @Test
  public void
      encodeUsername_Should_ReturnEncodedUsernameWithReplacedPaddingAndAddedPrefix_WhenDecodedUsernameIsGiven() {
    assertEquals(USERNAME_ENCODED, usernameTranscoder.encodeUsername(USERNAME_DECODED));
  }

  @Test
  public void encodeUsername_Should_ReturnEncodedUsername_WhenEncodedUsernameIsGiven() {
    assertEquals(USERNAME_ENCODED, usernameTranscoder.encodeUsername(USERNAME_ENCODED));
  }

  @Test
  public void decodeUsername_Should_ReturnDecodedUsername_WhenEncodedUsernameIsGiven() {
    assertEquals(USERNAME_DECODED, usernameTranscoder.decodeUsername(USERNAME_ENCODED));
  }

  @Test
  public void decodeUsername_Should_ReturnDecodedUsername_WhenDecodedUsernameIsGiven() {
    assertEquals(USERNAME_DECODED, usernameTranscoder.decodeUsername(USERNAME_DECODED));
  }
}
