package de.caritas.cob.userservice.api.adapters.keycloak;

import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class KeycloakMapper {

  public OtpSetupDTO otpSetupDtoOf(String initialCode, String secret, String email) {
    var otpSetupDTO = new OtpSetupDTO();
    otpSetupDTO.setSecret(secret);
    otpSetupDTO.setInitialCode(initialCode);
    otpSetupDTO.setEmail(email);

    return otpSetupDTO;
  }

  public Map<String, Boolean> mapOf(HttpStatus statusCode) {
    return Map.of(
        "created", statusCode.equals(HttpStatus.CREATED),
        "attemptsLeft", !statusCode.equals(HttpStatus.TOO_MANY_REQUESTS)
    );
  }
}
