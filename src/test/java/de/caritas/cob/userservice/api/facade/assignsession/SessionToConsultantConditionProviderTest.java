package de.caritas.cob.userservice.api.facade.assignsession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

public class SessionToConsultantConditionProviderTest {

  private Session session;
  private Consultant consultant;

  @Before
  public void setup() {
    this.session = new Session();
    session.setId(1L);
    this.consultant = new Consultant();
    consultant.setId("id");
    consultant.setRocketChatId("rc id");
  }

  @Test
  public void isSessionInProgress_Should_returnTrue_When_SessionIsInProgress() {
    session.setStatus(SessionStatus.IN_PROGRESS);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isSessionInProgress();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionInProgress_Should_returnFalse_When_SessionIsNew() {
    session.setStatus(SessionStatus.NEW);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isSessionInProgress();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionInProgress_Should_returnFalse_When_SessionIsNotSet() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isSessionInProgress();

    assertThat(result, is(false));
  }

  @Test
  public void isNewSession_Should_returnTrue_When_SessionIsNew() {
    session.setStatus(SessionStatus.NEW);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isNewSession();

    assertThat(result, is(true));
  }

  @Test
  public void isNewSession_Should_returnFalse_When_SessionIsInProgress() {
    session.setStatus(SessionStatus.IN_PROGRESS);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isNewSession();

    assertThat(result, is(false));
  }

  @Test
  public void isNewSession_Should_returnFalse_When_SessionIsNotSet() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isNewSession();

    assertThat(result, is(false));
  }

  @Test
  public void hasSessionNoConsultant_Should_returnTrue_When_SessionConsultantIsNull() {
    this.session.setConsultant(null);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).hasSessionNoConsultant();

    assertThat(result, is(true));
  }

  @Test
  public void hasSessionNoConsultant_Should_returnTrue_When_SessionConsultantHasNoId() {
    consultant.setId("");
    this.session.setConsultant(consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).hasSessionNoConsultant();

    assertThat(result, is(true));
  }

  @Test
  public void hasSessionNoConsultant_Should_returnFalse_When_SessionConsultantHasId() {
    this.session.setConsultant(this.consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).hasSessionNoConsultant();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionAlreadyAssignedToConsultant_Should_returnTrue_When_SessionIsInProgressAndHasConsultant() {
    this.session.setStatus(SessionStatus.IN_PROGRESS);
    this.session.setConsultant(this.consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionAlreadyAssignedToConsultant();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionAlreadyAssignedToConsultant_Should_returnFalse_When_SessionIsNotInProgress() {
    this.session.setConsultant(this.consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionAlreadyAssignedToConsultant();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionAlreadyAssignedToConsultant_Should_returnFalse_When_SessionIsInProgressAndHasOtherConsultant() {
    this.session.setStatus(SessionStatus.IN_PROGRESS);
    Consultant consultant = new Consultant();
    consultant.setId("other");
    this.session.setConsultant(consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, this.consultant)
            .isSessionAlreadyAssignedToConsultant();

    assertThat(result, is(false));
  }

  @Test
  public void hasSessionUserNoRcId_Should_returnTrue_When_SessionHasUserWithoutRcId() {
    this.session.setUser(mock(User.class));
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasSessionUserNoRcId();

    assertThat(result, is(true));
  }

  @Test
  public void hasSessionUserNoRcId_Should_returnFalse_When_SessionHasNoUser() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasSessionUserNoRcId();

    assertThat(result, is(false));
  }

  @Test
  public void hasSessionUserNoRcId_Should_returnFalse_When_SessionHasUserWithRcId() {
    User userMock = mock(User.class);
    when(userMock.getRcUserId()).thenReturn("user id");
    this.session.setUser(userMock);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasSessionUserNoRcId();

    assertThat(result, is(false));
  }

  @Test
  public void hasConsultantNoRcId_Should_returnTrue_When_ConsultantHasNoRcId() {
    consultant.setRocketChatId("");
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasConsultantNoRcId();

    assertThat(result, is(true));
  }

  @Test
  public void hasConsultantNoRcId_Should_returnFalse_When_ConsultantHasRcId() {
    consultant.setRocketChatId("rc id");
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasConsultantNoRcId();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionsAgencyNotAvailableInConsultantAgencies_Should_returnTrue_When_ConsultantHasNoAgencies() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionsAgencyNotAvailableInConsultantAgencies();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionsAgencyNotAvailableInConsultantAgencies_Should_returnTrue_When_ConsultantAgenciesDoesNotContainSessionAgencyId() {
    session.setAgencyId(99L);
    consultant.setConsultantAgencies(asSet(
        new ConsultantAgency(1L, consultant, 1L, LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now()),
        new ConsultantAgency(2L, consultant, 2L, LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now()),
        new ConsultantAgency(3L, consultant, 3L, LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now())
    ));
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionsAgencyNotAvailableInConsultantAgencies();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionsAgencyNotAvailableInConsultantAgencies_Should_returnFalse_When_ConsultantAgenciesContainSessionAgencyId() {
    session.setAgencyId(99L);
    consultant.setConsultantAgencies(asSet(
        new ConsultantAgency(1L, consultant, 1L, LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now()),
        new ConsultantAgency(2L, consultant, 99L, LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now()),
        new ConsultantAgency(3L, consultant, 3L, LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now())
    ));
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionsAgencyNotAvailableInConsultantAgencies();

    assertThat(result, is(false));
  }

}
