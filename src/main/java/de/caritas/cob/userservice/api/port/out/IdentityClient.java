package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import java.util.Map;
import java.util.Optional;

public interface IdentityClient {

  boolean changePassword(final String userId, final String password);

  KeycloakLoginResponseDTO loginUser(final String userName, final String password);

  boolean logoutUser(final String refreshToken);

  void changeEmailAddress(final String emailAddress);

  void changeEmailAddress(final String username, final String emailAddress);

  void deleteEmailAddress();

  OtpInfoDTO getOtpCredential(final String userName);

  boolean setUpOtpCredential(final String userName, final String initialCode, final String secret);

  void deleteOtpCredential(final String userName);

  Optional<String> initiateEmailVerification(final String username, final String email);

  Map<String, String> finishEmailVerification(final String username, final String initialCode);

  Map<String, String> findUserByEmail(String email);

  void deactivateUser(String userId);

  boolean verifyIgnoringOtp(String username, String password);
}
