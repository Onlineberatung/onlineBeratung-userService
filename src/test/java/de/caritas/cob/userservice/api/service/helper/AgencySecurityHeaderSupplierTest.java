package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID_LIST;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class AgencySecurityHeaderSupplierTest {

  private final String FIELD_NAME_GET_AGENCY_API_URL = "agencyServiceApiGetAgenciesUrl";
  private final String AGENCIES_API_URL = "http://caritas.local/service/agencies/";
  private final String GET_AGENCY_METHOD_NAME = "getAgency";
  private final String GET_AGENCIES_METHOD_NAME = "getAgencies";
  private final Class<?>[] GET_AGENCY_METHOD_PARAMS = new Class[]{Long.class};
  private final Class<?>[] GET_AGENCIES_METHOD_PARAMS = new Class[]{List.class};

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;
  @InjectMocks
  private AgencyServiceHelper agencyServiceHelper;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(agencyServiceHelper,
        agencyServiceHelper.getClass().getDeclaredField(FIELD_NAME_GET_AGENCY_API_URL),
        AGENCIES_API_URL);
  }

  /**
   * Method getAgencies
   **/

  @Test
  public void getAgencies_Should_ReturnAgencyServiceHelperException_OnError() {

    Exception exception = new RuntimeException(new Exception());

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        ArgumentMatchers.<ParameterizedTypeReference<List<AgencyDTO>>>any()))
        .thenThrow(exception);

    try {
      agencyServiceHelper.getAgencies(AGENCY_ID_LIST);
      fail("Expected exception: AgencyServiceHelperException");
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      assertTrue("Excepted AgencyServiceHelperException thrown", true);
    }

  }

  @Test
  public void getAgencies_Should_ReturnAgencyDTOList_When_ProvidedWithValidAgencyIds()
      throws Exception {

    ResponseEntity<List<AgencyDTO>> response = new ResponseEntity<>(AGENCY_DTO_LIST,
        HttpStatus.OK);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        ArgumentMatchers.<ParameterizedTypeReference<List<AgencyDTO>>>any()))
        .thenReturn(response);

    assertThat(agencyServiceHelper.getAgencies(AGENCY_ID_LIST).get(0), instanceOf(AgencyDTO.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void test_Should_Fail_When_MethodgetAgenciesFromAgencyServiceDoesNotHaveCacheableAnnotation()
      throws NoSuchMethodException, SecurityException {

    AgencyServiceHelper agencyServiceHelper = new AgencyServiceHelper();
    Class classToTest = agencyServiceHelper.getClass();
    Method methodToTest = classToTest
        .getMethod(GET_AGENCIES_METHOD_NAME, GET_AGENCIES_METHOD_PARAMS);
    Cacheable annotation = methodToTest.getAnnotation(Cacheable.class);

    assertNotNull(annotation);
  }

  /**
   * Method getAgency
   **/

  @Test
  public void getAgency_Should_ReturnAgencyDTO_When_ProvidedWithValidAgencyId() throws Exception {

    ResponseEntity<List<AgencyDTO>> response = new ResponseEntity<>(AGENCY_DTO_LIST,
        HttpStatus.OK);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(),
        ArgumentMatchers.<ParameterizedTypeReference<List<AgencyDTO>>>any()))
        .thenReturn(response);

    assertThat(agencyServiceHelper.getAgency(AGENCY_ID), instanceOf(AgencyDTO.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void test_Should_Fail_When_MethodgetAgencyFromAgencyServiceDoesNotHaveCacheableAnnotation()
      throws NoSuchMethodException, SecurityException {

    AgencyServiceHelper agencyServiceHelper = new AgencyServiceHelper();
    Class classToTest = agencyServiceHelper.getClass();
    Method methodToTest = classToTest.getMethod(GET_AGENCY_METHOD_NAME, GET_AGENCY_METHOD_PARAMS);
    Cacheable annotation = methodToTest.getAnnotation(Cacheable.class);

    assertNotNull(annotation);
  }

  /**
   * 
   * Method getAgencyWithoutCaching
   * 
   **/

  @Test
  public void getAgencyWithoutCaching_Should_ReturnAgencyServiceHelperException_OnError() {

    Exception exception = new RuntimeException(new Exception());

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(),
        ArgumentMatchers.<ParameterizedTypeReference<List<AgencyDTO>>>any()))
        .thenThrow(exception);

    try {
      agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID);
      fail("Expected exception: AgencyServiceHelperException");
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      assertTrue("Excepted AgencyServiceHelperException thrown", true);
    }

  }

  @Test
  public void getAgencyWithoutCaching_Should_ReturnAgencyDTO_WhenProvidedWithValidAgencyId()
      throws Exception {

    ResponseEntity<List<AgencyDTO>> response = new ResponseEntity<>(AGENCY_DTO_LIST,
        HttpStatus.OK);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(),
        ArgumentMatchers.<ParameterizedTypeReference<List<AgencyDTO>>>any()))
        .thenReturn(response);

    assertThat(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID), instanceOf(AgencyDTO.class));
  }

}
