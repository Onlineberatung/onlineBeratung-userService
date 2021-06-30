package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.service.KeycloakTwoFactorAuthService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TwoFactorAuthValidator {

  private final @NonNull KeycloakTwoFactorAuthService keycloakTwoFactorAuthService;

  private static final int OTP_INITIAL_CODE_LENGTH = 6;
  private static final int OTP_SECRET_LENGTH = 32;

  /**
   * checks if the parameters of the request have the correct length
   *
   * @param otpSetupDTO {@link OtpSetupDTO}
   */
  public void checkRequestParameterForTwoFactorAuthActivations(OtpSetupDTO otpSetupDTO) {
    if (otpSetupDTO.getInitialCode().length() != OTP_INITIAL_CODE_LENGTH
        || otpSetupDTO.getSecret().length() != OTP_SECRET_LENGTH) {
      throw new BadRequestException("The request parameter are invalid");
    }
  }

  /**
   * checks if user role has two factor authentication disabled. if yes it throws a conflict
   * exception.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   */
  public void checkIfRoleHasTwoFactorAuthEnabled(AuthenticatedUser authenticatedUser) {
    var roles = authenticatedUser.getRoles();
    if ((roles.contains(UserRole.USER.getValue()) && Boolean.FALSE
        .equals(keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled())
        || roles.contains(UserRole.CONSULTANT.getValue()) && Boolean.FALSE
        .equals(keycloakTwoFactorAuthService.getConsultantTwoFactorAuthEnabled()))) {
      throw new ConflictException("Role from the request has two factor auth disabled");
    }
  }

  /**
   * checks if user role has two factor authentication enabled. if yes it fetches the {@link
   * OtpInfoDTO} from keycloak and returns the data.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link TwoFactorAuthDTO}
   */
  public TwoFactorAuthDTO createAndValidateTwoFactorAuthDTO(AuthenticatedUser authenticatedUser) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();

    twoFactorAuthDTO.setIsEnabled(isTwoFactorAuthEnabled(authenticatedUser));

    if (Boolean.TRUE.equals(twoFactorAuthDTO.getIsEnabled())) {
      var optionalOtpInfoDTO = keycloakTwoFactorAuthService
          .getOtpCredential(authenticatedUser.getUsername());

      if (optionalOtpInfoDTO.isPresent()) {
        fillInTwoFactorAuth(twoFactorAuthDTO, optionalOtpInfoDTO.get());
        return twoFactorAuthDTO;
      }
      return twoFactorAuthDTO.isEnabled(false);
    }
    return twoFactorAuthDTO;
  }

  /**
   * checks if user role has two factor authentication enabled..
   *
   * @param twoFactorAuthDTO {@link TwoFactorAuthDTO} the object which need to be filled
   * @param otpInfoDTO       {@link OtpInfoDTO} the object with the data
   */
  private void fillInTwoFactorAuth(TwoFactorAuthDTO twoFactorAuthDTO, OtpInfoDTO otpInfoDTO) {
    twoFactorAuthDTO.isActive(otpInfoDTO.getOtpSetup());

    if (Boolean.FALSE.equals(otpInfoDTO.getOtpSetup())) {
      twoFactorAuthDTO.setQrCode(otpInfoDTO.getOtpSecretQrCode());
      twoFactorAuthDTO.setSecret(otpInfoDTO.getOtpSecret());
    }
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
    return false;
  }

}
