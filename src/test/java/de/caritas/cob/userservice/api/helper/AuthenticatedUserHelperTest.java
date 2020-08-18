package de.caritas.cob.userservice.api.helper;

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
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatedUserHelperTest {

  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, USERNAME,
      FIRST_NAME, LAST_NAME, EMAIL, false, false, null, false, null, null, null);
  private final Consultant TEAM_CONSULTANT = new Consultant(TEAM_CONSULTANT_ID, ROCKETCHAT_ID,
      USERNAME, FIRST_NAME, LAST_NAME, EMAIL, false, true, null, true, null, null, null);
  private final Session SESSION = new Session(SESSION_ID, null, CONSULTANT, ConsultingType.SUCHT,
      POSTCODE, AGENCY_ID, SessionStatus.NEW, new Date(), null);
  private final Session SESSION_WITH_DIFFERENT_CONSULTANT =
      new Session(SESSION_ID, null, TEAM_CONSULTANT, ConsultingType.SUCHT, POSTCODE, AGENCY_ID,
          SessionStatus.NEW, new Date(), null);
  private final Session TEAM_SESSION =
      new Session(TEAM_SESSION_ID, null, TEAM_CONSULTANT, ConsultingType.SUCHT, POSTCODE, AGENCY_ID,
          SessionStatus.IN_PROGRESS, new Date(), null, null, IS_TEAM_SESSION, IS_MONITORING);
  private final Session TEAM_SESSION_WITH_DIFFERENT_CONSULTANT =
      new Session(TEAM_SESSION_ID, null, CONSULTANT, ConsultingType.SUCHT, POSTCODE, AGENCY_ID,
          SessionStatus.IN_PROGRESS, new Date(), null, null, IS_TEAM_SESSION, IS_MONITORING);

  @InjectMocks
  private AuthenticatedUserHelper authenticatedUserHelper;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ConsultantAgencyService consultantAgencyService;

  @Test
  public void hasPermissionForSession_Should_ReturnFalse_WhenConsultantIsNotAssignedToSingleSession()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT.getId());

    boolean result =
        authenticatedUserHelper.hasPermissionForSession(SESSION_WITH_DIFFERENT_CONSULTANT);

    assertFalse(result);
  }

  @Test
  public void hasPermissionForSession_Should_ReturnFalse_WhenConsultantIsNotAssignedToAgencyOfTeamSessionOrToSession()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(TEAM_CONSULTANT.getId());

    boolean result =
        authenticatedUserHelper.hasPermissionForSession(TEAM_SESSION_WITH_DIFFERENT_CONSULTANT);

    assertFalse(result);
  }

  @Test
  public void hasPermissionForSession_Should_ReturnTrue_WhenConsultantIsAssignedToSingleSession()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT.getId());

    boolean result = authenticatedUserHelper.hasPermissionForSession(SESSION);

    assertTrue(result);
  }

  @Test
  public void hasPermissionForSession_Should_ReturnTrue_WhenConsultantIsAssignedToAgencyOfTeamSession()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(TEAM_CONSULTANT.getId());
    when(consultantAgencyService.isConsultantInAgency(TEAM_CONSULTANT.getId(),
        TEAM_SESSION.getAgencyId())).thenReturn(true);

    boolean result =
        authenticatedUserHelper.hasPermissionForSession(TEAM_SESSION_WITH_DIFFERENT_CONSULTANT);

    assertTrue(result);
  }

}
