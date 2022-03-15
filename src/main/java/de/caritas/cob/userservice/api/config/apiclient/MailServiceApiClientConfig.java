package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MailServiceApiClientConfig {

  @Value("${mail.service.api.url}")
  private String mailServiceApiUrl;

  @Bean
  public MailsControllerApi mailsControllerApi(ApiClient mailServiceApiClient) {
    return new MailsControllerApi(mailServiceApiClient);
  }

  @Bean
  @Primary
  public ApiClient mailServiceApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.mailServiceApiUrl);
  }

}
