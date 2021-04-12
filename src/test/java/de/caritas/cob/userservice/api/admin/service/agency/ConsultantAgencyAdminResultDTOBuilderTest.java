package de.caritas.cob.userservice.api.admin.service.agency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
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

  private List<ConsultantAgency> agencyList;

  @Before
  public void setupResultPageMock() {
    EasyRandom easyRandom = new EasyRandom();
    agencyList = easyRandom
        .objects(ConsultantAgency.class, MOCKED_CONSULTANT_AGENCY_LIST_SIZE)
        .collect(Collectors.toList());
  }

  @Test
  public void build_Should_returnEmptyConsultantAgencyAdminResultDTOWithDefaultSelfLink_When_noParametersAreSet() {
    ConsultantAgencyAdminResultDTO resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.getEmbedded(), hasSize(0));
    assertThat(resultDTO.getLinks(), notNullValue());
    assertThat(resultDTO.getLinks().getSelf(), notNullValue());
    assertThat(resultDTO.getLinks().getSelf().getHref(),
        is("/useradmin/consultants/{consultantId}/agencies"));
  }

  @Test
  public void build_Should_returnEmptyConsultantAgencyAdminResultDTOWithParameterizedSelfLink_When_ConsultantIdParameterIsSet() {
    ConsultantAgencyAdminResultDTO resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .withConsultantId("1da238c6-cd46-4162-80f1-bff74eafe77f")
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.getEmbedded(), hasSize(0));
    assertThat(resultDTO.getLinks(), notNullValue());
    assertThat(resultDTO.getLinks().getSelf(), notNullValue());
    assertThat(resultDTO.getLinks().getSelf().getHref(),
        is("/useradmin/consultants/1da238c6-cd46-4162-80f1-bff74eafe77f/agencies"));
  }

  @Test
  public void build_Should_returnEmptyConsultantAgencyAdminResultDTOWithContent_When_ParametersAreSet() {
    ConsultantAgencyAdminResultDTO resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .withConsultantId("1da238c6-cd46-4162-80f1-bff74eafeAAA")
        .withResult(agencyList)
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.getEmbedded(), hasSize(MOCKED_CONSULTANT_AGENCY_LIST_SIZE));
    assertThat(resultDTO.getEmbedded().get(0).getLinks().getDelete().getHref(), notNullValue());
    assertThat(resultDTO.getEmbedded().get(0).getLinks().getDelete().getMethod(),
        is(MethodEnum.DELETE));
    assertThat(resultDTO.getLinks(), notNullValue());
    assertThat(resultDTO.getLinks().getSelf(), notNullValue());
    assertThat(resultDTO.getLinks().getSelf().getHref(),
        is("/useradmin/consultants/1da238c6-cd46-4162-80f1-bff74eafeAAA/agencies"));
  }

  @Test
  public void build_Should_returnEmptyConsultantAgencyAdminResultDTOWithTotal_When_ParametersAreSet() {
    ConsultantAgencyAdminResultDTO resultDTO = ConsultantAgencyAdminResultDTOBuilder.getInstance()
        .withConsultantId("1da238c6-cd46-4162-80f1-bff74eafeAAA")
        .withResult(agencyList)
        .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.getTotal(), is(MOCKED_CONSULTANT_AGENCY_LIST_SIZE));
  }

}
