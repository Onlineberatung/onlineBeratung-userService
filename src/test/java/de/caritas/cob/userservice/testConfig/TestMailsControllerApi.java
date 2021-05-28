package de.caritas.cob.userservice.testConfig;

import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

public class TestMailsControllerApi extends MailsControllerApi {

  public TestMailsControllerApi(ApiClient apiClient) {
    super(apiClient);
  }

  @Override
  public void sendErrorMail(ErrorMailDTO errorMailDTO) {
  }

  @Override
  public ResponseEntity<Void> sendErrorMailWithHttpInfo(ErrorMailDTO errorMailDTO)
      throws RestClientException {
    return ResponseEntity.ok().build();
  }

  @Override
  public void sendMails(MailsDTO mailsDTO) {
  }

  @Override
  public ResponseEntity<Void> sendMailsWithHttpInfo(MailsDTO mailsDTO) {
    return ResponseEntity.ok().build();
  }
}
