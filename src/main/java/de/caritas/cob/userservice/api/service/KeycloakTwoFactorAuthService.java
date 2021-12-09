package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.helper.RequestHelper.getAuthorizedHttpHeaders;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@Service
@RequiredArgsConstructor
public class KeycloakTwoFactorAuthService {

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;

  @Value("${twoFactorAuth.user.enabled}")
  private Boolean isUserTwoFactorAuthEnabled;
  @Value("${twoFactorAuth.consultant.enabled}")
  private Boolean isConsultantTwoFactorAuthEnabled;
  @Value("${keycloakApi.otp.setup.info}")
  private String keycloakOtpSetupInfo;
  @Value("${keycloakApi.otp.setup}")
  private String keycloakOtpSetup;
  @Value("${keycloakApi.otp.delete}")
  private String keycloakOtpDelete;

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
    var requestUrl = keycloakOtpSetupInfo + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      ResponseEntity<OtpInfoDTO> response = restTemplate
          .exchange(requestUrl, HttpMethod.GET, request, OtpInfoDTO.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException ex) {
      LogService.logKeycloakError("Could not fetch otp credential info", ex);
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
    var requestUrl = keycloakOtpSetup + userName;
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
    var requestUrl = keycloakOtpDelete + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      restTemplate.exchange(requestUrl, HttpMethod.DELETE, request, Void.class);
    } catch (RestClientException ex) {
      throw new InternalServerErrorException(ex.getMessage(), ex,
          LogService::logInternalServerError);
    }
  }

  /**
   * Returns if if 2fa is enabled for users (askers).
   *
   * @return true, if 2fa is enabled for users (askers)
   */
  public Boolean getUserTwoFactorAuthEnabled() {
    return isUserTwoFactorAuthEnabled;
  }

  /**
   * Returns if if 2fa is enabled for consultants.
   *
   * @return true, if 2fa is enabled for consultants
   */
  public Boolean getConsultantTwoFactorAuthEnabled() {
    return isConsultantTwoFactorAuthEnabled;
  }
}
