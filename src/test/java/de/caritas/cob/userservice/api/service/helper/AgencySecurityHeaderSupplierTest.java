package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID_LIST;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.AgencyService;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;

@RunWith(MockitoJUnitRunner.class)
public class AgencySecurityHeaderSupplierTest {

  private final String FIELD_NAME_GET_AGENCY_API_URL = "agencyServiceApiGetAgenciesUrl";
  private final String AGENCIES_API_URL = "http://caritas.local/service/agencies/";
  private final String GET_AGENCY_METHOD_NAME = "getAgency";
  private final String GET_AGENCIES_METHOD_NAME = "getAgencies";
  private final Class<?>[] GET_AGENCY_METHOD_PARAMS = new Class[]{Long.class};
  private final Class<?>[] GET_AGENCIES_METHOD_PARAMS = new Class[]{List.class};

  @InjectMocks
  private AgencyService agencyService;

  @Mock
  private AgencyControllerApi agencyControllerApi;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  private List<AgencyResponseDTO> agencyResponseDTOS;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    this.agencyResponseDTOS = AGENCY_DTO_LIST.stream()
        .map(this::toAgencyResponseDTO)
        .collect(Collectors.toList());
    when(this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(new HttpHeaders());
  }

  @SneakyThrows
  private AgencyResponseDTO toAgencyResponseDTO(AgencyDTO agencyDTO) {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(objectMapper.writeValueAsString(agencyDTO),
        AgencyResponseDTO.class);
  }

  @Test
  public void getAgencies_Should_ReturnAgencyDTOList_When_ProvidedWithValidAgencyIds() {
    when(agencyControllerApi.getAgenciesByIds(ArgumentMatchers.any()))
        .thenReturn(this.agencyResponseDTOS);

    assertThat(agencyService.getAgencies(AGENCY_ID_LIST).get(0), instanceOf(AgencyDTO.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void test_Should_Fail_When_MethodgetAgenciesFromAgencyServiceDoesNotHaveCacheableAnnotation()
      throws NoSuchMethodException, SecurityException {

    AgencyService agencyService = new AgencyService(mock(AgencyControllerApi.class),
        mock(SecurityHeaderSupplier.class));
    Class classToTest = agencyService.getClass();
    Method methodToTest = classToTest
        .getMethod(GET_AGENCIES_METHOD_NAME, GET_AGENCIES_METHOD_PARAMS);
    Cacheable annotation = methodToTest.getAnnotation(Cacheable.class);

    assertNotNull(annotation);
  }

  @Test
  public void getAgency_Should_ReturnAgencyDTO_When_ProvidedWithValidAgencyId() {
    when(agencyControllerApi.getAgenciesByIds(ArgumentMatchers.any()))
        .thenReturn(this.agencyResponseDTOS);

    assertThat(agencyService.getAgency(AGENCY_ID), instanceOf(AgencyDTO.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void test_Should_Fail_When_MethodgetAgencyFromAgencyServiceDoesNotHaveCacheableAnnotation()
      throws NoSuchMethodException, SecurityException {

    AgencyService agencyService = new AgencyService(mock(AgencyControllerApi.class),
        mock(SecurityHeaderSupplier.class));
    Class classToTest = agencyService.getClass();
    Method methodToTest = classToTest.getMethod(GET_AGENCY_METHOD_NAME, GET_AGENCY_METHOD_PARAMS);
    Cacheable annotation = methodToTest.getAnnotation(Cacheable.class);

    assertNotNull(annotation);
  }

  @Test
  public void getAgencyWithoutCaching_Should_ReturnAgencyDTO_WhenProvidedWithValidAgencyId() {
    when(agencyControllerApi.getAgenciesByIds(ArgumentMatchers.any()))
        .thenReturn(this.agencyResponseDTOS);

    assertThat(agencyService.getAgencyWithoutCaching(AGENCY_ID), instanceOf(AgencyDTO.class));
  }

}
