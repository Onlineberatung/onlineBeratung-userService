package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.messageservice.generated.ApiClient;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/** Configuration class for the MessageService API client. */
@Configuration
public class MessageServiceApiClientConfig {

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  /**
   * MessageService controller bean.
   *
   * @param messageServiceApiClient {@link ApiClient}
   * @return the MessageService controller {@link MessageControllerApi}
   */
  @Bean
  public MessageControllerApi messageControllerApi(ApiClient messageServiceApiClient) {
    return new MessageControllerApi(messageServiceApiClient);
  }

  /**
   * MessageService API client bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return the MessageService {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient messageServiceApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.messageServiceApiUrl);
  }
}
