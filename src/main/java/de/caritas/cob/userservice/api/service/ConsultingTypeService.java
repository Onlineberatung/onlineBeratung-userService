package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.config.apiclient.ConsultingTypeServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.consultingtypeservice.generated.ApiClient;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Service class to communicate with the ConsultingTypeService. */
@Component
@RequiredArgsConstructor
public class ConsultingTypeService {

  private final @NonNull ConsultingTypeServiceApiControllerFactory
      consultingTypeServiceApiControllerFactory;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  /**
   * Returns the {@link ExtendedConsultingTypeResponseDTO} for the provided consulting type ID. the
   * ExtendedConsultingTypeResponseDTO will be cached for further requests.
   *
   * @param consultingTypeId the consulting type ID for the extended consulting type response DTO
   * @return ExtendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   */
  @Cacheable(cacheNames = CacheManagerConfig.CONSULTING_TYPE_CACHE, key = "#consultingTypeId")
  public ExtendedConsultingTypeResponseDTO getExtendedConsultingTypeResponseDTO(
      int consultingTypeId) throws RestClientException {
    var consultingTypeControllerApi =
        consultingTypeServiceApiControllerFactory.createControllerApi();
    addDefaultHeaders(consultingTypeControllerApi.getApiClient());
    return consultingTypeControllerApi.getExtendedConsultingTypeById(consultingTypeId);
  }

  /**
   * Returns all existing consulting type ids. the id´s will be cached for further requests.
   *
   * @return list with consulting type ids
   */
  @Cacheable(cacheNames = CacheManagerConfig.CONSULTING_TYPE_CACHE)
  public List<Integer> getAllConsultingTypeIds(Long tenantId) {
    var consultingTypeControllerApi =
        consultingTypeServiceApiControllerFactory.createControllerApi();
    addDefaultHeaders(consultingTypeControllerApi.getApiClient());
    return consultingTypeControllerApi.getBasicConsultingTypeList().stream()
        .map(BasicConsultingTypeResponseDTO::getId)
        .collect(Collectors.toList());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}
