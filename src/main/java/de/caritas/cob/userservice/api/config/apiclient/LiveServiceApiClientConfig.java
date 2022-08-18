package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.liveservice.generated.ApiClient;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/** Configuration class for the LiveService API client. */
@Configuration
public class LiveServiceApiClientConfig {

  @Value("${live.service.api.url}")
  private String liveServiceApiUrl;

  /**
   * LiveService controller bean.
   *
   * @param liveServiceApiClient {@link ApiClient}
   * @return the LiveService controller {@link LiveControllerApi}
   */
  @Bean
  public LiveControllerApi liveControllerApi(ApiClient liveServiceApiClient) {
    return new LiveControllerApi(liveServiceApiClient);
  }

  /**
   * LiveService API client bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return the LiveService {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient liveServiceApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.liveServiceApiUrl);
  }
}
