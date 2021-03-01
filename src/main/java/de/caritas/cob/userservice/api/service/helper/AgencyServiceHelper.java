package de.caritas.cob.userservice.api.service.helper;

import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.config.CachingConfig;

/**
 * Helper class to communicate with the AgencyService
 */

@Component
public class AgencyServiceHelper {

  @Value("${agency.service.api.get.agencies}")
  private String agencyServiceApiGetAgenciesUrl;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private SecurityHeaderSupplier securityHeaderSupplier;

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency will be cached for further
   * requests.
   *
   * @param agencyId {@link AgencyDTO#getId()}
   * @return AgencyDTO {@link AgencyDTO}
   */
  @Cacheable(value = CachingConfig.AGENCY_CACHE, key = "#agencyId")
  public AgencyDTO getAgency(Long agencyId) throws AgencyServiceHelperException {
    return getAgenciesFromAgencyService(Collections.singletonList(agencyId)).get(0);
  }

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency won't be cached for further
   * requests.
   *
   * @param agencyId {@link AgencyDTO#getId()}
   * @return AgencyDTO {@link AgencyDTO}
   */
  public AgencyDTO getAgencyWithoutCaching(Long agencyId) throws AgencyServiceHelperException {
    return getAgenciesFromAgencyService(Collections.singletonList(agencyId)).get(0);
  }

  /**
   * Returns List of {@link AgencyDTO} for provided agencyIds. Agencies will be cached for further
   * requests.
   *
   * @param agencyIds List of {@link AgencyDTO#getId()}
   * @return List<AgencyDTO> List of {@link AgencyDTO}
   */
  @Cacheable(value = CachingConfig.AGENCY_CACHE, key = "#agencyIds")
  public List<AgencyDTO> getAgencies(List<Long> agencyIds) throws AgencyServiceHelperException {
    return getAgenciesFromAgencyService(agencyIds);
  }

  /**
   * Returns List of {@link AgencyDTO} for provided agencyIds.
   *
   * @param agencyIds List of {@link AgencyDTO#getId()}
   * @return List<AgencyDTO> List of {@link AgencyDTO}
   */
  private List<AgencyDTO> getAgenciesFromAgencyService(List<Long> agencyIds)
      throws AgencyServiceHelperException {
    ResponseEntity<List<AgencyDTO>> response;
    String agencyIdsCommaSeparated = StringUtils.join(agencyIds, ",");

    try {
      HttpHeaders header = securityHeaderSupplier.getCsrfHttpHeaders();
      HttpEntity<?> request = new HttpEntity<>(header);

      response = restTemplate.exchange(agencyServiceApiGetAgenciesUrl + agencyIdsCommaSeparated,
          HttpMethod.GET, request, new ParameterizedTypeReference<List<AgencyDTO>>() {});

    } catch (Exception ex) {
      throw new AgencyServiceHelperException(ex);
    }

    return response.getBody();
  }

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency won't be cached for further
   * requests.
   *
   * @param agencyIds the List of agency ids
   * @return
   */
  public List<AgencyDTO> getAgenciesWithoutCaching(List<Long> agencyIds)
      throws AgencyServiceHelperException {
    return getAgenciesFromAgencyService(agencyIds);
  }
}
