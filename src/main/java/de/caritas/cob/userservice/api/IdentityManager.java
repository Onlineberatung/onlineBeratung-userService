package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.port.in.IdentityManaging;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityManager implements IdentityManaging {

  private final IdentityClient identityClient;

  @Override
  public Optional<String> setUpOneTimePassword(String username, String email) {
    return identityClient.initiateEmailVerification(username, email);
  }

  @Override
  public boolean setUpOneTimePassword(String username, String initialCode, String secret) {
    return identityClient.setUpOtpCredential(username, initialCode, secret);
  }

  @Override
  public Map<String, String> validateOneTimePassword(String username, String code) {
    var validationResult = identityClient.finishEmailVerification(username, code);
    if (validationResult.get("created").equals("true")) {
      var email = validationResult.get("email");
      identityClient.changeEmailAddress(username, email);
    }

    return validationResult;
  }

  @Override
  public boolean validatePasswordIgnoring2fa(String username, String password) {
    return identityClient.verifyIgnoringOtp(username, password);
  }

  @Override
  public void deleteOneTimePassword(String username) {
    identityClient.deleteOtpCredential(username);
  }

  @Override
  public OtpInfoDTO getOtpCredential(String username) {
    return identityClient.getOtpCredential(username);
  }

  @Override
  public boolean isEmailAvailableOrOwn(String username, String email) {
    var user = identityClient.findUserByEmail(email);

    return user.isEmpty()
        || user.get("encodedUsername").equals(username)
        || user.get("decodedUsername").equals(username);
  }
}
