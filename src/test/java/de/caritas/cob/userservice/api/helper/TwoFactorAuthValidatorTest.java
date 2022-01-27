package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.INVALID_OTP_SETUP_DTO_WRONG_SECRET;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.OPTIONAL_OTP_INFO_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.VALID_OTP_SETUP_DTO;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.service.KeycloakTwoFactorAuthService;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class TwoFactorAuthValidatorTest {

  @InjectMocks
  private TwoFactorAuthValidator twoFactorAuthValidator;

  @Mock
  private KeycloakTwoFactorAuthService keycloakTwoFactorAuthService;

  static Stream<Arguments> twoFactorAuthEnabledRoleArguments() {
    return Stream.of(
        Arguments.of(true, false, UserRole.USER.getValue()),
        Arguments.of(true, true, UserRole.USER.getValue()),
        Arguments.of(false, true, UserRole.CONSULTANT.getValue()),
        Arguments.of(true, true, UserRole.CONSULTANT.getValue()));
  }

  static Stream<Arguments> twoFactorAuthDisabledRoleArguments() {
    return Stream.of(
        Arguments.of(false, false, UserRole.USER.getValue()),
        Arguments.of(false, true, UserRole.USER.getValue()),
        Arguments.of(false, false, UserRole.CONSULTANT.getValue()),
        Arguments.of(true, false, UserRole.CONSULTANT.getValue()));
  }

  @ParameterizedTest
  @MethodSource("twoFactorAuthEnabledRoleArguments")
  public void checkIfRoleHasTwoFactorAuthEnabled_Should_Not_ThrowConflictException_When_Request_Is_From_TwoFactorAuthEnabledRole(
      Boolean isUserTwoFactorAuthEnabled,
      Boolean isConsultantTwoFactorAuthEnabled,
      String requestRole) {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(requestRole));
    lenient().when(this.keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled())
        .thenReturn(isUserTwoFactorAuthEnabled);
    lenient().when(this.keycloakTwoFactorAuthService.getConsultantTwoFactorAuthEnabled())
        .thenReturn(isConsultantTwoFactorAuthEnabled);
    twoFactorAuthValidator.checkIfRoleHasTwoFactorAuthEnabled(authenticatedUser);

  }

  @ParameterizedTest
  @MethodSource("twoFactorAuthDisabledRoleArguments")
  public void checkIfRoleHasTwoFactorAuthEnabled_Should_ThrowConflictException_When_Request_Is_From_TwoFactorAuthDisabledRole(
      Boolean isUserTwoFactorAuthEnabled,
      Boolean isConsultantTwoFactorAuthEnabled,
      String requestRole) {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(requestRole));
    lenient().when(this.keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled())
        .thenReturn(isUserTwoFactorAuthEnabled);
    lenient().when(this.keycloakTwoFactorAuthService.getConsultantTwoFactorAuthEnabled())
        .thenReturn(isConsultantTwoFactorAuthEnabled);
    Assert.assertThrows(ConflictException.class,
        () -> twoFactorAuthValidator.checkIfRoleHasTwoFactorAuthEnabled(authenticatedUser));
  }


  @Test(expected = BadRequestException.class)
  public void checkRequestParameterForTwoFactorAuthActivations_Should_Throw_BadRequestException_When_Otp_Secret_Length_Does_Not_Match() {
    twoFactorAuthValidator
        .checkRequestParameterForTwoFactorAuthActivations(INVALID_OTP_SETUP_DTO_WRONG_SECRET);
  }

  @Test
  public void checkRequestParameterForTwoFactorAuthActivations_Should_Not_Throw_BadRequestException_When_Otp_Secret_Length_Match() {
    try {
      twoFactorAuthValidator
          .checkRequestParameterForTwoFactorAuthActivations(VALID_OTP_SETUP_DTO);
    } catch (Exception e) {
      Assertions.assertNull(e);
    }
  }

  @Test
  public void createAndValidateTwoFactorAuthDTO_Should_Return_Valid_Object_When_Request_Was_Successful() {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(UserRole.USER.getValue()));
    authenticatedUser.setUsername("test");
    when(keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled()).thenReturn(true);
    when(keycloakTwoFactorAuthService.getOtpCredential("test"))
        .thenReturn(OPTIONAL_OTP_INFO_DTO);

    var twoFactorAuthDTO = new TwoFactorAuthDTO()
        .isEnabled(true).isActive(false).secret("secret").qrCode("QrCode");

    Assertions
        .assertEquals(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser),
            twoFactorAuthDTO);
  }

  @Test
  public void createAndValidateTwoFactorAuthDTO_Should_Return_Null_When_Request_Was_Not_Successful() {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(UserRole.USER.getValue()));
    authenticatedUser.setUsername("test");
    when(keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled()).thenReturn(true);
    when(keycloakTwoFactorAuthService.getOtpCredential("test"))
        .thenReturn(Optional.empty());

    var twoFactorAuthDTO = new TwoFactorAuthDTO().isEnabled(false);

    Assertions
        .assertEquals(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser),
            twoFactorAuthDTO);
  }

  @Test
  public void createAndValidateTwoFactorAuthDTO_Should_Return_Valid_Object_When_Two_Factor_Auth_For_Role_Is_Disabled() {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(UserRole.USER.getValue()));
    authenticatedUser.setUsername("test");
    when(keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled()).thenReturn(false);

    var twoFactorAuthDTO = new TwoFactorAuthDTO().isEnabled(false);

    Assertions
        .assertEquals(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser),
            twoFactorAuthDTO);
    verify(keycloakTwoFactorAuthService, times(0))
        .getOtpCredential(Mockito.any());
  }

}
