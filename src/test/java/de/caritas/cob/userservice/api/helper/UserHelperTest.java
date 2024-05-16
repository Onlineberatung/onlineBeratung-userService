package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_CONSULTANT_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_SHORT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserHelperTest {

  private static final EasyRandom easyRandom = new EasyRandom();
  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  @InjectMocks private UserHelper userHelper;
  @Mock private IdentityClientConfig identityClientConfig;

  @BeforeEach
  void setup() throws NoSuchFieldException, SecurityException {
    when(identityClientConfig.getEmailDummySuffix()).thenReturn(FIELD_VALUE_EMAIL_DUMMY_SUFFIX);
    setField(userHelper, "usernameTranscoder", usernameTranscoder);
  }

  @Test
  void isUsernameValid_Should_ReturnFalse_WhenUsernameIsTooShort() {
    assertFalse(userHelper.isUsernameValid(USERNAME_TOO_SHORT));
  }

  @Test
  void isUsernameValid_Should_ReturnFalse_WhenUsernameIsTooLong() {
    assertFalse(userHelper.isUsernameValid(USERNAME_TOO_LONG));
  }

  @Test
  void isValidEmailShouldReturnTrueOnValidAddress() {
    var emailAddress = givenARandomEmail();

    assertTrue(userHelper.isValidEmail(emailAddress));
  }

  @Test
  void isValidEmailShouldReturnTrueOnAddressWithUmlaut() {
    var emailAddress = givenARandomEmailWithAnUmlaut();

    assertTrue(userHelper.isValidEmail(emailAddress));
  }

  @Test
  void isValidEmailShouldReturnFalseOnEmptyAddress() {
    assertFalse(userHelper.isValidEmail(null));
    assertFalse(userHelper.isValidEmail(""));
    assertFalse(userHelper.isValidEmail("@"));
    assertFalse(userHelper.isValidEmail("@.de"));
    assertFalse(userHelper.isValidEmail("@sld.de"));
  }

  @Test
  void getDummyEmail_Should_ReturnRcUserIdWithDummyEmailSuffix() {
    assertEquals(USER_ID + FIELD_VALUE_EMAIL_DUMMY_SUFFIX, userHelper.getDummyEmail(USER_ID));
  }

  @Test
  void doUsernamesMatch_Should_ReturnFalse_WhenDecodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_DECODED, USERNAME_DECODED));
  }

  @Test
  void doUsernamesMatch_Should_ReturnFalse_WhenEncodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_ENCODED, USERNAME_ENCODED));
  }

  @Test
  void doUsernamesMatch_Should_ReturnFalse_WhenEncodedAndDecodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_ENCODED, USERNAME_DECODED));
  }

  @Test
  void doUsernamesMatch_Should_ReturnTrue_WhenDecodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_DECODED, USERNAME_DECODED));
  }

  @Test
  void doUsernamesMatch_Should_ReturnTrue_WhenEncodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_ENCODED, USERNAME_ENCODED));
  }

  @Test
  void doUsernamesMatch_Should_ReturnTrue_WhenEncodedAndDecodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_ENCODED, USERNAME_DECODED));
  }

  private String givenARandomEmail() {
    return randomAlphabetic(16)
        + "@"
        + randomAlphabetic(8)
        + "."
        + (easyRandom.nextBoolean() ? "de" : "com");
  }

  private String givenARandomEmailWithAnUmlaut() {
    var umlauts = List.of("ä", "ö", "ü");

    return randomAlphabetic(8)
        + umlauts.get(easyRandom.nextInt(umlauts.size()))
        + randomAlphabetic(8)
        + "@"
        + randomAlphabetic(8)
        + "."
        + (easyRandom.nextBoolean() ? "de" : "com");
  }
}
