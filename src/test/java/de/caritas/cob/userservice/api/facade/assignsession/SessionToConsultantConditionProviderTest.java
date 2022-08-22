package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_DEBT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionToConsultantConditionProviderTest {

  private Session session;
  private Consultant consultant;

  @InjectMocks private SessionToConsultantConditionProvider sessionToConsultantConditionProvider;
  @Mock private AgencyService agencyService;

  @BeforeEach
  public void setup() {
    this.session = new Session();
    session.setId(1L);
    this.consultant = new Consultant();
    consultant.setId("id");
    consultant.setRocketChatId("rc id");
  }

  @Test
  void isSessionInProgress_Should_returnTrue_When_SessionIsInProgress() {
    session.setStatus(SessionStatus.IN_PROGRESS);
    boolean result = sessionToConsultantConditionProvider.isSessionInProgress(session);

    assertThat(result, is(true));
  }

  @Test
  void isSessionInProgress_Should_returnFalse_When_SessionIsNew() {
    session.setStatus(SessionStatus.NEW);
    boolean result = sessionToConsultantConditionProvider.isSessionInProgress(session);

    assertThat(result, is(false));
  }

  @Test
  void isSessionInProgress_Should_returnFalse_When_SessionIsNotSet() {
    boolean result = sessionToConsultantConditionProvider.isSessionInProgress(session);

    assertThat(result, is(false));
  }

  @Test
  void isNewSession_Should_returnTrue_When_SessionIsNew() {
    session.setStatus(SessionStatus.NEW);
    boolean result = sessionToConsultantConditionProvider.isNewSession(session);

    assertThat(result, is(true));
  }

  @Test
  void isNewSession_Should_returnFalse_When_SessionIsInProgress() {
    session.setStatus(SessionStatus.IN_PROGRESS);
    boolean result = sessionToConsultantConditionProvider.isNewSession(session);

    assertThat(result, is(false));
  }

  @Test
  void isNewSession_Should_returnFalse_When_SessionIsNotSet() {
    boolean result = sessionToConsultantConditionProvider.isNewSession(session);

    assertThat(result, is(false));
  }

  @Test
  void hasSessionNoConsultant_Should_returnTrue_When_SessionConsultantIsNull() {
    this.session.setConsultant(null);
    boolean result = sessionToConsultantConditionProvider.hasSessionNoConsultant(session);

    assertThat(result, is(true));
  }

  @Test
  void hasSessionNoConsultant_Should_returnTrue_When_SessionConsultantHasNoId() {
    consultant.setId("");
    this.session.setConsultant(consultant);
    boolean result = sessionToConsultantConditionProvider.hasSessionNoConsultant(session);

    assertThat(result, is(true));
  }

  @Test
  void hasSessionNoConsultant_Should_returnFalse_When_SessionConsultantHasId() {
    this.session.setConsultant(this.consultant);
    boolean result = sessionToConsultantConditionProvider.hasSessionNoConsultant(session);

    assertThat(result, is(false));
  }

  @Test
  void
      isSessionAlreadyAssignedToConsultant_Should_returnTrue_When_SessionIsInProgressAndHasConsultant() {
    this.session.setStatus(SessionStatus.IN_PROGRESS);
    this.session.setConsultant(this.consultant);
    boolean result =
        sessionToConsultantConditionProvider.isSessionAlreadyAssignedToConsultant(
            consultant, session);

    assertThat(result, is(true));
  }

  @Test
  void isSessionAlreadyAssignedToConsultant_Should_returnFalse_When_SessionIsNotInProgress() {
    this.session.setConsultant(this.consultant);
    boolean result =
        sessionToConsultantConditionProvider.isSessionAlreadyAssignedToConsultant(
            consultant, session);

    assertThat(result, is(false));
  }

  @Test
  void
      isSessionAlreadyAssignedToConsultant_Should_returnFalse_When_SessionIsInProgressAndHasOtherConsultant() {
    this.session.setStatus(SessionStatus.IN_PROGRESS);
    Consultant consultant = new Consultant();
    consultant.setId("other");
    this.session.setConsultant(consultant);
    boolean result =
        sessionToConsultantConditionProvider.isSessionAlreadyAssignedToConsultant(
            this.consultant, session);

    assertThat(result, is(false));
  }

  @Test
  void hasSessionUserNoRcId_Should_returnTrue_When_SessionHasUserWithoutRcId() {
    this.session.setUser(mock(User.class));
    boolean result = sessionToConsultantConditionProvider.hasSessionUserNoRcId(session);

    assertThat(result, is(true));
  }

  @Test
  void hasSessionUserNoRcId_Should_returnFalse_When_SessionHasNoUser() {
    boolean result = sessionToConsultantConditionProvider.hasSessionUserNoRcId(session);

    assertThat(result, is(false));
  }

  @Test
  void hasSessionUserNoRcId_Should_returnFalse_When_SessionHasUserWithRcId() {
    User userMock = mock(User.class);
    when(userMock.getRcUserId()).thenReturn("user id");
    this.session.setUser(userMock);
    boolean result = sessionToConsultantConditionProvider.hasSessionUserNoRcId(session);

    assertThat(result, is(false));
  }

  @Test
  void hasConsultantNoRcId_Should_returnTrue_When_ConsultantHasNoRcId() {
    consultant.setRocketChatId("");
    boolean result = sessionToConsultantConditionProvider.hasConsultantNoRcId(consultant);

    assertThat(result, is(true));
  }

  @Test
  void hasConsultantNoRcId_Should_returnFalse_When_ConsultantHasRcId() {
    consultant.setRocketChatId("rc id");
    boolean result = sessionToConsultantConditionProvider.hasConsultantNoRcId(consultant);

    assertThat(result, is(false));
  }

  @Test
  void
      isSessionsAgencyNotAvailableInConsultantAgencies_Should_returnTrue_When_ConsultantHasNoAgencies() {
    boolean result =
        sessionToConsultantConditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies(
            consultant, session);

    assertThat(result, is(true));
  }

  @Test
  void
      isSessionsAgencyNotAvailableInConsultantAgencies_Should_returnTrue_When_ConsultantAgenciesDoesNotContainSessionAgencyId() {
    session.setAgencyId(99L);
    consultant.setConsultantAgencies(
        asSet(
            new ConsultantAgency(
                1L, consultant, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
            new ConsultantAgency(
                2L, consultant, 2L, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
            new ConsultantAgency(
                3L, consultant, 3L, nowInUtc(), nowInUtc(), nowInUtc(), null, null)));
    boolean result =
        sessionToConsultantConditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies(
            consultant, session);

    assertThat(result, is(true));
  }

  @Test
  void
      isSessionsAgencyNotAvailableInConsultantAgencies_Should_returnFalse_When_ConsultantAgenciesContainSessionAgencyId() {
    session.setAgencyId(99L);
    consultant.setConsultantAgencies(
        asSet(
            new ConsultantAgency(
                1L, consultant, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
            new ConsultantAgency(
                2L, consultant, 99L, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
            new ConsultantAgency(
                3L, consultant, 3L, nowInUtc(), nowInUtc(), nowInUtc(), null, null)));
    boolean result =
        sessionToConsultantConditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies(
            consultant, session);

    assertThat(result, is(false));
  }

  @Test
  void
      isSessionsConsultingTypeNotAvailableForConsultant_Should_returnTrue_When_ConsultantHasNoAgencies() {
    boolean result =
        sessionToConsultantConditionProvider.isSessionsConsultingTypeNotAvailableForConsultant(
            consultant, session);

    assertThat(result, is(true));
  }

  @Test
  void
      isSessionsConsultingTypeNotAvailableForConsultant_Should_returnTrue_When_ConsultantAgenciesDoesNotContainSessionConsultingType() {
    session.setConsultingTypeId(CONSULTING_TYPE_ID_U25);
    AgencyDTO differentAgencyDTO = new AgencyDTO().consultingType(CONSULTING_TYPE_ID_SUCHT);
    ConsultantAgency differentConsultantAgency = mock(ConsultantAgency.class);
    whenAgencyServiceReturnsDTOForId(differentConsultantAgency, 1L, differentAgencyDTO);
    AgencyDTO otherAgencyDTO = new AgencyDTO().consultingType(CONSULTING_TYPE_ID_DEBT);
    ConsultantAgency otherConsultantAgency = mock(ConsultantAgency.class);
    whenAgencyServiceReturnsDTOForId(otherConsultantAgency, 2L, otherAgencyDTO);
    consultant.setConsultantAgencies(asSet(differentConsultantAgency, otherConsultantAgency));

    boolean result =
        sessionToConsultantConditionProvider.isSessionsConsultingTypeNotAvailableForConsultant(
            consultant, session);

    assertThat(result, is(true));
  }

  @Test
  void
      isSessionsConsultingTypeNotAvailableForConsultant_Should_returnFalse_When_ConsultantAgenciesContainSessionConsultingType() {
    session.setConsultingTypeId(CONSULTING_TYPE_ID_U25);
    AgencyDTO u25AgencyDTO = new AgencyDTO().consultingType(CONSULTING_TYPE_ID_U25);
    ConsultantAgency u25ConsultantAgency = mock(ConsultantAgency.class);
    when(agencyService.getAgency(any())).thenReturn(u25AgencyDTO);
    consultant.setConsultantAgencies(asSet(u25ConsultantAgency));

    boolean result =
        sessionToConsultantConditionProvider.isSessionsConsultingTypeNotAvailableForConsultant(
            consultant, session);

    assertThat(result, is(false));
  }

  private void whenAgencyServiceReturnsDTOForId(
      ConsultantAgency consultantAgency, long agencyId, AgencyDTO agencyDTO) {
    when(consultantAgency.getAgencyId()).thenReturn(agencyId);
    when(agencyService.getAgency(agencyId)).thenReturn(agencyDTO);
  }
}
