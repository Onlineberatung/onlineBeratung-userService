package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.service.KeycloakTwoFactorAuthService;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TwoFactorAuthValidator {

  private final @NonNull KeycloakTwoFactorAuthService keycloakTwoFactorAuthService;

  private static final int OTP_INITIAL_CODE_LENGTH = 6;
  private static final int OTP_SECRET_LENGTH = 32;

  public void checkRequestParameterForTwoFactorAuthActivations(OtpSetupDTO otpSetupDTO) {
    if (otpSetupDTO.getInitialCode().length() != OTP_INITIAL_CODE_LENGTH
        || otpSetupDTO.getSecret().length() != OTP_SECRET_LENGTH) {
      throw new BadRequestException("The request parameter are invalid");
    }
  }

  public void checkIfRoleHasTwoFactorAuthEnabled(Set<String> roles, String role, boolean enabled) {
    if ((roles.contains(role) && Boolean.FALSE.equals(enabled))) {
      throw new ConflictException(role + " has two factor auth disabled");
    }
  }

  public TwoFactorAuthDTO createAndValidateTwoFactorAuthDTO(AuthenticatedUser authenticatedUser) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();

    if(authenticatedUser.getRoles().contains(UserRole.USER.getValue()))
      twoFactorAuthDTO.isEnabled(keycloakTwoFactorAuthService.getUserTwoFactorAuthEnabled());
    else if(authenticatedUser.getRoles().contains(UserRole.CONSULTANT.getValue()))
      twoFactorAuthDTO.isEnabled(keycloakTwoFactorAuthService.getConsultantTwoFactorAuthEnabled());
    else
      twoFactorAuthDTO.isEnabled(false);

    if(twoFactorAuthDTO.getIsEnabled()){
      var OptionalOtpInfoDTO = keycloakTwoFactorAuthService.getOtpCredential(authenticatedUser.getUsername());

      if(OptionalOtpInfoDTO.isPresent()){
        twoFactorAuthDTO.isActive(OptionalOtpInfoDTO.get().getOtpSetup());

        if (Boolean.FALSE.equals(OptionalOtpInfoDTO.get().getOtpSetup())) {
          twoFactorAuthDTO.setQrCode(OptionalOtpInfoDTO.get().getOtpSecretQrCode());
          twoFactorAuthDTO.setSecret(OptionalOtpInfoDTO.get().getOtpSecret());
        }
        return twoFactorAuthDTO;
      }
      return twoFactorAuthDTO.isEnabled(false);
    }
    return twoFactorAuthDTO;
  }

}
