package de.caritas.cob.userservice.api.port.in;

import java.util.Optional;

public interface IdentityManaging {

  Optional<String> setUpOneTimePassword(String username, String email);

  void setUpOneTimePassword(String username, String initialCode, String secret);

  void deleteOneTimePassword(String username);
}
