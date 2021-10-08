package de.caritas.cob.userservice.api.admin.service.agency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantAgencyAdminResultDTOBuilderTest {

  private static final int MOCKED_CONSULTANT_AGENCY_LIST_SIZE = 6;

  private List<de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO> agencyList;

  @Before
  public void setupResultPageMock() {
    EasyRandom easyRandom = new EasyRandom();
    agencyList = easyRandom
        .objects(
            de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO.class,
            MOCKED_CONSULTANT_AGENCY_LIST_SIZE)
        .collect(Collectors.toList());
  }

  @Test
  public void build_Should_returnEmptyAgencyAdminSearchResultDTO_When_noParametersAreSet() {
    var resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO, hasSize(0));
  }

  @Test
  public void build_Should_returnEmptyAgencyAdminSearchResultDTOWithContent_When_ParametersAreSet() {
    var resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .withResult(agencyList)
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO, hasSize(MOCKED_CONSULTANT_AGENCY_LIST_SIZE));
  }

  @Test
  public void build_Should_returnEmptyAgencyAdminSearchResultDTOWithTotal_When_ParametersAreSet() {
    var resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .withResult(agencyList)
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.size(), is(MOCKED_CONSULTANT_AGENCY_LIST_SIZE));
  }

}
