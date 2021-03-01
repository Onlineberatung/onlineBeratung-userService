package de.caritas.cob.userservice.api.service.helper;

import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * Service class to communicate with the MailService.
 */
@Service
@RequiredArgsConstructor
public class MailService {

  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull MailsControllerApi mailsControllerApi;

  /**
   * Send a email notification asynchron via the MailService.
   *
   * @param mailsDTO the transfer object to be handled in MailService
   */
  public void sendEmailNotification(MailsDTO mailsDTO) {
    HttpHeaders header = securityHeaderSupplier.getCsrfHttpHeaders();
    ApiClient apiClient = this.mailsControllerApi.getApiClient();
    header.forEach((name, value) -> apiClient.addDefaultHeader(name, value.iterator().next()));
    try {
      this.mailsControllerApi.sendMails(mailsDTO);
    } catch (Exception e) {
      LogService.logMailServiceException("Error while calling the MailService", e);
    }
  }

}
