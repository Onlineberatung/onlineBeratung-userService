package de.caritas.cob.userservice.api.facade.assignsession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
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
  public void isSessionInProgress_Should_return_true_When_Session_is_in_progress() {
    session.setStatus(SessionStatus.IN_PROGRESS);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isSessionInProgress();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionInProgress_Should_return_false_When_Session_is_new() {
    session.setStatus(SessionStatus.NEW);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isSessionInProgress();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionInProgress_Should_return_false_When_Session_is_not_set() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isSessionInProgress();

    assertThat(result, is(false));
  }

  @Test
  public void isNewSession_Should_return_true_When_Session_is_new() {
    session.setStatus(SessionStatus.NEW);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isNewSession();

    assertThat(result, is(true));
  }

  @Test
  public void isNewSession_Should_return_false_When_Session_is_in_progress() {
    session.setStatus(SessionStatus.IN_PROGRESS);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isNewSession();

    assertThat(result, is(false));
  }

  @Test
  public void isNewSession_Should_return_false_When_Session_is_not_set() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).isNewSession();

    assertThat(result, is(false));
  }

  @Test
  public void hasSessionNoConsultant_Should_return_true_When_Session_consultant_is_null() {
    this.session.setConsultant(null);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).hasSessionNoConsultant();

    assertThat(result, is(true));
  }

  @Test
  public void hasSessionNoConsultant_Should_return_true_When_Session_consultant_has_no_id() {
    consultant.setId("");
    this.session.setConsultant(consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).hasSessionNoConsultant();

    assertThat(result, is(true));
  }

  @Test
  public void hasSessionNoConsultant_Should_return_false_When_Session_consultant_has_id() {
    this.session.setConsultant(this.consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant).hasSessionNoConsultant();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionAlreadyAssignedToConsultant_Should_return_true_When_Session_is_in_proress_and_has_consultant() {
    this.session.setStatus(SessionStatus.IN_PROGRESS);
    this.session.setConsultant(this.consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionAlreadyAssignedToConsultant();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionAlreadyAssignedToConsultant_Should_return_false_When_Session_is_not_in_proress() {
    this.session.setConsultant(this.consultant);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionAlreadyAssignedToConsultant();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionAlreadyAssignedToConsultant_Should_return_false_When_Session_is_in_proress_and_has_other_consultant() {
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
  public void hasSessionUserNoRcId_Should_return_true_When_Session_has_user_without_rc_id() {
    this.session.setUser(mock(User.class));
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasSessionUserNoRcId();

    assertThat(result, is(true));
  }

  @Test
  public void hasSessionUserNoRcId_Should_return_false_When_Session_has_no_user() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasSessionUserNoRcId();

    assertThat(result, is(false));
  }

  @Test
  public void hasSessionUserNoRcId_Should_return_false_When_Session_has_user_with_rc_id() {
    User userMock = mock(User.class);
    when(userMock.getRcUserId()).thenReturn("user id");
    this.session.setUser(userMock);
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasSessionUserNoRcId();

    assertThat(result, is(false));
  }

  @Test
  public void hasConsultantNoRcId_Should_return_true_When_Consultant_has_no_rc_id() {
    consultant.setRocketChatId("");
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasConsultantNoRcId();

    assertThat(result, is(true));
  }

  @Test
  public void hasConsultantNoRcId_Should_return_false_When_Consultant_has_rc_id() {
    consultant.setRocketChatId("rc id");
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .hasConsultantNoRcId();

    assertThat(result, is(false));
  }

  @Test
  public void isSessionsAgencyNotAvailableInConsultantAgencies_Should_return_true_When_Consultant_has_no_agencies() {
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionsAgencyNotAvailableInConsultantAgencies();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionsAgencyNotAvailableInConsultantAgencies_Should_return_true_When_Consultant_agencies_does_not_contain_session_agency_id() {
    session.setAgencyId(99L);
    consultant.setConsultantAgencies(asSet(
        new ConsultantAgency(1L, consultant, 1L),
        new ConsultantAgency(2L, consultant, 2L),
        new ConsultantAgency(3L, consultant, 3L)
    ));
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionsAgencyNotAvailableInConsultantAgencies();

    assertThat(result, is(true));
  }

  @Test
  public void isSessionsAgencyNotAvailableInConsultantAgencies_Should_return_false_When_Consultant_agencies_contain_session_agency_id() {
    session.setAgencyId(99L);
    consultant.setConsultantAgencies(asSet(
        new ConsultantAgency(1L, consultant, 1L),
        new ConsultantAgency(2L, consultant, 99L),
        new ConsultantAgency(3L, consultant, 3L)
    ));
    boolean result =
        new SessionToConsultantConditionProvider(session, consultant)
            .isSessionsAgencyNotAvailableInConsultantAgencies();

    assertThat(result, is(false));
  }

}
