package de.caritas.cob.userservice.testConfig;

import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.FullAgencyResponseDTO;
import java.util.List;
import org.springframework.web.client.RestClientException;

public class TestAgencyControllerApi extends AgencyControllerApi {

  public TestAgencyControllerApi(ApiClient apiClient) {
    super(apiClient);
  }

  @Override
  public List<FullAgencyResponseDTO> getAgencies(String postcode, Integer consultingType)
      throws RestClientException {
    return List.of(new FullAgencyResponseDTO());
  }

  @Override
  public List<AgencyResponseDTO> getAgenciesByConsultingType(Integer consultingTypeId)
      throws RestClientException {
    return List.of(new AgencyResponseDTO());
  }

  @Override
  public List<AgencyResponseDTO> getAgenciesByIds(List<Long> agencyIds)
      throws RestClientException {
    return List.of(new AgencyResponseDTO());
  }
}
