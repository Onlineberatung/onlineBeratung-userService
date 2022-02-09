package de.caritas.cob.userservice.api.port.in;

public interface IdentityManaging {

  void setUpOneTimePassword(String username, String initialCode, String secret);

  void deleteOneTimePassword(String username);
}
