package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.GRANTED_AUTHORIZATION_CONSULTANT_DEFAULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.HashSet;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantDataProviderTest {

  @Mock
  AgencyService agencyService;
  @Mock
  AuthenticatedUser authenticatedUser;
  @InjectMocks
  private ConsultantDataProvider consultantDataProvider;
  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @Mock
  private SessionRepository sessionRepository;


  @Test(expected = InternalServerErrorException.class)
  public void retrieveData_Should_ThrowInternalServerErrorException_When_NoAgenciesFound() {
    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getConsultantAgencies()).thenReturn(new HashSet<>());

    consultantDataProvider.retrieveData(consultant);
  }

  @Test
  public void retrieveData_Should_ReturnUserDataResponseDTOWithAgencyDTO_When_ProvidedWithCorrectConsultant() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(agencyService.getAgencies(any())).thenReturn(List.of(AGENCY_DTO_SUCHT));

    List<AgencyDTO> result = consultantDataProvider.retrieveData(CONSULTANT_WITH_AGENCY)
        .getAgencies();

    assertNotNull(result);
    assertEquals(AGENCY_DTO_SUCHT, result.get(0));
  }

  @Test
  public void retrieveData_Should_ReturnValidData() {
    when(agencyService.getAgencies(any())).thenReturn(List.of(AGENCY_DTO_SUCHT));

    when(authenticatedUser.getGrantedAuthorities())
        .thenReturn(asSet(GRANTED_AUTHORIZATION_CONSULTANT_DEFAULT));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.toString()));
    when(sessionRepository.countByConsultantAndStatusInAndRegistrationType(any(), any(),
        any())).thenReturn(5L);

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
    assertEquals(UserRole.CONSULTANT.toString(),
        result.getUserRoles().stream().findFirst().orElse(null));
    assertEquals(AGENCY_DTO_SUCHT, result.getAgencies().get(0));
    assertTrue(result.isHasArchive());
  }

  @Test
  public void retrieveData_Should_returnDataWithAnonymousConversationsTrue_When_consultantHasAtLeastOneConsultingTypeWithAnonymousConversationsAllowed() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.agencyService.getAgencies(any()))
        .thenReturn(List.of(new AgencyDTO().consultingType(1)));
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(new ExtendedConsultingTypeResponseDTO().isAnonymousConversationAllowed(true));

    var result = consultantDataProvider.retrieveData(consultant);

    assertThat(result.isHasAnonymousConversations(), is(true));
  }

  @Test
  public void retrieveData_Should_returnDataWithAnonymousConversationsFalse_When_allConsultingTypesHaveAnonymousConversationsDisabled() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.agencyService.getAgencies(any()))
        .thenReturn(List.of(new AgencyDTO().consultingType(1)));
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(new ExtendedConsultingTypeResponseDTO().isAnonymousConversationAllowed(false));

    var result = consultantDataProvider.retrieveData(consultant);

    assertThat(result.isHasAnonymousConversations(), is(false));
  }

  @Test
  public void retrieveData_Should_returnDataWithHasArchiveTrue_When_ConsultantHasRegisteredSessions() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.agencyService.getAgencies(any()))
        .thenReturn(List.of(new AgencyDTO().consultingType(1)));
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(new ExtendedConsultingTypeResponseDTO().isAnonymousConversationAllowed(false));
    when(sessionRepository.countByConsultantAndStatusInAndRegistrationType(any(), any(),
        any())).thenReturn(5L);

    var result = consultantDataProvider.retrieveData(consultant);

    assertTrue(result.isHasArchive());
  }

  @Test
  public void retrieveData_Should_returnDataWithHasArchiveFalse_When_ConsultantHasNoRegisteredSessions() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.agencyService.getAgencies(any()))
        .thenReturn(List.of(new AgencyDTO().consultingType(1)));
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(new ExtendedConsultingTypeResponseDTO().isAnonymousConversationAllowed(false));
    when(sessionRepository.countByConsultantAndStatusInAndRegistrationType(any(), any(),
        any())).thenReturn(0L);

    var result = consultantDataProvider.retrieveData(consultant);

    assertFalse(result.isHasArchive());
  }

}
