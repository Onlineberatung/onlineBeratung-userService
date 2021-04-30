package de.caritas.cob.userservice.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.config.CachingConfig;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Service class to communicate with the AgencyService.
 */
@Component
@RequiredArgsConstructor
public class AgencyService {

  private final @NonNull AgencyControllerApi agencyControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency will be cached for further
   * requests.
   *
   * @param agencyId {@link AgencyDTO#getId()}
   * @return AgencyDTO {@link AgencyDTO}
   */
  @Cacheable(value = CachingConfig.AGENCY_CACHE, key = "#agencyId")
  public AgencyDTO getAgency(Long agencyId) {
    return getAgenciesFromAgencyService(Collections.singletonList(agencyId))
        .iterator()
        .next();
  }

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency won't be cached for further
   * requests.
   *
   * @param agencyId {@link AgencyDTO#getId()}
   * @return AgencyDTO {@link AgencyDTO}
   */
  public AgencyDTO getAgencyWithoutCaching(Long agencyId) {
    return getAgenciesFromAgencyService(Collections.singletonList(agencyId))
        .iterator()
        .next();
  }

  /**
   * Returns List of {@link AgencyDTO} for provided agencyIds. Agencies will be cached for further
   * requests.
   *
   * @param agencyIds List of {@link AgencyDTO#getId()}
   * @return List<AgencyDTO> List of {@link AgencyDTO}
   */
  @Cacheable(value = CachingConfig.AGENCY_CACHE, key = "#agencyIds")
  public List<AgencyDTO> getAgencies(List<Long> agencyIds) {
    return getAgenciesFromAgencyService(agencyIds);
  }

  /**
   * Returns List of {@link AgencyDTO} for provided agencyIds.
   *
   * @param agencyIds List of {@link AgencyDTO#getId()}
   * @return List<AgencyDTO> List of {@link AgencyDTO}
   */
  private List<AgencyDTO> getAgenciesFromAgencyService(List<Long> agencyIds) {
    addDefaultHeaders(this.agencyControllerApi.getApiClient());
    return this.agencyControllerApi.getAgenciesByIds(agencyIds).stream()
        .map(this::fromOriginalAgency)
        .collect(Collectors.toList());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  private AgencyDTO fromOriginalAgency(AgencyResponseDTO agencyResponseDTO) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper
          .readValue(objectMapper.writeValueAsString(agencyResponseDTO), AgencyDTO.class);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Model definition of agency in userservice does not "
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
