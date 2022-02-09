package de.caritas.cob.userservice.api.adapters.keycloak;

import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import org.springframework.stereotype.Service;

@Service
public class KeycloakMapper {

  public OtpSetupDTO otpSetupDtoOf(String initialCode, String secret) {
    var otpSetupDTO = new OtpSetupDTO();
    otpSetupDTO.setSecret(secret);
    otpSetupDTO.setInitialCode(initialCode);

    return otpSetupDTO;
  }
}
