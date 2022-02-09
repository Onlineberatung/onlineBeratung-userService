package de.caritas.cob.userservice.api.helper;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Provides validator methods for 2FA.
 */
@Component
@RequiredArgsConstructor
public class TwoFactorAuthValidator {

  private final IdentityClient identityClient;
  private final IdentityClientConfig identityClientConfig;

  /**
   * Checks if user role has two factor authentication enabled. if yes it fetches the {@link
   * OtpInfoDTO} from keycloak and returns the {@link TwoFactorAuthDTO}.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link TwoFactorAuthDTO}
   */
  public TwoFactorAuthDTO createAndValidateTwoFactorAuthDTO(AuthenticatedUser authenticatedUser) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO()
        .isEnabled(isTwoFactorAuthEnabled(authenticatedUser));

    if (isTrue(twoFactorAuthDTO.getIsEnabled())) {
      return updateDtoWith2FaInformationFromKeycloak(authenticatedUser, twoFactorAuthDTO);
    }

    return twoFactorAuthDTO;
  }

  private TwoFactorAuthDTO updateDtoWith2FaInformationFromKeycloak(
      AuthenticatedUser authenticatedUser, TwoFactorAuthDTO twoFactorAuthDTO) {
    var username = authenticatedUser.getUsername();

    return identityClient.getOtpCredential(username)
        .map(otpInfoDTO -> fillInTwoFactorAuth(twoFactorAuthDTO, otpInfoDTO))
        .orElseGet(() -> twoFactorAuthDTO.isEnabled(false));
  }

  private TwoFactorAuthDTO fillInTwoFactorAuth(TwoFactorAuthDTO twoFactorAuthDTO,
      OtpInfoDTO otpInfoDTO) {
    twoFactorAuthDTO.isActive(otpInfoDTO.getOtpSetup());
    if (isFalse(otpInfoDTO.getOtpSetup())) {
      twoFactorAuthDTO.setQrCode(otpInfoDTO.getOtpSecretQrCode());
      twoFactorAuthDTO.setSecret(otpInfoDTO.getOtpSecret());
    }
    return twoFactorAuthDTO;
  }

  private Boolean isTwoFactorAuthEnabled(AuthenticatedUser authenticatedUser) {
    if (authenticatedUser.getRoles().contains(UserRole.USER.getValue())) {
      return identityClientConfig.getOtpAllowedForUsers();
    } else if (authenticatedUser.getRoles().contains(UserRole.CONSULTANT.getValue())) {
      return identityClientConfig.getOtpAllowedForConsultants();
    }
    return Boolean.FALSE;
  }

}
