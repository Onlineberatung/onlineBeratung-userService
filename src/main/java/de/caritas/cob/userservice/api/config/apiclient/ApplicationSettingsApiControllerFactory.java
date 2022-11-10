package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.applicationsettingsservice.generated.web.ApplicationsettingsControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApplicationSettingsApiControllerFactory {

  @Value("${consulting.type.service.api.url}")
  private String applicationsettingsServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public ApplicationsettingsControllerApi createControllerApi() {
    var apiClient =
        new ApplicationSettingsApiClient(restTemplate)
            .setBasePath(this.applicationsettingsServiceApiUrl);
    return new ApplicationsettingsControllerApi(apiClient);
  }
}
