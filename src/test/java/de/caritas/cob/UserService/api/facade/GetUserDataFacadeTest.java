package de.caritas.cob.UserService.api.facade;

import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_DTO_KREUZBUND;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_WITH_AGENCIES;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_WITH_SESSIONS;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.UserService.api.helper.SessionDataHelper;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.UserDataResponseDTO;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

@RunWith(MockitoJUnitRunner.class)
public class GetUserDataFacadeTest {

  @InjectMocks
  private GetUserDataFacade getUserDataFacade;
  @Mock
  AgencyServiceHelper agencyServiceHelper;
  @Mock
  LogService logService;
  @Mock
  SessionService sessionService;
  @Mock
  SessionDataHelper sessionDataHelper;

  @Test
  public void getConsultantData_Should_ReturnNullAndLogAgencyServiceHelperException_WhenAgencyServiceHelperFails()
      throws Exception {

    AgencyServiceHelperException agencyServiceHelperException =
        new AgencyServiceHelperException(new Exception());
    when(agencyServiceHelper.getAgency(AGENCY_ID)).thenThrow(agencyServiceHelperException);

    assertNull(getUserDataFacade.getConsultantData(CONSULTANT_WITH_AGENCY));
    verify(logService, times(1)).logAgencyServiceHelperException(Mockito.anyString(),
        Mockito.eq(agencyServiceHelperException));

  }

  @Test
  public void getConsultantData_Should_ReturnUserDataResponseDTOWithAgencyDTO_WhenProvidedWithCorrectConsultant() {

    when(agencyServiceHelper.getAgency(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    assertThat(getUserDataFacade.getConsultantData(CONSULTANT_WITH_AGENCY).getAgencies(),
        everyItem(instanceOf(AgencyDTO.class)));
  }

  @Test
  public void getUserData_Should_ReturnUserDataResponseDTO_WhenProvidedWithValidUser() {
    assertThat(getUserDataFacade.getUserData(USER_WITH_SESSIONS),
        instanceOf(UserDataResponseDTO.class));
  }

  @Test
  public void getUserData_Should_ReturnUserDataWithAgency_WhenProvidedWithUserWithAgencyInSession() {

    when(agencyServiceHelper.getAgencies(Mockito.anyList()))
        .thenReturn(Collections.singletonList(AGENCY_DTO_SUCHT));

    LinkedHashMap<String, Object> consultingTypeData = (LinkedHashMap<String, Object>) getUserDataFacade
        .getUserData(USER_WITH_SESSIONS)
        .getConsultingTypes()
        .get(Integer.toString(AGENCY_DTO_SUCHT.getConsultingType().getValue()));
    AgencyDTO agency = (AgencyDTO) consultingTypeData.get("agency");

    assertEquals(AGENCY_DTO_SUCHT, agency);
  }

  @Test
  public void getUserData_Should_ReturnUserDataWithAgency_WhenProvidedWithUserWithAgencies() {

    when(agencyServiceHelper.getAgencies(Mockito.anyList()))
        .thenReturn(Collections.singletonList(AGENCY_DTO_KREUZBUND));

    LinkedHashMap<String, Object> consultingTypeData = (LinkedHashMap<String, Object>) getUserDataFacade
        .getUserData(USER_WITH_AGENCIES)
        .getConsultingTypes()
        .get(Integer.toString(AGENCY_DTO_KREUZBUND.getConsultingType().getValue()));
    AgencyDTO agency = (AgencyDTO) consultingTypeData.get("agency");

    assertEquals(AGENCY_DTO_KREUZBUND, agency);
  }

  @Test
  public void getUserData_GetConsultingTypes_Should_ReturnNullAndLogAgencyServiceHelperException_WhenAgencyServiceHelperFails()
      throws Exception {

    AgencyServiceHelperException agencyServiceHelperException =
        new AgencyServiceHelperException(new Exception());
    when(agencyServiceHelper.getAgencies(Mockito.anyList()))
        .thenThrow(agencyServiceHelperException);

    assertNull(getUserDataFacade.getUserData(USER_WITH_SESSIONS).getConsultingTypes());
    verify(logService, times(1)).logAgencyServiceHelperException(Mockito.anyString(),
        Mockito.eq(agencyServiceHelperException));

  }

}
