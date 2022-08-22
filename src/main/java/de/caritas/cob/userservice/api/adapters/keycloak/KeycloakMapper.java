package de.caritas.cob.userservice.api.adapters.keycloak;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import java.util.Map;
import java.util.Objects;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class KeycloakMapper {

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  public OtpSetupDTO otpSetupDtoOf(String initialCode, String secret, String email) {
    var otpSetupDTO = new OtpSetupDTO();
    otpSetupDTO.setSecret(secret);
    otpSetupDTO.setInitialCode(initialCode);
    otpSetupDTO.setEmail(email);

    return otpSetupDTO;
  }

  public UserRepresentation userRepresentationOf(String locale) {
    return new UserRepresentation().singleAttribute("locale", locale);
  }

  public Map<String, String> mapOf(ResponseEntity<SuccessWithEmail> responseEntity) {
    var status = responseEntity.getStatusCode();
    var isCreated = status.equals(HttpStatus.CREATED);
    var hasBeenCreatedBefore = status.equals(HttpStatus.OK);
    var hasBeenTriedTooOften = status.equals(HttpStatus.TOO_MANY_REQUESTS);

    return Map.of(
        "created", String.valueOf(isCreated),
        "createdBefore", String.valueOf(hasBeenCreatedBefore),
        "attemptsLeft",
            String.valueOf(!hasBeenTriedTooOften && !isCreated && !hasBeenCreatedBefore),
        "email", Objects.requireNonNull(responseEntity.getBody()).getEmail());
  }

  public Map<String, String> mapOf(HttpClientErrorException exception) {
    var status = exception.getStatusCode();

    return Map.of(
        "created", "false",
        "createdBefore", "false",
        "attemptsLeft", String.valueOf(!status.equals(HttpStatus.TOO_MANY_REQUESTS)),
        "email", "null");
  }

  public Map<String, String> mapOf(UserRepresentation userRepresentation) {
    var username = userRepresentation.getUsername();

    return Map.of(
        "encodedUsername", username,
        "decodedUsername", usernameTranscoder.decodeUsername(username),
        "email", userRepresentation.getEmail());
  }
}
