package de.caritas.cob.userservice.api.testConfig;

import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiClientConfig;
import de.caritas.cob.userservice.api.config.apiclient.MailServiceApiControllerFactory;
import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ApiControllerTestConfig {

  @Bean
  @Primary
  public AgencyServiceApiClientConfig agencyServiceApiClientConfig() {
    return new AgencyServiceApiClientConfig() {
      @Override
      public AgencyControllerApi agencyControllerApi(
          de.caritas.cob.userservice.agencyserivce.generated.ApiClient agencyServiceApiClient) {
        return new TestAgencyControllerApi(agencyServiceApiClient);
      }
    };
  }

  @Bean
  @Primary
  public MailServiceApiControllerFactory mailServiceApiControllerFactory() {
    return new MailServiceApiControllerFactory() {
      @Override
      public MailsControllerApi createControllerApi() {
        return new TestMailsControllerApi(new ApiClient());
      }
    };
  }
}
