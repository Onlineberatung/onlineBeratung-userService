package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.service.KeycloakTwoFactorAuthService;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Provides validator methods for 2FA.
 */
@Component
@RequiredArgsConstructor
public class TwoFactorAuthValidator {

  private static final int OTP_INITIAL_CODE_LENGTH = 6;
  private static final int OTP_SECRET_LENGTH = 32;

  private final @NonNull KeycloakTwoFactorAuthService keycloakTwoFactorAuthService;

  /**
   * Checks if the parameters of the request have the correct length.
   *
   * @param otpSetupDTO {@link OtpSetupDTO}
   */
  public void checkRequestParameterForTwoFactorAuthActivations(OtpSetupDTO otpSetupDTO) {
    if (isInitialCodeValid(otpSetupDTO) && isSecretValid(otpSetupDTO)) {
      return;
    }
    throw new BadRequestException("The request secret and/or initial code parameter are invalid.");
  }

  private boolean isSecretValid(OtpSetupDTO otpSetupDTO) {
    return nonNull(otpSetupDTO.getSecret())
        && otpSetupDTO.getSecret().length() == OTP_SECRET_LENGTH;
  }

  private boolean isInitialCodeValid(OtpSetupDTO otpSetupDTO) {
    return nonNull(otpSetupDTO.getInitialCode())
        && otpSetupDTO.getInitialCode().length() == OTP_INITIAL_CODE_LENGTH;
  }

  /**
   * Checks if 2FA is for user role disabled.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   */
  public void checkIfRoleHasTwoFactorAuthEnabled(AuthenticatedUser authenticatedUser) {
    var roles = authenticatedUser.getRoles();
    if (isUserRoleAnd2FaIsDisabled(roles) || isConsultantRoleAnd2FaIsDisabled(roles)) {
      throw new ConflictException("Two factor auth disabled for user role");
    }
  }

  private boolean isConsultantRoleAnd2FaIsDisabled(Set<String> roles) {
    return roles.contains(UserRole.CONSULTANT.getValue())
        && Boolean.FALSE.equals(keycloakTwoFactorAuthService.getConsultantTwoFactorAuthEnabled());
  }

  private boolean isUserRoleAnd2FaIsDisabled(Set<String> roles) {
    return roles.contains(UserRole.USER.getValue())
        && Boolean.FALSE.equals(keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled());
  }

  /**
   * Checks if user role has two factor authentication enabled. if yes it fetches the {@link
   * OtpInfoDTO} from keycloak and returns the {@link TwoFactorAuthDTO}.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link TwoFactorAuthDTO}
   */
  public TwoFactorAuthDTO createAndValidateTwoFactorAuthDTO(AuthenticatedUser authenticatedUser) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();
    twoFactorAuthDTO.setIsEnabled(isTwoFactorAuthEnabled(authenticatedUser));

    if (Boolean.TRUE.equals(twoFactorAuthDTO.getIsEnabled())) {
      return updateDtoWith2FaInformationFromKeycloak(authenticatedUser, twoFactorAuthDTO);
    }

    return twoFactorAuthDTO;
  }

  private TwoFactorAuthDTO updateDtoWith2FaInformationFromKeycloak(
      AuthenticatedUser authenticatedUser,
      TwoFactorAuthDTO twoFactorAuthDTO) {
    var optionalOtpInfoDTO = keycloakTwoFactorAuthService
        .getOtpCredential(authenticatedUser.getUsername());
    return optionalOtpInfoDTO
        .map(otpInfoDTO -> fillInTwoFactorAuth(twoFactorAuthDTO, otpInfoDTO))
        .orElseGet(() -> twoFactorAuthDTO.isEnabled(false));
  }

  private TwoFactorAuthDTO fillInTwoFactorAuth(TwoFactorAuthDTO twoFactorAuthDTO,
      OtpInfoDTO otpInfoDTO) {
    twoFactorAuthDTO.isActive(otpInfoDTO.getOtpSetup());
    if (Boolean.FALSE.equals(otpInfoDTO.getOtpSetup())) {
      twoFactorAuthDTO.setQrCode(otpInfoDTO.getOtpSecretQrCode());
      twoFactorAuthDTO.setSecret(otpInfoDTO.getOtpSecret());
    }
    return twoFactorAuthDTO;
  }

  /**
   * checks if user role has two factor authentication enabled.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link Boolean}
   */
  private Boolean isTwoFactorAuthEnabled(AuthenticatedUser authenticatedUser) {
    if (authenticatedUser.getRoles().contains(UserRole.USER.getValue())) {
      return keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled();
    } else if (authenticatedUser.getRoles().contains(UserRole.CONSULTANT.getValue())) {
      return keycloakTwoFactorAuthService.getConsultantTwoFactorAuthEnabled();
    }
    return Boolean.FALSE;
  }

}
