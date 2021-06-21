package de.caritas.cob.userservice.testConfig;

import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.config.apiclient.AgencyServiceApiClientConfig;
import de.caritas.cob.userservice.config.apiclient.MailServiceApiClientConfig;
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
  public MailServiceApiClientConfig mailServiceApiClientConfig() {
    return new MailServiceApiClientConfig() {
      @Override
      public MailsControllerApi mailsControllerApi(
          de.caritas.cob.userservice.mailservice.generated.ApiClient mailServiceApiClient) {
        return new TestMailsControllerApi(mailServiceApiClient);
      }
    };
  }
}