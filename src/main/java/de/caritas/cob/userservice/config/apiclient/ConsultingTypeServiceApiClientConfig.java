package de.caritas.cob.userservice.config.apiclient;

import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for the ConsultingTypeService API client.
 */
@Component
public class ConsultingTypeServiceApiClientConfig {

  @Value("${consulting.type.service.api.url}")
  private String consultingTypeServiceApiUrl;

  /**
   * ConsultingTypeService controller bean.
   *
   * @param apiClient {@link ApiClient}
   * @return the ConsultingTypeService controller {@link ConsultingTypeControllerApi}
   */
  @Bean
  public ConsultingTypeControllerApi consultingTypeControllerApi(ApiClient apiClient) {
    return new ConsultingTypeControllerApi(apiClient);
  }

  /**
   * ConsultingTypeService API client bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return the ConsultingTypeService {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient consultingTypeApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.consultingTypeServiceApiUrl);
  }

}
