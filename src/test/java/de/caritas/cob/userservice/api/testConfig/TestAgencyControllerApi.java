package de.caritas.cob.userservice.api.testConfig;

import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.FullAgencyResponseDTO;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.springframework.web.client.RestClientException;

public class TestAgencyControllerApi extends AgencyControllerApi {

  private static final EasyRandom easyRandom = new EasyRandom();

  public TestAgencyControllerApi(ApiClient apiClient) {
    super(apiClient);
  }

  @Override
  public List<FullAgencyResponseDTO> getAgencies(
      String postcode, Integer consultingType, Integer topicId, Integer age, String gender)
      throws RestClientException {
    return List.of(new FullAgencyResponseDTO());
  }

  @Override
  public List<AgencyResponseDTO> getAgenciesByConsultingType(Integer consultingTypeId)
      throws RestClientException {
    return List.of(new AgencyResponseDTO());
  }

  @Override
  public List<AgencyResponseDTO> getAgenciesByIds(List<Long> agencyIds) throws RestClientException {
    if (Objects.isNull(agencyIds)) {
      return List.of(new AgencyResponseDTO());
    }

    return agencyIds.stream()
        .map(
            id -> {
              var agencyResponseDto = easyRandom.nextObject(AgencyResponseDTO.class);
              agencyResponseDto.setId(id);
              agencyResponseDto.setConsultingType(1);
              return agencyResponseDto;
            })
        .collect(Collectors.toList());
  }
}
