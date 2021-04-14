package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_AGENCY_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_AGENCY_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.GRANTED_AUTHORIZATION_CONSULTANT_DEFAULT;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.AgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import org.apache.commons.collections.SetUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantDataProviderTest {

  @InjectMocks
  private ConsultantDataProvider consultantDataProvider;

  @Mock
  AgencyService agencyService;

  @Mock
  AuthenticatedUser authenticatedUser;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test(expected = InternalServerErrorException.class)
  public void retrieveData_Should_ThrowInternalServerErrorException_When_NoAgenciesFound() {
    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getConsultantAgencies()).thenReturn(SetUtils.EMPTY_SET);

    consultantDataProvider.retrieveData(consultant);
  }

  @Test
  public void retrieveData_Should_ReturnUserDataResponseDTOWithAgencyDTO_When_ProvidedWithCorrectConsultant() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(agencyService.getAgency(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    List<AgencyDTO> result = consultantDataProvider.retrieveData(CONSULTANT_WITH_AGENCY)
        .getAgencies();

    assertNotNull(result);
    assertEquals(AGENCY_DTO_SUCHT, result.get(0));
  }

  @Test
  public void retrieveData_Should_LogError_When_AgencyHelperCallFails() {
    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getConsultantAgencies())
        .thenReturn(asSet(CONSULTANT_AGENCY_2, CONSULTANT_AGENCY_3));
    when(agencyService.getAgency(CONSULTANT_AGENCY_2.getId())).thenThrow(new RuntimeException());
    when(agencyService.getAgency(CONSULTANT_AGENCY_3.getId())).thenReturn(AGENCY_DTO_SUCHT);

    consultantDataProvider.retrieveData(consultant);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void retrieveData_Should_ReturnValidData() {
    when(agencyService.getAgency(
        CONSULTANT_WITH_AGENCY.getConsultantAgencies().stream().findFirst().get().getId()))
        .thenReturn(AGENCY_DTO_SUCHT);

    when(authenticatedUser.getGrantedAuthorities())
        .thenReturn(asSet(GRANTED_AUTHORIZATION_CONSULTANT_DEFAULT));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.toString()));

    UserDataResponseDTO result = consultantDataProvider.retrieveData(CONSULTANT_WITH_AGENCY);

    assertEquals(CONSULTANT_WITH_AGENCY.getId(), result.getUserId());
    assertEquals(CONSULTANT_WITH_AGENCY.getUsername(), result.getUserName());
    assertEquals(CONSULTANT_WITH_AGENCY.getFirstName(), result.getFirstName());
    assertEquals(CONSULTANT_WITH_AGENCY.getLastName(), result.getLastName());
    assertEquals(CONSULTANT_WITH_AGENCY.getEmail(), result.getEmail());
    assertNull(result.getConsultingTypes());
    assertEquals(CONSULTANT_WITH_AGENCY.isLanguageFormal(), result.isFormalLanguage());
    assertEquals(CONSULTANT_WITH_AGENCY.isAbsent(), result.isAbsent());
    assertEquals(CONSULTANT_WITH_AGENCY.isTeamConsultant(), result.isInTeamAgency());
    assertEquals(GRANTED_AUTHORIZATION_CONSULTANT_DEFAULT,
        result.getGrantedAuthorities().stream().findFirst().orElse(null));
    assertEquals(UserRole.CONSULTANT.toString(), result.getUserRoles().stream().findFirst().orElse(null));
    assertEquals(AGENCY_DTO_SUCHT, result.getAgencies().get(0));
  }

}
