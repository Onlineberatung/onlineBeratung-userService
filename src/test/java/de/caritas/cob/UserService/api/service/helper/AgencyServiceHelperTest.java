package de.caritas.cob.UserService.api.service.helper;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;

@RunWith(MockitoJUnitRunner.class)
public class AgencyServiceHelperTest {

  private final String FIELD_NAME_GET_AGENCY_API_URL = "agencyServiceApiGetAgencyDataUrl";
  private final String AGENCY_API_URL = "http://caritas.local/service/agencies/data/";
  private final Long AGENCY_ID = 1L;
  private final String AGENCY_NAME = "testagency";
  private final String POSTCODE = "12345";
  private final String DESCRIPTION = "testdescription";
  private final AgencyDTO AGENCY_DTO = new AgencyDTO(AGENCY_ID, AGENCY_NAME, POSTCODE, DESCRIPTION,
      false, false, ConsultingType.SUCHT);
  ResponseEntity<AgencyDTO> RESPONSE = new ResponseEntity<AgencyDTO>(AGENCY_DTO, HttpStatus.OK);
  private final String GET_AGENCY_METHOD_NAME = "getAgency";
  private final Class<?>[] GET_AGENCY_METHOD_PARAMS = new Class[] {Long.class};

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private ServiceHelper serviceHelper;
  @InjectMocks
  private AgencyServiceHelper agencyServiceHelper;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(agencyServiceHelper,
        agencyServiceHelper.getClass().getDeclaredField(FIELD_NAME_GET_AGENCY_API_URL),
        AGENCY_API_URL);
  }

  /**
   * 
   * Method getAgency
   * 
   **/

  @Test
  public void getAgency_Should_ReturnAgencyServiceHelperException_OnError() throws Exception {

    AgencyServiceHelperException exception = new AgencyServiceHelperException(new Exception());

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<AgencyDTO>>any()))
            .thenThrow(exception);

    try {
      agencyServiceHelper.getAgency(AGENCY_ID);
      fail("Expected exception: AgencyServiceHelperException");
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      assertTrue("Excepted AgencyServiceHelperException thrown", true);
    }

  }

  @Test
  public void getAgency_Should_ReturnAgencyDTO_WhenProvidedWithValidAgencyId() throws Exception {

    ResponseEntity<AgencyDTO> response = new ResponseEntity<AgencyDTO>(AGENCY_DTO, HttpStatus.OK);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<AgencyDTO>>any()))
            .thenReturn(response);

    assertThat(agencyServiceHelper.getAgency(AGENCY_ID), instanceOf(AgencyDTO.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void test_Should_Fail_WhenMethodgetAgencyFromAgencyServiceDoesNotHaveCacheableAnnotation()
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
  public void getAgencyWithoutCaching_Should_ReturnAgencyServiceHelperException_OnError()
      throws Exception {

    AgencyServiceHelperException exception = new AgencyServiceHelperException(new Exception());

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<AgencyDTO>>any()))
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

    ResponseEntity<AgencyDTO> response = new ResponseEntity<AgencyDTO>(AGENCY_DTO, HttpStatus.OK);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<AgencyDTO>>any()))
            .thenReturn(response);

    assertThat(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID), instanceOf(AgencyDTO.class));
  }

}
