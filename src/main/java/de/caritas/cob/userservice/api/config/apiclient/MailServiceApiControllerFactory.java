package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MailServiceApiControllerFactory {

  @Value("${mail.service.api.url}")
  private String mailServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public MailsControllerApi createControllerApi() {
    var apiClient = new ApiClient(restTemplate).setBasePath(this.mailServiceApiUrl);
    return new MailsControllerApi(apiClient);
  }
}
