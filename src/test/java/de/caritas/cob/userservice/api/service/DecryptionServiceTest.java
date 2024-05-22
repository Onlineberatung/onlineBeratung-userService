package de.caritas.cob.userservice.api.service;

import static org.junit.jupiter.api.Assertions.*;

import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DecryptionServiceTest {

  private final String KEY_MASTER = "MasterKeyTestKey";
  private final String KEY_APPLICATION = "ApplicationTestKey";
  private final String KEY_SESSION = "SessionTestKey";
  private final String KEY_SESSION_WRONG = "WrongSessionTestKey";
  private final String FIELD_NAME_FRAGMENT_APP_KEY = "fragment_applicationKey";

  private final String MESSAGE_PLAIN = "Das hier ist jetzt mal eine Test-Message";
  private final String MESSAGE_ENCRYPTED =
      "enc:uWHNUkWrQJikGnVpknvB3SkzT1RWHJuY0igDT9p7fGFHWECLBpV2+0eIZF6Qi7J0";

  @InjectMocks private DecryptionService encryptionService;

  @Mock private LogService logService;

  @BeforeEach
  public void setup() throws NoSuchFieldException {
    ReflectionTestUtils.setField(encryptionService, FIELD_NAME_FRAGMENT_APP_KEY, KEY_APPLICATION);
    encryptionService.updateMasterKey(KEY_MASTER);
  }

  @Test
  public void check_setup() {
    assertEquals(KEY_MASTER, encryptionService.getMasterKey(), "MasterKey was not properly set");
    assertEquals(
        KEY_APPLICATION,
        encryptionService.getApplicationKey(),
        "ApplicationKey was not properly set");
  }

  @Test
  public void updateMasterKey_Should_UpdateMasterKeyFragment() {
    encryptionService.updateMasterKey(KEY_MASTER);
    assertEquals(KEY_MASTER, encryptionService.getMasterKey(), "Cannot properly set MasterKey");
  }

  @Test
  public void decrypt_Should_ReturnDecryptedText_WhenProvidedWithValidParameters()
      throws Exception {
    String decryptedMessage = encryptionService.decrypt(MESSAGE_ENCRYPTED, KEY_SESSION);
    assertEquals(MESSAGE_PLAIN, decryptedMessage, "Did not get the expected decrypted result.");
  }

  @Test
  public void decrypt_Should_ReturnWrongDecryptedText_WhenProvidedWithInvalidParameters() {
    try {
      encryptionService.decrypt(MESSAGE_ENCRYPTED, KEY_SESSION_WRONG);
      fail("The expected BadPaddingException due to wrong password was not thrown.");
    } catch (CustomCryptoException ex) {
      assertTrue(true, "Expected BadPaddingException thrown");
    }
  }
}
