package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.OtpType;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserDtoMapper {

  public TwoFactorAuthDTO twoFactorAuthDtoOf(Boolean encourage2fa) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();
    twoFactorAuthDTO.setIsToEncourage(encourage2fa);

    return twoFactorAuthDTO;
  }

  public TwoFactorAuthDTO twoFactorAuthDtoOf(OtpInfoDTO otpInfoDTO, Boolean encourage2fa) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();
    twoFactorAuthDTO.setIsEnabled(true);
    twoFactorAuthDTO.setIsToEncourage(encourage2fa);

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

  public Map<String, Object> mapOf(PatchUserDTO patchUserDTO, AuthenticatedUser user) {
    return Map.of(
        "id", user.getUserId(),
        "encourage2fa", patchUserDTO.getEncourage2fa()
    );
  }

  public Map<String, Object> mapOf(String email, AuthenticatedUser user) {
    return Map.of(
        "id", user.getUserId(),
        "email", email
    );
  }

  @SuppressWarnings("unchecked")
  public List<String> bannedChatUserIdsOf(Map<String, Object> chatMetaInfoMap) {
    return (List<String>) chatMetaInfoMap.get("mutedUsers");
  }
}
