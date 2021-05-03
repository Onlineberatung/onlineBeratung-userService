package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.FIRST_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.LAST_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.TEAM_CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.TEAM_SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatedUserHelperTest {

  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, USERNAME,
      FIRST_NAME, LAST_NAME, EMAIL, false, false, null, false, null, null, null, null, null, null);
  private final Consultant TEAM_CONSULTANT = new Consultant(TEAM_CONSULTANT_ID, ROCKETCHAT_ID,
      USERNAME, FIRST_NAME, LAST_NAME, EMAIL, false, true, null, true, null, null, null, null, null,
      null);
  private final Session SESSION = new Session(SESSION_ID, null, CONSULTANT, 0,
      POSTCODE, AGENCY_ID, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session SESSION_WITH_DIFFERENT_CONSULTANT =
      new Session(SESSION_ID, null, TEAM_CONSULTANT, 0, POSTCODE, AGENCY_ID,
          SessionStatus.NEW, nowInUtc(), null, null, null,
          false, false, null, null);
  private final Session TEAM_SESSION =
      new Session(TEAM_SESSION_ID, null, TEAM_CONSULTANT, 0, POSTCODE, AGENCY_ID,
          SessionStatus.IN_PROGRESS, nowInUtc(), null, null, null, IS_TEAM_SESSION, IS_MONITORING,
          null, null);
  private final Session TEAM_SESSION_WITH_DIFFERENT_CONSULTANT =
      new Session(TEAM_SESSION_ID, null, CONSULTANT, 0, POSTCODE, AGENCY_ID,
          SessionStatus.IN_PROGRESS, nowInUtc(), null, null, null, IS_TEAM_SESSION, IS_MONITORING,
          null, null);

  @InjectMocks
  private AuthenticatedUserHelper authenticatedUserHelper;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ConsultantAgencyService consultantAgencyService;

  @Test
  public void hasPermissionForSession_Should_ReturnFalse_WhenConsultantIsNotAssignedToSingleSession() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT.getId());

    boolean result =
        authenticatedUserHelper.hasPermissionForSession(SESSION_WITH_DIFFERENT_CONSULTANT);

    assertFalse(result);
  }

  @Test
  public void hasPermissionForSession_Should_ReturnFalse_WhenConsultantIsNotAssignedToAgencyOfTeamSessionOrToSession() {

    when(authenticatedUser.getUserId()).thenReturn(TEAM_CONSULTANT.getId());

    boolean result =
        authenticatedUserHelper.hasPermissionForSession(TEAM_SESSION_WITH_DIFFERENT_CONSULTANT);

    assertFalse(result);
  }

  @Test
  public void hasPermissionForSession_Should_ReturnTrue_WhenConsultantIsAssignedToSingleSession() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT.getId());

    boolean result = authenticatedUserHelper.hasPermissionForSession(SESSION);

    assertTrue(result);
  }

  @Test
  public void hasPermissionForSession_Should_ReturnTrue_WhenConsultantIsAssignedToAgencyOfTeamSession() {

    when(authenticatedUser.getUserId()).thenReturn(TEAM_CONSULTANT.getId());
    when(consultantAgencyService.isConsultantInAgency(TEAM_CONSULTANT.getId(),
        TEAM_SESSION.getAgencyId())).thenReturn(true);

    boolean result =
        authenticatedUserHelper.hasPermissionForSession(TEAM_SESSION_WITH_DIFFERENT_CONSULTANT);

    assertTrue(result);
  }

}
