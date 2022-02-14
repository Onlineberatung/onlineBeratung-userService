package de.caritas.cob.userservice.api.port.in;

import java.util.Map;
import java.util.Optional;

public interface IdentityManaging {

  Optional<String> setUpOneTimePassword(String username, String email);

  boolean setUpOneTimePassword(String username, String initialCode, String secret);

  Map<String, String> validateOneTimePassword(String username, String code);

  void deleteOneTimePassword(String username);
}
