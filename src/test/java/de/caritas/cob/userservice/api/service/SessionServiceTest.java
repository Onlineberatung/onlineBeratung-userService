package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ROLES;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ENQUIRY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ENQUIRY_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_INVALID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_NEW;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ROLES;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_WITH_RC_ID;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.userservice.api.exception.UpdateSessionException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jeasy.random.EasyRandom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, "consultant",
      "first name", "last name", "consultant@cob.de", false, false, null, false, null, null, null,
      null, null, null);
  private final User USER = new User(USER_ID, null, "username", "name@domain.de", false);
  private final Session SESSION = new Session(ENQUIRY_ID, null, null, ConsultingType.SUCHT, "99999",
      1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session SESSION_2 = new Session(ENQUIRY_ID_2, null, null, ConsultingType.SUCHT,
      "99999", 1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session SESSION_WITH_CONSULTANT = new Session(ENQUIRY_ID, null, CONSULTANT,
      ConsultingType.SUCHT, "99999", 1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session ACCEPTED_SESSION = new Session(ENQUIRY_ID, null, CONSULTANT,
      ConsultingType.SUCHT, "99999", 1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final ConsultantAgency CONSULTANT_AGENCY_1 = new ConsultantAgency(1L, CONSULTANT, 1L,
      nowInUtc(), nowInUtc(), nowInUtc());
  private final Set<ConsultantAgency> CONSULTANT_AGENCY_SET = new HashSet<>();
  private final List<Session> SESSION_LIST_WITH_CONSULTANT = Collections
      .singletonList(SESSION_WITH_CONSULTANT);
  private final String ERROR_MSG = "error";
  private final UserDTO USER_DTO = new UserDTO(USERNAME, POSTCODE, AGENCY_ID, "XXX", "x@y.de", null,
      null, null, null, null, null, ConsultingType.SUCHT.getValue() + "", true);

  @InjectMocks
  private SessionService sessionService;
  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private AgencyServiceHelper agencyServiceHelper;
  @Mock
  private Logger logger;
  @Mock
  private SessionDataHelper sessionDataHelper;
  @Mock
  private UserHelper userHelper;
  @Mock
  private ConsultantService consultantService;

  @Before
  public void setUp() {
    CONSULTANT_AGENCY_SET.add(CONSULTANT_AGENCY_1);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void getSessionsForConsultant_Should_SessionsSorted() {

    // Sorting for COBH-199 is done directly via the Spring CRUD repository using method notation.
    // The test becomes invalid if the method name has been changed.
    // Then you have to check if the sorting still exists.
    Consultant consultant = Mockito.mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = Collections.singletonList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(agencyIds,
            SessionStatus.NEW);
  }

  @Test
  public void getSession_Should_ThrowInternalServerErrorException_WhenRepositoryFails() {

    DataAccessException ex = new DataAccessException("Database error") {
    };
    when(sessionRepository.findById(ENQUIRY_ID)).thenThrow(ex);
    try {
      sessionService.getSession(ENQUIRY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSession_Should_ReturnSession_WhenGetSessionIsSuccessful() {

    Optional<Session> session = Optional.of(SESSION);

    when(sessionRepository.findById(ENQUIRY_ID)).thenReturn(session);

    Optional<Session> result = sessionService.getSession(ENQUIRY_ID);

    assertTrue(result.isPresent());
    assertEquals(SESSION, result.get());
  }

  @Test
  public void updateConsultantAndStatusForSession_Should_ThrowUpdateSessionException_WhenSaveSessionFails() {

    InternalServerErrorException ex = new InternalServerErrorException("service error") {
    };
    when(sessionService.saveSession(any())).thenThrow(ex);

    try {
      sessionService.updateConsultantAndStatusForSession(SESSION, CONSULTANT, SessionStatus.NEW);
      fail("Expected exception: UpdateSessionException");
    } catch (UpdateSessionException updateSessionException) {
      assertTrue("Excepted UpdateSessionException thrown", true);
    }

  }

  @Test
  public void updateConsultantAndStatusForSession_Should_SaveSession()
      throws UpdateSessionException {

    sessionService.updateConsultantAndStatusForSession(SESSION, CONSULTANT, SessionStatus.NEW);
    verify(sessionRepository, times(1)).save(SESSION);

  }

  @Test
  public void saveSession_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    DataAccessException ex = new DataAccessException("database error") {
    };
    when(sessionRepository.save(any())).thenThrow(ex);

    try {
      sessionService.saveSession(SESSION);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void deleteSession_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    DataAccessException ex = new DataAccessException("database error") {
    };
    Mockito.doThrow(ex).when(sessionRepository).delete(any(Session.class));

    try {
      sessionService.deleteSession(SESSION);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void deleteSession_Should_DeleteSession() {

    sessionService.deleteSession(SESSION);
    verify(sessionRepository, times(1)).delete(SESSION);

  }

  @Test
  public void initializeSession_Should_ReturnSession() {

    when(sessionRepository.save(any())).thenReturn(SESSION);

    Session expectedSession = sessionService
        .initializeSession(USER, USER_DTO, IS_TEAM_SESSION, CONSULTING_TYPE_SETTINGS_SUCHT);
    Assert.assertEquals(expectedSession, SESSION);

  }

  @Test
  public void initializeSession_TeamSession_Should_ReturnSession() {

    when(sessionRepository.save(any())).thenReturn(SESSION);

    Session expectedSession = sessionService
        .initializeSession(USER, USER_DTO, IS_TEAM_SESSION, CONSULTING_TYPE_SETTINGS_SUCHT);
    Assert.assertEquals(expectedSession, SESSION);

  }

  @Test
  public void getSessionsForUserId_Should_ThrowInternalServerErrorException_OnDatabaseError() {

    DataAccessException ex = new DataAccessException("Database error") {
    };

    when(sessionRepository.findByUserUserId(USER_ID)).thenThrow(ex);

    try {
      sessionService.getSessionsForUserId(USER_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSessionsForUserId_Should_ThrowInternalServerErrorException_OnAgencyServiceHelperError()
      throws Exception {

    AgencyServiceHelperException ex =
        new AgencyServiceHelperException(new Exception("AgencyService error"));
    List<Session> sessions = new ArrayList<>();
    sessions.add(ACCEPTED_SESSION);

    when(sessionRepository.findByUserUserId(USER_ID)).thenReturn(sessions);
    when(agencyServiceHelper.getAgencies(any())).thenThrow(ex);

    try {
      sessionService.getSessionsForUserId(USER_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSessionsForUser_Should_ReturnListOfUserSessionResponseDTO_When_ProvidedWithValidUserId()
      throws Exception {

    List<Session> sessions = new ArrayList<>();
    sessions.add(ACCEPTED_SESSION);

    when(sessionRepository.findByUserUserId(USER_ID)).thenReturn(sessions);
    when(agencyServiceHelper.getAgencies(any())).thenReturn(AGENCY_DTO_LIST);

    assertThat(sessionService.getSessionsForUserId(USER_ID),
        everyItem(instanceOf(UserSessionResponseDTO.class)));
  }

  /**
   * method: getSessionsForUser
   */

  @Test
  public void getSessionsForUser_Should_ReturnListOfSessionsForUser() {

    List<Session> sessions = new ArrayList<>();
    sessions.add(SESSION);
    sessions.add(SESSION_2);

    when(sessionRepository.findByUser(USER)).thenReturn(sessions);

    List<Session> result = sessionService.getSessionsForUser(USER);

    assertEquals(sessions, result);

  }

  @Test
  public void getSessionsForUser_Should_ThrowInternalServerErrorExceptionAndLogExceptionOnDatabaseError() {

    DataAccessException ex = new DataAccessException("Database error") {
    };
    when(sessionRepository.findByUser(USER)).thenThrow(ex);
    try {
      sessionService.getSessionsForUser(USER);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  /**
   * method: getSessionsForUserByConsultingType
   */

  @Test
  public void getSessionsForUserByConsultingType_Should_ReturnListOfSessionsForUser() {

    List<Session> sessions = new ArrayList<>();
    sessions.add(SESSION);
    sessions.add(SESSION_2);

    when(sessionRepository.findByUserAndConsultingType(USER, ConsultingType.SUCHT))
        .thenReturn(sessions);

    List<Session> result =
        sessionService.getSessionsForUserByConsultingType(USER, ConsultingType.SUCHT);

    assertEquals(sessions, result);
    assertThat(result.get(0), instanceOf(Session.class));
  }

  @Test
  public void getSessionsForUserByConsultingType_Should_ThrowInternalServerErrorExceptionOnDatabaseError() {

    DataAccessException ex = new DataAccessException("Database error") {
    };
    when(sessionRepository.findByUserAndConsultingType(USER, ConsultingType.SUCHT)).thenThrow(ex);
    try {
      sessionService.getSessionsForUserByConsultingType(USER, ConsultingType.SUCHT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  /**
   * method: getSessionsForConsultant
   */

  @Test
  public void getSessionsForConsultant_Should_ReturnInternalServerErrorExceptionOnDatabaseError() {

    DataAccessException ex = new DataAccessException("reason") {
    };
    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository.findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
        any(), any())).thenThrow(ex);

    try {
      sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test(expected = BadRequestException.class)
  public void getSessionsForConsultant_Should_ThrowBadRequestException_WhenStatusParameterIsInvalid() {

    sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_INVALID);
  }

  @Test
  public void getSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusNew() {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository.findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
        any(), any())).thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  public void getSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusInProgress() {

    when(sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  /**
   * Method: getSessionByGroupIdAndUser
   */

  @Test
  public void getSessionByGroupIdAndUser_Should_ReturnSession_WhenAskerIsSessionOwner() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.getUser().setUserId(USER_ID);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    Session result = sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES);

    assertThat(result, instanceOf(Session.class));
  }

  @Test
  public void getSessionByGroupIdAndUser_Should_ReturnSession_WhenConsultantIsAssignedToSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.getConsultant().setId(USER_ID);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));
    when(consultantService.getConsultant(anyString()))
        .thenReturn(Optional.of(session.getConsultant()));

    Session result =
        sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);

    assertThat(result, instanceOf(Session.class));
  }

  @Test
  public void getSessionByGroupIdAndUser_Should_ReturnSession_WhenConsultantIsAssignedToAgencyOfSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));
    when(consultantService.getConsultant(anyString()))
        .thenReturn(Optional.of(session.getConsultant()));
    session.setAgencyId(AGENCY_ID);
    session.getConsultant().getConsultantAgencies()
        .forEach(consultantAgency -> consultantAgency.setAgencyId(AGENCY_ID));

    Session result =
        sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);

    assertThat(result, instanceOf(Session.class));
  }

  @Test(expected = NotFoundException.class)
  public void getSessionByGroupIdAndUser_Should_ThrowNotFoundException_When_SessionDoesNotExist() {
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.empty());

    sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);
  }

  @Test(expected = ForbiddenException.class)
  public void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_AskerIsNotOwnerOfSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES);
  }

  @Test(expected = ForbiddenException.class)
  public void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_ConsultantIsNotAssignedToSessionOrToSessionsAgency() {
    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    session.getConsultant().setId("notDirectlyAssignedId");
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));
    when(consultantService.getConsultant(anyString()))
        .thenReturn(Optional.of(consultant));

    sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);
  }

  @Test(expected = ForbiddenException.class)
  public void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_NotAskerOrConsultantRole() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, new HashSet<>(
        Collections.singletonList("no-role")));
  }

  /**
   * method: getTeamSessionsForConsultant
   */

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnInternalServerErrorExceptionOnDatabaseError() {

    DataAccessException ex = new DataAccessException(ERROR_MSG) {
    };
    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), any(), Mockito.anyBoolean())).thenThrow(ex);

    try {
      sessionService.getTeamSessionsForConsultant(consultant);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultant() {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), any(), Mockito.anyBoolean()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getTeamSessionsForConsultant(consultant),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithConsultant_WhenProvidedWithValidConsultant() {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), any(), Mockito.anyBoolean()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    SessionConsultantForConsultantDTO sessionDTO =
        sessionService.getTeamSessionsForConsultant(consultant)
            .get(0).getConsultant();

    assertTrue(sessionDTO.getId() != null && sessionDTO.getFirstName() != null
        && sessionDTO.getLastName() != null);
  }

  @Test
  public void updateFeedbackGroupId_Should_ThrowUpdateFeedbackGroupIdException_WhenSaveSessionFails() {

    InternalServerErrorException ex = new InternalServerErrorException(ERROR_MSG) {
    };
    when(sessionService.saveSession(any())).thenThrow(ex);

    try {
      sessionService.updateFeedbackGroupId(Optional.of(SESSION), RC_GROUP_ID);
      fail("Expected exception: UpdateFeedbackGroupIdException");
    } catch (UpdateFeedbackGroupIdException updateFeedbackGroupIdException) {
      assertTrue("Excepted UpdateFeedbackGroupIdException thrown", true);
    }

  }

  @Test
  public void updateFeedbackGroupId_Should_SaveSession() throws UpdateFeedbackGroupIdException {

    sessionService.updateFeedbackGroupId(Optional.of(SESSION), RC_GROUP_ID);
    verify(sessionRepository, times(1)).save(SESSION);
  }

  @Test(expected = NotFoundException.class)
  public void fetchSessionForConsultant_Should_ThrowNotFoundException_When_SessionIsNotFound() {

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
    sessionService.fetchSessionForConsultant(SESSION_ID, CONSULTANT);
  }

  @Test
  public void fetchSessionForConsultant_Should_Return_ValidConsultantSessionDTO() {

    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    ConsultantSessionDTO result = sessionService
        .fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY);

    assertEquals(session.getId(), result.getId());
    assertEquals(session.isTeamSession(), result.getIsTeamSession());
    assertEquals(session.getAgencyId(), result.getAgencyId());
    assertEquals(session.getConsultant().getId(), result.getConsultantId());
    assertEquals(session.getConsultant().getRocketChatId(), result.getConsultantRcId());
    assertEquals(session.getUser().getUserId(), result.getAskerId());
    assertEquals(session.getUser().getRcUserId(), result.getAskerRcId());
    assertEquals(session.getPostcode(), result.getPostcode());
    assertEquals(session.isMonitoring(), result.getIsMonitoring());
    assertEquals(session.getStatus().getValue(), result.getStatus().intValue());
    assertEquals(session.getGroupId(), result.getGroupId());
    assertEquals(session.getFeedbackGroupId(), result.getFeedbackGroupId());
    assertEquals(session.getConsultingType().getValue(), result.getConsultingType().intValue());

  }

  @Test(expected = ForbiddenException.class)
  public void fetchSessionForConsultant_Should_ThrowForbiddenException_When_NoPermission() {

    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY_2);
    session.setUser(USER_WITH_RC_ID);
    session.setAgencyId(CONSULTANT_WITH_AGENCY_2
        .getConsultantAgencies()
        .iterator()
        .next()
        .getAgencyId());
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY);
  }

  @Test
  public void fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_WhenConsultantIsAssigned() {

    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    session.setAgencyId(CONSULTANT_WITH_AGENCY
        .getConsultantAgencies()
        .iterator()
        .next()
        .getAgencyId());
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    assertNotNull(
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY));
  }

  @Test
  public void fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_WhenConsultantIsNotAssignedButInAgency() {

    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY_2);
    session.setUser(USER_WITH_RC_ID);
    session.setAgencyId(CONSULTANT_WITH_AGENCY
        .getConsultantAgencies()
        .iterator()
        .next()
        .getAgencyId());
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    assertNotNull(
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY));
  }

}
