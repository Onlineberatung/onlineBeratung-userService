package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakMapper;
import de.caritas.cob.userservice.api.port.in.IdentityManaging;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityManager implements IdentityManaging {

  private final KeycloakMapper keycloakMapper;

  private final IdentityClient keycloakService;

  @Override
  public void setUpOneTimePassword(String username, String initialCode, String secret) {
    var otpSetupDTO = keycloakMapper.otpSetupDtoOf(initialCode, secret);
    keycloakService.setUpOtpCredential(username, otpSetupDTO);
  }

  @Override
  public void deleteOneTimePassword(String username) {
    keycloakService.deleteOtpCredential(username);
  }
}
