package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID_LIST;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgencySecurityHeaderSupplierTest {

  private final String GET_AGENCY_METHOD_NAME = "getAgency";
  private final String GET_AGENCIES_METHOD_NAME = "getAgencies";
  private final Class<?>[] GET_AGENCY_METHOD_PARAMS = new Class[] {Long.class};
  private final Class<?>[] GET_AGENCIES_METHOD_PARAMS = new Class[] {List.class};

  @InjectMocks private AgencyService agencyService;

  @Mock private AgencyControllerApi agencyControllerApi;

  @Mock private SecurityHeaderSupplier securityHeaderSupplier;

  private List<AgencyResponseDTO> agencyResponseDTOS;

  @Mock private TenantHeaderSupplier tenantHeaderSupplier;

  @Mock private AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @BeforeEach
  void setup() throws NoSuchFieldException, SecurityException {
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);
    this.agencyResponseDTOS =
        AGENCY_DTO_LIST.stream().map(this::toAgencyResponseDTO).collect(Collectors.toList());
    when(this.securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(new HttpHeaders());
  }

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }

  @SneakyThrows
  private AgencyResponseDTO toAgencyResponseDTO(AgencyDTO agencyDTO) {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(
        objectMapper.writeValueAsString(agencyDTO), AgencyResponseDTO.class);
  }

  @Test
  void getAgencies_Should_ReturnAgencyDTOList_When_ProvidedWithValidAgencyIds() {
    when(agencyControllerApi.getAgenciesByIds(ArgumentMatchers.any()))
        .thenReturn(this.agencyResponseDTOS);

    assertThat(agencyService.getAgencies(AGENCY_ID_LIST).get(0), instanceOf(AgencyDTO.class));
    resetRequestAttributes();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void test_Should_Fail_When_MethodgetAgenciesFromAgencyServiceDoesNotHaveCacheableAnnotation()
      throws NoSuchMethodException, SecurityException {

    AgencyService agencyService =
        new AgencyService(
            mock(SecurityHeaderSupplier.class),
            mock(TenantHeaderSupplier.class),
            mock(AgencyServiceApiControllerFactory.class));
    Class classToTest = agencyService.getClass();
    Method methodToTest =
        classToTest.getMethod(GET_AGENCIES_METHOD_NAME, GET_AGENCIES_METHOD_PARAMS);
    Cacheable annotation = methodToTest.getAnnotation(Cacheable.class);

    assertNotNull(annotation);
  }

  @Test
  void getAgency_Should_ReturnAgencyDTO_When_ProvidedWithValidAgencyId() {

    when(agencyControllerApi.getAgenciesByIds(ArgumentMatchers.any()))
        .thenReturn(this.agencyResponseDTOS);

    assertThat(agencyService.getAgency(AGENCY_ID), instanceOf(AgencyDTO.class));
    resetRequestAttributes();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void test_Should_Fail_When_MethodgetAgencyFromAgencyServiceDoesNotHaveCacheableAnnotation()
      throws NoSuchMethodException, SecurityException {

    AgencyService agencyService =
        new AgencyService(
            mock(SecurityHeaderSupplier.class),
            mock(TenantHeaderSupplier.class),
            mock(AgencyServiceApiControllerFactory.class));
    Class classToTest = agencyService.getClass();
    Method methodToTest = classToTest.getMethod(GET_AGENCY_METHOD_NAME, GET_AGENCY_METHOD_PARAMS);
    Cacheable annotation = methodToTest.getAnnotation(Cacheable.class);

    assertNotNull(annotation);
    resetRequestAttributes();
  }

  @Test
  void getAgencyWithoutCaching_Should_ReturnAgencyDTO_WhenProvidedWithValidAgencyId() {
    when(agencyControllerApi.getAgenciesByIds(ArgumentMatchers.any()))
        .thenReturn(this.agencyResponseDTOS);

    assertThat(agencyService.getAgencyWithoutCaching(AGENCY_ID), instanceOf(AgencyDTO.class));
    resetRequestAttributes();
  }
}
