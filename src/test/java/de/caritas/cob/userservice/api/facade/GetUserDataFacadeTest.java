package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.ValidatedUserAccountProvider;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class GetUserDataFacadeTest {

  @InjectMocks
  private GetUserDataFacade getUserDataFacade;
  @Mock
  AgencyServiceHelper agencyServiceHelper;
  @Mock
  Logger logger;
  @Mock
  SessionService sessionService;
  @Mock
  SessionDataHelper sessionDataHelper;
  @Mock
  AuthenticatedUser authenticatedUser;
  @Mock
  ValidatedUserAccountProvider accountProvider;

  @Before
  public void setup() {
    setField(getUserDataFacade, "emailDummySuffix", "@dummysuffix.de");
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test(expected = InternalServerErrorException.class)
  public void getConsultantData_Should_ThrowInternalServerErrorException_When_AgencyServiceHelperFails()
      throws Exception {

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(CONSULTANT_WITH_AGENCY);
    AgencyServiceHelperException agencyServiceHelperException =
        new AgencyServiceHelperException(new Exception());
    when(agencyServiceHelper.getAgency(AGENCY_ID)).thenThrow(agencyServiceHelperException);

    getUserDataFacade.buildUserDataPreferredByConsultantRole();
  }

  @Test
  public void getConsultantData_Should_ReturnUserDataResponseDTOWithAgencyDTO_When_ProvidedWithCorrectConsultant()
      throws AgencyServiceHelperException {

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(CONSULTANT_WITH_AGENCY);
    when(agencyServiceHelper.getAgency(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    assertThat(getUserDataFacade.buildUserDataPreferredByConsultantRole().getAgencies(),
        everyItem(instanceOf(AgencyDTO.class)));
  }

  @Test
  public void getUserData_Should_ReturnUserDataResponseDTO_When_ProvidedWithValidUser() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    assertThat(getUserDataFacade.buildUserDataPreferredByConsultantRole(),
        instanceOf(UserDataResponseDTO.class));
  }

  @Test
  public void getUserData_Should_ReturnUserDataWithAgency_When_ProvidedWithUserWithAgencyInSession()
      throws AgencyServiceHelperException {

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    when(agencyServiceHelper.getAgencies(Mockito.anyList()))
        .thenReturn(Collections.singletonList(AGENCY_DTO_SUCHT));

    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> consultingTypeData =
        (LinkedHashMap<String, Object>) getUserDataFacade.buildUserDataPreferredByConsultantRole()
            .getConsultingTypes()
            .get(Integer.toString(AGENCY_DTO_SUCHT.getConsultingType().getValue()));
    AgencyDTO agency = (AgencyDTO) consultingTypeData.get("agency");

    assertEquals(AGENCY_DTO_SUCHT, agency);
  }

  @Test
  public void getUserData_Should_ReturnUserDataWithAgency_When_ProvidedWithUserWithAgencies()
      throws AgencyServiceHelperException {

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    when(agencyServiceHelper.getAgencies(Mockito.anyList()))
        .thenReturn(Collections.singletonList(AGENCY_DTO_KREUZBUND));

    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> consultingTypeData =
        (LinkedHashMap<String, Object>) getUserDataFacade.buildUserDataPreferredByConsultantRole()
            .getConsultingTypes()
            .get(Integer.toString(AGENCY_DTO_KREUZBUND.getConsultingType().getValue()));
    AgencyDTO agency = (AgencyDTO) consultingTypeData.get("agency");

    assertEquals(AGENCY_DTO_KREUZBUND, agency);
  }

  @Test
  public void getUserData_GetConsultingTypes_Should_ThrowInternalServerErrorException_When_AgencyServiceHelperFails()
      throws Exception {

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    AgencyServiceHelperException agencyServiceHelperException =
        new AgencyServiceHelperException(new Exception());
    when(agencyServiceHelper.getAgencies(Mockito.anyList()))
        .thenThrow(agencyServiceHelperException);

    try {
      getUserDataFacade.buildUserDataPreferredByConsultantRole();
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
    verify(agencyServiceHelper, times(1)).getAgencies(Mockito.anyList());
  }

  @Test
  public void getUserData_Should_ReturnUserDataResponseDTOWithEmail_When_ProvidedWithValidUser() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);

    UserDataResponseDTO resultUser = getUserDataFacade.buildUserDataPreferredByConsultantRole();

    assertThat(resultUser.getEmail(), notNullValue());
  }

  @Test
  public void getUserData_Should_ReturnUserDataResponseDTOWithoutEmail_When_UserHasDummyMail() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    User user = mock(User.class);
    when(user.getEmail()).thenReturn("user@dummysuffix.de");
    when(accountProvider.retrieveValidatedUser()).thenReturn(user);

    UserDataResponseDTO resultUser = getUserDataFacade.buildUserDataPreferredByConsultantRole();

    assertThat(resultUser.getEmail(), nullValue());
  }

}
