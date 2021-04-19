package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.GRANTED_AUTHORIZATION_USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_WITH_SESSIONS;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.AgencyService;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AskerDataProviderTest {

  @InjectMocks
  private AskerDataProvider askerDataProvider;

  @Mock
  AgencyService agencyService;

  @Mock
  AuthenticatedUser authenticatedUser;

  @Mock
  SessionDataProvider sessionDataProvider;

  @Before
  public void setup() {
    setField(askerDataProvider, "emailDummySuffix", "@dummysuffix.de");
  }

  @Test
  public void retrieveData_Should_ReturnUserDataWithAgency_When_ProvidedWithUserWithAgencyInSession() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(agencyService.getAgencies(Mockito.anyList()))
        .thenReturn(Collections.singletonList(AGENCY_DTO_SUCHT));

    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> consultingTypeData =
        (LinkedHashMap<String, Object>) askerDataProvider.retrieveData(USER)
            .getConsultingTypes()
            .get(Integer.toString(AGENCY_DTO_SUCHT.getConsultingType().getValue()));
    AgencyDTO agency = (AgencyDTO) consultingTypeData.get("agency");

    assertEquals(AGENCY_DTO_SUCHT, agency);
  }

  @Test
  public void retrieveData_Should_ReturnUserDataWithAgency_When_ProvidedWithUserWithAgencies() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(agencyService.getAgencies(Mockito.anyList()))
        .thenReturn(Collections.singletonList(AGENCY_DTO_KREUZBUND));

    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> consultingTypeData =
        (LinkedHashMap<String, Object>) askerDataProvider.retrieveData(USER)
            .getConsultingTypes()
            .get(Integer.toString(AGENCY_DTO_KREUZBUND.getConsultingType().getValue()));
    AgencyDTO agency = (AgencyDTO) consultingTypeData.get("agency");

    assertEquals(AGENCY_DTO_KREUZBUND, agency);
  }

  @Test(expected = InternalServerErrorException.class)
  public void retrieveData_GetConsultingTypes_Should_ThrowInternalServerErrorException_When_AgencyServiceHelperFails() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(agencyService.getAgencies(Mockito.anyList()))
        .thenThrow(new InternalServerErrorException(""));

    askerDataProvider.retrieveData(USER);
  }

  @Test
  public void retrieveData_Should_ReturnUserDataResponseDTOWithValidEmail_When_ProvidedWithValidUser() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));

    UserDataResponseDTO resultUser = askerDataProvider.retrieveData(USER);

    assertNotNull(resultUser.getEmail());
    assertEquals(resultUser.getEmail(), USER.getEmail());
  }

  @Test
  public void retrieveData_Should_ReturnUserDataResponseDTOWithoutEmail_When_UserHasDummyMail() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    User user = mock(User.class);
    when(user.getEmail()).thenReturn("user@dummysuffix.de");

    UserDataResponseDTO resultUser = askerDataProvider.retrieveData(user);

    assertNull(resultUser.getEmail());
  }


  @Test
  public void retrieveData_Should_ReturnValidData() {
    when(agencyService.getAgencies(any())).thenReturn(
        Collections.singletonList(AGENCY_DTO_SUCHT));
    LinkedHashMap<String, Object> sessionData = new LinkedHashMap<>();
    sessionData.put("addictiveDrugs", "3");
    when(sessionDataProvider.getSessionDataMapFromSession(any())).thenReturn(sessionData);

    when(authenticatedUser.getGrantedAuthorities())
        .thenReturn(asSet(GRANTED_AUTHORIZATION_USER));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.toString()));

    UserDataResponseDTO result = askerDataProvider.retrieveData(USER_WITH_SESSIONS);

    assertEquals(USER_WITH_SESSIONS.getUserId(), result.getUserId());
    assertEquals(USER_WITH_SESSIONS.getUsername(), result.getUserName());
    assertEquals(USER_WITH_SESSIONS.getEmail(), result.getEmail());
    assertEquals(USER_WITH_SESSIONS.isLanguageFormal(), result.isFormalLanguage());
    assertFalse(result.isAbsent());
    assertEquals(CONSULTANT_WITH_AGENCY.isTeamConsultant(), result.isInTeamAgency());
    assertEquals(GRANTED_AUTHORIZATION_USER,
        result.getGrantedAuthorities().stream().findFirst().orElse(null));
    assertEquals(UserRole.USER.toString(), result.getUserRoles().stream().findFirst().orElse(null));
    for (ConsultingType consultingType : ConsultingType.values()) {
      LinkedHashMap<String, Object> consultingTypeEntry = (LinkedHashMap<String, Object>) result
          .getConsultingTypes().get(String.valueOf(consultingType.getValue()));
      if (consultingType.getValue() == ConsultingType.SUCHT.getValue()) {
        assertTrue((boolean) consultingTypeEntry.get("isRegistered"));
        assertEquals(AGENCY_DTO_SUCHT, consultingTypeEntry.get("agency"));
        assertEquals(sessionData, consultingTypeEntry.get("sessionData"));
      } else {
        assertFalse((boolean) consultingTypeEntry.get("isRegistered"));
      }
    }
  }

}
