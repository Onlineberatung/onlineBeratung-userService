package de.caritas.cob.userservice.api.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserVerifierTest {

  private static final String VALID_AGE = "20";

  @InjectMocks private UserVerifier userVerifier;
  @Mock private KeycloakService keycloakService;

  EasyRandom easyRandom = new EasyRandom();

  @Test
  void
      checkIfUsernameIsAvailable_Should_ThrowCustomValidationHttpStatusException_When_UsernameIsNotAvailable() {
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    when(keycloakService.isUsernameAvailable(userDTO.getUsername())).thenReturn(false);

    try {
      userVerifier.checkIfUsernameIsAvailable(userDTO);
    } catch (CustomValidationHttpStatusException exception) {
      assertThat(exception, instanceOf(CustomValidationHttpStatusException.class));
      assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
    }
  }

  @Test
  void checkIfUsernameIsAvailable_ShouldNot_ThrowException_When_UsernameIsAvailable() {
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    when(keycloakService.isUsernameAvailable(userDTO.getUsername())).thenReturn(true);

    userVerifier.checkIfUsernameIsAvailable(userDTO);
  }

  @Test
  void
      checkIfAllRequiredAttributesAreCorrectlyFilled_Should_ThrowCustomValidationHttpStatusException_When_DemographicsFeatureOnAndGenderNotSet() {
    ReflectionTestUtils.setField(userVerifier, "demographicsFeatureEnabled", true);
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setAge(VALID_AGE);
    userDTO.setUserGender(null);

    try {
      userVerifier.checkIfAllRequiredAttributesAreCorrectlyFilled(userDTO);
    } catch (CustomValidationHttpStatusException exception) {
      assertThat(exception, instanceOf(CustomValidationHttpStatusException.class));
      assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "testAge", "-1", "101"})
  void
      checkIfAllRequiredAttributesAreCorrectlyFilled_Should_ThrowCustomValidationHttpStatusException_When_DemographicsFeatureOnAndAgeNotValid(
          String age) {
    ReflectionTestUtils.setField(userVerifier, "demographicsFeatureEnabled", true);
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setUserGender("MALE");
    userDTO.setAge(age);

    try {
      userVerifier.checkIfAllRequiredAttributesAreCorrectlyFilled(userDTO);
    } catch (CustomValidationHttpStatusException exception) {
      assertThat(exception, instanceOf(CustomValidationHttpStatusException.class));
      assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }
  }

  @Test
  void
      checkIfAllRequiredAttributesAreCorrectlyFilled_Should_PassValidation_When_DemographicsFeatureOnAndAgeAndGenderInValidFormat() {
    ReflectionTestUtils.setField(userVerifier, "demographicsFeatureEnabled", true);
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setUserGender("MALE");
    userDTO.setAge("20");

    // no exception is thrown
    try {
      userVerifier.checkIfAllRequiredAttributesAreCorrectlyFilled(userDTO);
    } catch (Exception exception) {
      fail("no exception is expected");
    }
  }

  @AfterEach
  public void tearDown() {
    ReflectionTestUtils.setField(userVerifier, "demographicsFeatureEnabled", false);
  }
}
