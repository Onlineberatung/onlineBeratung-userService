package de.caritas.cob.UserService.api.helper;

import static de.caritas.cob.UserService.testHelper.FieldConstants.FIELD_NAME_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.UserService.testHelper.FieldConstants.FIELD_NAME_HOST_BASE_URL;
import static de.caritas.cob.UserService.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.UserService.testHelper.KeycloakConstants.USER_REPRESENTATION_WITH_ENCODED_USERNAME;
import static de.caritas.cob.UserService.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.CHAT_LINK_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.HOST_BASE_URL;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_CONSULTANT_DECODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_INVALID_ENCODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_TOO_SHORT;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_NO_RC_USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.exception.HelperException;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.UserService;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;

@RunWith(MockitoJUnitRunner.class)
public class UserHelperTest {

  @InjectMocks
  private UserHelper userHelper;
  @Mock
  private KeycloakAdminClientHelper keycloakAdminClientHelper;
  @Mock
  private UserService userService;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(userHelper,
        userHelper.getClass().getDeclaredField(FIELD_NAME_EMAIL_DUMMY_SUFFIX),
        FIELD_VALUE_EMAIL_DUMMY_SUFFIX);
    FieldSetter.setField(userHelper,
        userHelper.getClass().getDeclaredField(FIELD_NAME_HOST_BASE_URL), HOST_BASE_URL);
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
  public void getDummyEmail_Should_ReturnRcUserIdWithDummyEmailSuffix() {
    assertEquals(USER_ID + FIELD_VALUE_EMAIL_DUMMY_SUFFIX, userHelper.getDummyEmail(USER_ID));
  }

  @Test
  public void encodeUsername_Should_ReturnEncodedUsernameWithReplacedPaddingAndAddedPrefix_WhenDecodedUsernameIsGiven() {
    assertEquals(USERNAME_ENCODED, userHelper.encodeUsername(USERNAME_DECODED));
  }

  @Test
  public void encodeUsername_Should_ReturnEncodedUsername_WhenEncodedUsernameIsGiven() {
    assertEquals(USERNAME_ENCODED, userHelper.encodeUsername(USERNAME_ENCODED));
  }

  @Test
  public void decodeUsername_Should_ReturnDecodedUsername_WhenEncodedUsernameIsGiven() {
    assertEquals(USERNAME_DECODED, userHelper.decodeUsername(USERNAME_ENCODED));
  }

  @Test
  public void decodeUsername_Should_ReturnDecodedUsername_WhenDecodedUsernameIsGiven() {
    assertEquals(USERNAME_DECODED, userHelper.decodeUsername(USERNAME_DECODED));
  }

  @Test
  public void decodeUsername_Should_ThrowHelperException_WhenDecodingFails() {

    try {
      userHelper.decodeUsername(USERNAME_INVALID_ENCODED);
      fail("Expected exception: HelperException");
    } catch (HelperException helperException) {
      assertTrue("Excepted HelperException thrown", true);
    }

  }

  @Test
  public void isUsernameAvailable_Should_ReturnFalse_WhenUsernameIsAlreadyTaken() {
    List<UserRepresentation> userRepresentationList = new ArrayList<UserRepresentation>();
    userRepresentationList.add(USER_REPRESENTATION_WITH_ENCODED_USERNAME);

    when(keycloakAdminClientHelper.findByUsername(Mockito.any()))
        .thenReturn(userRepresentationList);

    assertFalse(userHelper.isUsernameAvailable(USERNAME));
  }

  @Test
  public void isUsernameAvailable_Should_ReturnTrue_WhenUsernameIsAvailable() {
    List<UserRepresentation> userRepresentationList = new ArrayList<UserRepresentation>();
    when(keycloakAdminClientHelper.findByUsername(Mockito.any()))
        .thenReturn(userRepresentationList);

    assertTrue(userHelper.isUsernameAvailable(USERNAME));
  }

  @Test
  public void generateChatUrl_Should_ReturnChatLinkWithConsultingTypeUrlNameAndEncodedChatId() {
    assertEquals(CHAT_LINK_SUCHT, userHelper.generateChatUrl(CHAT_ID, CONSULTING_TYPE_SUCHT));
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

  @Test
  public void updateRocketChatIdInDatabase_Should_UpdateUserObjectAndSaveToDb() {

    ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);

    userHelper.updateRocketChatIdInDatabase(USER_NO_RC_USER_ID, RC_USER_ID);

    verify(userService).saveUser(argument.capture());
    assertEquals(RC_USER_ID, argument.getValue().getRcUserId());
  }
}
