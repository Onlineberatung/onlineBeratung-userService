package de.caritas.cob.userservice.api.service.agency;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** Service class to communicate with the AgencyService. */
@Component
@RequiredArgsConstructor
public class AgencyService {

  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;
  private final @NonNull AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;
  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency will be cached for further
   * requests.
   *
   * @param agencyId {@link AgencyDTO#getId()}
   * @return AgencyDTO {@link AgencyDTO}
   */
  @Cacheable(value = CacheManagerConfig.AGENCY_CACHE, key = "#agencyId")
  public AgencyDTO getAgency(Long agencyId) {
    return getAgenciesFromAgencyService(Collections.singletonList(agencyId)).iterator().next();
  }

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency won't be cached for further
   * requests.
   *
   * @param agencyId {@link AgencyDTO#getId()}
   * @return AgencyDTO {@link AgencyDTO}
   */
  public AgencyDTO getAgencyWithoutCaching(Long agencyId) {
    return getAgenciesFromAgencyService(Collections.singletonList(agencyId)).iterator().next();
  }

  /**
   * Returns List of {@link AgencyDTO} for provided agencyIds. Agencies will be cached for further
   * requests.
   *
   * @param agencyIds List of {@link AgencyDTO#getId()}
   * @return List<AgencyDTO> List of {@link AgencyDTO}
   */
  @Cacheable(value = CacheManagerConfig.AGENCY_CACHE, key = "#agencyIds")
  public List<AgencyDTO> getAgencies(List<Long> agencyIds) {
    return getAgenciesFromAgencyService(agencyIds);
  }

  public List<AgencyDTO> getAgenciesNotCached(List<Long> agencyIds) {
    return getAgenciesFromAgencyService(agencyIds);
  }

  /**
   * Returns List of {@link AgencyDTO} for provided agencyIds.
   *
   * @param agencyIds List of {@link AgencyDTO#getId()}
   * @return List<AgencyDTO> List of {@link AgencyDTO}
   */
  private List<AgencyDTO> getAgenciesFromAgencyService(List<Long> agencyIds) {
    if (isNotEmpty(agencyIds)) {
      AgencyControllerApi agencyControllerApi = this.getAgencyControllerApi();
      addDefaultHeaders(agencyControllerApi.getApiClient());
      return agencyControllerApi.getAgenciesByIds(agencyIds).stream()
          .map(this::fromOriginalAgency)
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  private AgencyControllerApi getAgencyControllerApi() {
    return agencyServiceApiControllerFactory.createControllerApi();
  }

  /**
   * Returns a list of {@link AgencyDTO} for the provided consulting type.
   *
   * @param consultingTypeId
   * @return List of {@link AgencyDTO}
   */
  public List<AgencyDTO> getAgenciesByConsultingType(int consultingTypeId) {
    var agencyControllerApi = getAgencyControllerApi();
    addDefaultHeaders(agencyControllerApi.getApiClient());
    return agencyControllerApi.getAgenciesByConsultingType(consultingTypeId).stream()
        .map(this::fromOriginalAgency)
        .collect(Collectors.toList());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  private AgencyDTO fromOriginalAgency(AgencyResponseDTO agencyResponseDTO) {
    var objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(
          objectMapper.writeValueAsString(agencyResponseDTO), AgencyDTO.class);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException(
          "Model definition of agency in userservice does not "
              + "match the definition of agencyservice");
    }
  }

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency won't be cached for further
   * requests.
   *
   * @param agencyIds the List of agency ids
   */
  public List<AgencyDTO> getAgenciesWithoutCaching(List<Long> agencyIds) {
    return getAgenciesFromAgencyService(agencyIds);
  }
}
