package de.caritas.cob.userservice.api.service.helper;

import de.caritas.cob.userservice.api.model.mailservice.MailsDTO;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Helper class to communicate with the MailService
 *
 */
@Service
@RequiredArgsConstructor
public class MailServiceHelper {

  @Value("${mail.service.api.mails.send}")
  private String mailServiceApiSendMailUrl;

  private final @NonNull ServiceHelper serviceHelper;
  private final @NonNull RestTemplate restTemplate;

  /**
   * Send a email notification asynchron via the MailService.
   * 
   * @param mailsDTO the transfer object to be handled in MailService
   */
  public void sendEmailNotification(MailsDTO mailsDTO) {

    ResponseEntity<Void> response = null;

    try {
      HttpHeaders header = serviceHelper.getCsrfHttpHeaders();
      HttpEntity<MailsDTO> request = new HttpEntity<>(mailsDTO, header);

      response =
          restTemplate.exchange(mailServiceApiSendMailUrl, HttpMethod.POST, request, Void.class);

    } catch (Exception ex) {
      LogService.logMailServiceHelperException("Error while calling the MailService", ex);
    }

    if (response != null && response.getStatusCode() != HttpStatus.OK) {
      LogService.logMailServiceHelperException(
          "Response status from MailService is not OK (200). Response-Status:"
              + response.getStatusCodeValue());
    }

  }

}
