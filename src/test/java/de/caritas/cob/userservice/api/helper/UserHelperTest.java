package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_NAME_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_NAME_HOST_BASE_URL;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_LINK_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.HOST_BASE_URL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_CONSULTANT_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_SHORT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserHelperTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks private UserHelper userHelper;

  @Mock private ConsultingTypeManager consultingTypeManager;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    ReflectionTestUtils.setField(
        userHelper, FIELD_NAME_EMAIL_DUMMY_SUFFIX, FIELD_VALUE_EMAIL_DUMMY_SUFFIX);
    ReflectionTestUtils.setField(userHelper, FIELD_NAME_HOST_BASE_URL, HOST_BASE_URL);
  }

  @Test
  public void isUsernameValid_Should_ReturnFalse_WhenUsernameIsTooShort() {
    assertFalse(userHelper.isUsernameValid(USERNAME_TOO_SHORT));
  }

  @Test
  public void isUsernameValid_Should_ReturnFalse_WhenUsernameIsTooLong() {
    assertFalse(userHelper.isUsernameValid(USERNAME_TOO_LONG));
  }

  @Test
  public void isValidEmailShouldReturnTrueOnValidAddress() {
    var emailAddress = givenARandomEmail();

    assertTrue(userHelper.isValidEmail(emailAddress));
  }

  @Test
  public void isValidEmailShouldReturnTrueOnAddressWithUmlaut() {
    var emailAddress = givenARandomEmailWithAnUmlaut();

    assertTrue(userHelper.isValidEmail(emailAddress));
  }

  @Test
  public void isValidEmailShouldReturnFalseOnEmptyAddress() {
    assertFalse(userHelper.isValidEmail(null));
    assertFalse(userHelper.isValidEmail(""));
    assertFalse(userHelper.isValidEmail("@"));
    assertFalse(userHelper.isValidEmail("@.de"));
    assertFalse(userHelper.isValidEmail("@sld.de"));
  }

  @Test
  public void getDummyEmail_Should_ReturnRcUserIdWithDummyEmailSuffix() {
    assertEquals(USER_ID + FIELD_VALUE_EMAIL_DUMMY_SUFFIX, userHelper.getDummyEmail(USER_ID));
  }

  @Test
  public void generateChatUrl_Should_ReturnChatLinkWithConsultingTypeUrlNameAndEncodedChatId() {
    when(consultingTypeManager.getConsultingTypeSettings(0))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);
    assertEquals(CHAT_LINK_SUCHT, userHelper.generateChatUrl(CHAT_ID, 0));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnFalse_WhenDecodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_DECODED, USERNAME_DECODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnFalse_WhenEncodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_ENCODED, USERNAME_ENCODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnFalse_WhenEncodedAndDecodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_ENCODED, USERNAME_DECODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnTrue_WhenDecodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_DECODED, USERNAME_DECODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnTrue_WhenEncodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_ENCODED, USERNAME_ENCODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnTrue_WhenEncodedAndDecodedUsernamesMatch() {
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
