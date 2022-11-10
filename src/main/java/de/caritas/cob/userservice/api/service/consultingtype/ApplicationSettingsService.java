package de.caritas.cob.userservice.api.service.consultingtype;

import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.config.apiclient.ApplicationSettingsApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.applicationsettingsservice.generated.ApiClient;
import de.caritas.cob.userservice.applicationsettingsservice.generated.web.ApplicationsettingsControllerApi;
import de.caritas.cob.userservice.applicationsettingsservice.generated.web.model.ApplicationSettingsDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** Service class to communicate with the ConsultingTypeService. */
@Component
@RequiredArgsConstructor
public class ApplicationSettingsService {

  private final @NonNull ApplicationSettingsApiControllerFactory
      applicationSettingsApiControllerFactory;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  @Cacheable(value = CacheManagerConfig.APPLICATION_SETTINGS_CACHE)
  public ApplicationSettingsDTO getApplicationSettings() {
    ApplicationsettingsControllerApi controllerApi =
        applicationSettingsApiControllerFactory.createControllerApi();
    addDefaultHeaders(controllerApi.getApiClient());
    return controllerApi.getApplicationSettings();
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}
