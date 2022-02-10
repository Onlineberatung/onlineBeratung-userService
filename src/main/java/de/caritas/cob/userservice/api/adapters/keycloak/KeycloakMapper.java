package de.caritas.cob.userservice.api.adapters.keycloak;

import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  public Map<String, String> mapOf(ResponseEntity<SuccessWithEmail> responseEntity) {
    var status = responseEntity.getStatusCode();
    var isCreated = status.equals(HttpStatus.CREATED);

    return Map.of(
        "created", String.valueOf(isCreated),
        "attemptsLeft", String.valueOf(status.equals(HttpStatus.TOO_MANY_REQUESTS) || isCreated),
        "email", Objects.requireNonNull(responseEntity.getBody()).getEmail()
    );
  }
}
