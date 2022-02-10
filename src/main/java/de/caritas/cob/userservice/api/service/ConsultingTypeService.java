package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.config.CacheManagerConfig;
import de.caritas.cob.userservice.consultingtypeservice.generated.ApiClient;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service class to communicate with the ConsultingTypeService.
 */
@Component
@RequiredArgsConstructor
public class ConsultingTypeService {

  private final @NonNull ConsultingTypeControllerApi consultingTypeControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

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
    addDefaultHeaders(this.consultingTypeControllerApi.getApiClient());
    return this.consultingTypeControllerApi.getExtendedConsultingTypeById(consultingTypeId);
  }

  /**
   * Returns all existing consulting type ids. the id´s will be cached for further requests.
   *
   * @return list with consulting type ids
   */
  @Cacheable(cacheNames = CacheManagerConfig.CONSULTING_TYPE_CACHE)
  public List<Integer> getAllConsultingTypeIds(Long tenantContext) {
    addDefaultHeaders(this.consultingTypeControllerApi.getApiClient());
    return this.consultingTypeControllerApi.getBasicConsultingTypeList().stream()
        .map(BasicConsultingTypeResponseDTO::getId)
        .collect(Collectors.toList());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getCsrfHttpHeaders();
    addOriginHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  private void addOriginHeader(HttpHeaders headers) {
    String originHeaderValue = getOriginHeaderValue();
    if (originHeaderValue != null) {
      headers.add("origin", originHeaderValue);
    }
  }

  private String getOriginHeaderValue() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();

    return Collections.list(request.getHeaderNames())
        .stream()
        .collect(Collectors.toMap(h -> h, request::getHeader)).get("host");
  }
}
