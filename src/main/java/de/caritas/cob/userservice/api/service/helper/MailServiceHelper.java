package de.caritas.cob.userservice.api.service.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.api.model.mailService.MailsDTO;
import de.caritas.cob.userservice.api.service.LogService;

/**
 * 
 * Helper class to communicate with the MailService
 *
 */
@Service
public class MailServiceHelper {

  @Value("${mail.service.api.mails.send}")
  private String mailServiceApiSendMailUrl;

  @Autowired
  private ServiceHelper serviceHelper;

  @Autowired
  private RestTemplate restTemplate;

  /**
   * Send a email notification asynchron via the MailService
   * 
   * @param mailsDTO
   */
  public void sendEmailNotification(MailsDTO mailsDTO) {

    ResponseEntity<Void> response = null;

    try {
      HttpHeaders header = serviceHelper.getCsrfHttpHeaders();
      HttpEntity<MailsDTO> request = new HttpEntity<MailsDTO>(mailsDTO, header);

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
