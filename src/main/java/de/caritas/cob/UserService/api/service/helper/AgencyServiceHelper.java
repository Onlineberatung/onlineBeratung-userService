package de.caritas.cob.UserService.api.service.helper;

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
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.config.CachingConfig;

/**
 * 
 * Helper class to communicate with the AgencyService
 *
 */

@Component
public class AgencyServiceHelper {

  @Value("${agency.service.api.get.agency.data}")
  private String agencyServiceApiGetAgencyDataUrl;

  @Value("${agency.service.api.get.agencies}")
  private String agencyServiceApiGetAgenciesUrl;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ServiceHelper serviceHelper;

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency will be cached for further
   * requests.
   * 
   * @param agencyId
   * @return
   */
  @Cacheable(value = CachingConfig.AGENCY_CACHE, key = "#agencyId")
  public AgencyDTO getAgency(Long agencyId) {
    return getAgencyFromAgencyService(agencyId);
  }

  /**
   * Returns the {@link AgencyDTO} for the provided agencyId. Agency won't be cached for further
   * requests.
   * 
   * @param agencyId
   * @return
   */
  public AgencyDTO getAgencyWithoutCaching(Long agencyId) {
    return getAgencyFromAgencyService(agencyId);
  }

  private AgencyDTO getAgencyFromAgencyService(Long agencyId) {
    ResponseEntity<AgencyDTO> response = null;

    try {
      HttpHeaders header = serviceHelper.getCsrfHttpHeaders();
      HttpEntity<?> request = new HttpEntity<>(header);

      response = restTemplate.exchange(agencyServiceApiGetAgencyDataUrl + Long.toString(agencyId),
          HttpMethod.GET, request, AgencyDTO.class);

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
  public List<AgencyDTO> getAgenciesWithoutCaching(List<Long> agencyIds) {
    return getAgenciesFromAgencyService(agencyIds);
  }

  /**
   * @param agencyIds - List of ids
   * @return List<AgencyDTO> - List of {@link AgencyDTO}
   */
  private List<AgencyDTO> getAgenciesFromAgencyService(List<Long> agencyIds) {
    ResponseEntity<List<AgencyDTO>> response;
    String agencyIdsCommaSeperated = StringUtils.join(agencyIds, ",");
    try {
      HttpHeaders header = serviceHelper.getCsrfHttpHeaders();
      HttpEntity<?> request = new HttpEntity<>(header);
      response = restTemplate.exchange(agencyServiceApiGetAgenciesUrl + agencyIdsCommaSeperated,
          HttpMethod.GET, request, new ParameterizedTypeReference<List<AgencyDTO>>() {});
    } catch (Exception ex) {
      throw new AgencyServiceHelperException(ex);
    }
    return response.getBody();
  }

}
