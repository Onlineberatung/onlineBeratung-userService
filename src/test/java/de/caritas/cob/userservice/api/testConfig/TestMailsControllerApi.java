package de.caritas.cob.userservice.api.testConfig;

import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

@Slf4j
public class TestMailsControllerApi extends MailsControllerApi {

  public TestMailsControllerApi(ApiClient apiClient) {
    super(apiClient);
  }

  @Override
  public void sendErrorMail(ErrorMailDTO errorMailDTO) {}

  @Override
  public ResponseEntity<Void> sendErrorMailWithHttpInfo(ErrorMailDTO errorMailDTO)
      throws RestClientException {
    return ResponseEntity.ok().build();
  }

  @Override
  public void sendMails(MailsDTO mailsDTO) {
    var size = mailsDTO.getMails().size();
    log.info("Sending {} emails", size);
    if (size > 0) {
      var firstEmail = mailsDTO.getMails().get(0);
      log.info("Sending {} email to {}", firstEmail.getTemplate(), firstEmail.getEmail());
    }
  }

  @Override
  public ResponseEntity<Void> sendMailsWithHttpInfo(MailsDTO mailsDTO) {
    return ResponseEntity.ok().build();
  }
}
