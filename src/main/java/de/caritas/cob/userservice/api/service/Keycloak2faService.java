package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class Keycloak2faService {

  @Value("${2fa.user.enabled}")
  private Boolean isUser2faEnabled;

  @Value("${2fa.consultant.enabled}")
  private Boolean isConsultant2faEnabled;

  @Value("${keycloakApi.otp.setup.info}")
  private String keycloakOtpSetupInfo;

  @Value("${keycloakApi.otp.setup}")
  private String keycloakOtpSetup;

  @Value("${keycloakApi.otp.delete}")
  private String keycloakOtpDelete;

  private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
  private static final String HEADER_BEARER_KEY = "Bearer ";

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;

  public Boolean hasUserOtpCredential(final String userName) {
    var otpInfoDTO = getOtpCredential(userName);

    if (!Objects.isNull(otpInfoDTO) && !Objects.isNull(otpInfoDTO.getOtpSetup())) {
      return otpInfoDTO.getOtpSetup();
    }

    throw new RestClientException("The OTPInfoDTO is not Valid");
  }

  public OtpInfoDTO getOtpCredential(final String userName) {

    var httpHeaders = getAuthorizedFormHttpHeaders();
    var requestUrl = keycloakOtpSetupInfo + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      ResponseEntity<OtpInfoDTO> response = restTemplate
          .exchange(requestUrl, HttpMethod.GET, request,
              OtpInfoDTO.class);
      return response.getBody();
    } catch (RestClientException ex) {
      LogService.logKeycloakError("Could not fetch otp credential info", ex);
    }

    return null;
  }

  public boolean setUpOtpCredential(final String userName, final OtpSetupDTO otpSetupDTO) {

    var httpHeaders = getAuthorizedFormHttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    var requestUrl = keycloakOtpSetup + userName;
    var request = new HttpEntity<>(otpSetupDTO, httpHeaders);

    try {
      restTemplate.put(requestUrl, request, OtpInfoDTO.class);
      return true;
    } catch (RestClientException ex) {
      LogService.logKeycloakError("Could not set up otp credential", ex);
    }
    return false;
  }

  public boolean deleteOtpCredential(final String userName) {

    var httpHeaders = getAuthorizedFormHttpHeaders();
    var requestUrl = keycloakOtpDelete + userName;
    var request = new HttpEntity<>(httpHeaders);

    try {
      restTemplate.exchange(requestUrl, HttpMethod.DELETE, request, (Class<Object>) null);
      return true;
    } catch (RestClientException ex) {
      LogService.logKeycloakError("Could not delete otp credential ", ex);
    }
    return false;
  }

  private HttpHeaders getAuthorizedFormHttpHeaders() {
    var httpHeaders = getFormHttpHeaders();
    httpHeaders.add(HEADER_AUTHORIZATION_KEY,
        HEADER_BEARER_KEY + this.keycloakAdminClientAccessor.getBearerToken());

    return httpHeaders;
  }

  private HttpHeaders getFormHttpHeaders() {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    return httpHeaders;
  }

  public Boolean getUser2faEnabled() {
    return isUser2faEnabled;
  }

  public Boolean getConsultant2faEnabled() {
    return isConsultant2faEnabled;
  }
}
