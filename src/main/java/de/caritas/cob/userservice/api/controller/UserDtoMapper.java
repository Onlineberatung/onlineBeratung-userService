package de.caritas.cob.userservice.api.controller;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpType;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import org.springframework.stereotype.Service;

@Service
public class UserDtoMapper {

  public TwoFactorAuthDTO twoFactorAuthDtoOf() {
    return new TwoFactorAuthDTO();
  }

  public TwoFactorAuthDTO twoFactorAuthDtoOf(OtpInfoDTO otpInfoDTO) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();
    twoFactorAuthDTO.setIsEnabled(true);

    if (otpInfoDTO.getOtpSetup()) {
      twoFactorAuthDTO.isActive(true);
      var foreignType = otpInfoDTO.getOtpType();
      if (nonNull(foreignType)) {
        var type = foreignType.getValue().equals("APP") ? OtpType.APP : OtpType.EMAIL;
        twoFactorAuthDTO.setType(type);
      }
    } else {
      twoFactorAuthDTO.setQrCode(otpInfoDTO.getOtpSecretQrCode());
      twoFactorAuthDTO.setSecret(otpInfoDTO.getOtpSecret());
    }

    return twoFactorAuthDTO;
  }
}
