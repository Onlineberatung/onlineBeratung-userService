package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.helper.RequestHelper.getAuthorizedFormHttpHeaders;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
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

@Service
@RequiredArgsConstructor
public class KeycloakTwoFactorAuthService {

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

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;

  /**
   * Performs a Keycloak request to get the {@link OtpInfoDTO} for the two Factor Authentication
   *
   * @param userName the username
   * @return {@link OtpInfoDTO}
   */
  public Optional<OtpInfoDTO> getOtpCredential(final String userName) {

    var httpHeaders = getAuthorizedFormHttpHeaders(this.keycloakAdminClientAccessor.getBearerToken());
    var requestUrl = keycloakOtpSetupInfo + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      ResponseEntity<OtpInfoDTO> response = restTemplate
          .exchange(requestUrl, HttpMethod.GET, request,
              OtpInfoDTO.class);
      return Optional.of(response.getBody());
    } catch (RestClientException ex) {
      LogService.logKeycloakError("Could not fetch otp credential info", ex);
    }

    return Optional.empty();
  }

  /**
   * Performs a Keycloak request to set up the two Factor Authentication
   *
   * @param userName the username
   * @param otpSetupDTO the secret and code for the two Factor Authentication
   */
  public void setUpOtpCredential(final String userName, final OtpSetupDTO otpSetupDTO) {

    var httpHeaders = getAuthorizedFormHttpHeaders(this.keycloakAdminClientAccessor.getBearerToken());
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    var requestUrl = keycloakOtpSetup + userName;
    var request = new HttpEntity<>(otpSetupDTO, httpHeaders);

    try {
      restTemplate.put(requestUrl, request, OtpInfoDTO.class);
    } catch (RestClientException ex) {
      throw new BadRequestException("Could not set up otp credential");
    }
  }

  /**
   * Performs a Keycloak request to delete the two Factor Authentication
   *
   * @param userName the username
   */
  public void deleteOtpCredential(final String userName) {
    var httpHeaders = getAuthorizedFormHttpHeaders(this.keycloakAdminClientAccessor.getBearerToken());
    var requestUrl = keycloakOtpDelete + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      restTemplate.exchange(requestUrl, HttpMethod.DELETE, request, Void.class);
    } catch (RestClientException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  public Boolean getUserTwoFactorAuthEnabled() {
    return isUserTwoFactorAuthEnabled;
  }

  public Boolean getConsultantTwoFactorAuthEnabled() {
    return isConsultantTwoFactorAuthEnabled;
  }
}
