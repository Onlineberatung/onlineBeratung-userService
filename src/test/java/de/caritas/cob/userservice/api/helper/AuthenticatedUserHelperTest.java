package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FIRST_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.LAST_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.TEAM_CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.TEAM_SESSION_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatedUserHelperTest {

  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, USERNAME,
      FIRST_NAME, LAST_NAME, EMAIL, false, false, null, false, null, null, null, null,
      null, null, null, null, true, null, ConsultantStatus.CREATED);
  private final Consultant TEAM_CONSULTANT = new Consultant(TEAM_CONSULTANT_ID, ROCKETCHAT_ID,
      USERNAME, FIRST_NAME, LAST_NAME, EMAIL, false, true, null, true, null, null, null,
      null, null, null, null, null, true, null, ConsultantStatus.CREATED);
  private final Session SESSION = new Session(SESSION_ID, null, CONSULTANT,
      CONSULTING_TYPE_ID_SUCHT, REGISTERED, POSTCODE, AGENCY_ID, null, SessionStatus.NEW,
      nowInUtc(), null, null, null, false, false, false, nowInUtc(), null, null);
  private final Session SESSION_WITH_DIFFERENT_CONSULTANT =
      new Session(SESSION_ID, null, TEAM_CONSULTANT, CONSULTING_TYPE_ID_SUCHT, REGISTERED, POSTCODE,
          AGENCY_ID, null, SessionStatus.NEW, nowInUtc(), null, null, null, false, false, false,
          nowInUtc(), null, null);
  private final Session TEAM_SESSION =
      new Session(TEAM_SESSION_ID, null, TEAM_CONSULTANT, CONSULTING_TYPE_ID_SUCHT, REGISTERED,
          POSTCODE, AGENCY_ID, null, SessionStatus.IN_PROGRESS, nowInUtc(), null, null, null,
          IS_TEAM_SESSION, IS_MONITORING, false, nowInUtc(), null, null);
  private final Session TEAM_SESSION_WITH_DIFFERENT_CONSULTANT =
      new Session(TEAM_SESSION_ID, null, CONSULTANT, CONSULTING_TYPE_ID_SUCHT, REGISTERED, POSTCODE,
          AGENCY_ID, null, SessionStatus.IN_PROGRESS, nowInUtc(), null, null, null, IS_TEAM_SESSION,
          IS_MONITORING, false, nowInUtc(), null, null);

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

  @Test
  public void authenticatedUserRolesContainAnyRoleOf_Should_ReturnTrue_WhenAuthenticatedUserHasRole() {

    when(authenticatedUser.getRoles()).thenReturn(
        new HashSet<>(List.of(UserRole.CONSULTANT.getValue())));

    boolean result =
        authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(
            UserRole.CONSULTANT.getValue(), UserRole.PEER_CONSULTANT.getValue());

    assertTrue(result);
  }

  @Test
  public void authenticatedUserRolesContainAnyRoleOf_Should_ReturnTrue_WhenAuthenticatedUserHasNotRole() {

    when(authenticatedUser.getRoles()).thenReturn(
        new HashSet<>(List.of(UserRole.CONSULTANT.getValue())));

    boolean result =
        authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.toString());

    assertFalse(result);
  }

}
