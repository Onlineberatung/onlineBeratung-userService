package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.helper.RequestHelper.getAuthorizedHttpHeaders;

import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for Keycloak 2FA calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTwoFactorAuthService {

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;
  private final IdentityConfig identityConfig;

  /**
   * Performs a Keycloak request to get the {@link OtpInfoDTO} for the 2FA.
   *
   * @param userName the username
   * @return an {link Optional} of {@link OtpInfoDTO}
   */
  public Optional<OtpInfoDTO> getOtpCredential(final String userName) {

    var httpHeaders =
        getAuthorizedHttpHeaders(this.keycloakAdminClientAccessor.getBearerToken(),
            MediaType.APPLICATION_FORM_URLENCODED);
    var requestUrl = identityConfig.getOtpInfoUrl() + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      ResponseEntity<OtpInfoDTO> response = restTemplate
          .exchange(requestUrl, HttpMethod.GET, request, OtpInfoDTO.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException ex) {
      log.error("Keycloak error: Could not fetch otp credential info", ex);
    }

    return Optional.empty();
  }

  /**
   * Performs a Keycloak request to set up the Two-Factor Authentication.
   *
   * @param userName    the username
   * @param otpSetupDTO the secret and code for the Two-Factor Authentication
   */
  public void setUpOtpCredential(final String userName, final OtpSetupDTO otpSetupDTO) {

    var httpHeaders =
        getAuthorizedHttpHeaders(this.keycloakAdminClientAccessor.getBearerToken(),
            MediaType.APPLICATION_JSON);
    var requestUrl = identityConfig.getOtpSetupUrl() + userName;
    var request = new HttpEntity<>(otpSetupDTO, httpHeaders);

    try {
      restTemplate.put(requestUrl, request, OtpInfoDTO.class);
    } catch (RestClientException ex) {
      throw new InternalServerErrorException(ex.getMessage(), ex,
          LogService::logInternalServerError);
    }
  }

  /**
   * Performs a Keycloak request to delete the two Factor Authentication.
   *
   * @param userName the username
   */
  public void deleteOtpCredential(final String userName) {
    var httpHeaders =
        getAuthorizedHttpHeaders(this.keycloakAdminClientAccessor.getBearerToken(),
            MediaType.APPLICATION_FORM_URLENCODED);
    var requestUrl = identityConfig.getOtpTeardownUrl() + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      restTemplate.exchange(requestUrl, HttpMethod.DELETE, request, Void.class);
    } catch (RestClientException ex) {
      throw new InternalServerErrorException(ex.getMessage(), ex,
          LogService::logInternalServerError);
    }
  }
}
