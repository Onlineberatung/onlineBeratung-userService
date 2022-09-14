package de.caritas.cob.userservice.api.port.in;

import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import java.util.Map;
import java.util.Optional;

public interface IdentityManaging {

  Optional<String> setUpOneTimePassword(String username, String email);

  boolean setUpOneTimePassword(String username, String initialCode, String secret);

  Map<String, String> validateOneTimePassword(String username, String code);

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  boolean validatePasswordIgnoring2fa(String username, String password);

  boolean changePassword(String userId, String password);

  void changeLanguage(String userId, String locale);

  void deleteOneTimePassword(String username);

  OtpInfoDTO getOtpCredential(String username);

  boolean isEmailAvailableOrOwn(String username, String email);

  boolean canViewPeerSessions(String consultantId);

  boolean canViewFeedbackSessions(String toString);
}
