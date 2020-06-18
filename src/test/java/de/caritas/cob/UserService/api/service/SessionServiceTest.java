package de.caritas.cob.UserService.api.service;

import static de.caritas.cob.UserService.testHelper.TestConstants.ACLOHOL;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_NAME;
import static de.caritas.cob.UserService.testHelper.TestConstants.CITY;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTANT_ROLES;
import static de.caritas.cob.UserService.testHelper.TestConstants.DRUGS;
import static de.caritas.cob.UserService.testHelper.TestConstants.ENQUIRY_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.ENQUIRY_ID_2;
import static de.caritas.cob.UserService.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.UserService.testHelper.TestConstants.OTHERS;
import static de.caritas.cob.UserService.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.SESSION_STATUS_INVALID;
import static de.caritas.cob.UserService.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.UserService.testHelper.TestConstants.SESSION_STATUS_NEW;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.exception.EnquiryMessageException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.UserService.api.exception.UpdateSessionException;
import de.caritas.cob.UserService.api.exception.responses.WrongParameterException;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.helper.Now;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.UserService.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.model.UserSessionResponseDTO;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.monitoring.MonitoringType;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionRepository;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, "consultant",
      "first name", "last name", "consultant@cob.de", false, false, null, false, null, null, null);
  private final User USER = new User(USER_ID, "username", "name@domain.de", null);
  private final Session SESSION = new Session(ENQUIRY_ID, null, null, ConsultingType.SUCHT, "99999",
      1L, SessionStatus.NEW, new Date(), null);
  private final Session SESSION_2 = new Session(ENQUIRY_ID_2, null, null, ConsultingType.SUCHT,
      "99999", 1L, SessionStatus.NEW, new Date(), null);
  private final Session SESSION_WITH_CONSULTANT = new Session(ENQUIRY_ID, null, CONSULTANT,
      ConsultingType.SUCHT, "99999", 1L, SessionStatus.NEW, new Date(), null);
  private final Session ACCEPTED_SESSION = new Session(ENQUIRY_ID, null, CONSULTANT,
      ConsultingType.SUCHT, "99999", 1L, SessionStatus.NEW, new Date(), null);
  private final ConsultantAgency CONSULTANT_AGENCY_1 = new ConsultantAgency(1L, CONSULTANT, 1L);
  private final Set<ConsultantAgency> CONSULTANT_AGENCY_SET = new HashSet<ConsultantAgency>();
  private final List<Session> SESSION_LIST = Arrays.asList(SESSION, SESSION_2);
  private final List<Session> SESSION_LIST_SINGLE = Arrays.asList(SESSION);
  private final List<Session> SESSION_LIST_WITH_CONSULTANT = Arrays.asList(SESSION_WITH_CONSULTANT);
  private final AgencyDTO AGENCY_DTO =
      new AgencyDTO(AGENCY_ID, AGENCY_NAME, POSTCODE, CITY, false, false, ConsultingType.SUCHT);
  private final String ERROR_MSG = "error";
  private LinkedHashMap<String, Object> SUCHT_MAP = new LinkedHashMap<String, Object>();
  private final UserDTO USER_DTO = new UserDTO(USERNAME, POSTCODE, AGENCY_ID, "XXX", "x@y.de", null,
      null, null, null, null, null, ConsultingType.SUCHT.getValue() + "");

  @InjectMocks
  private SessionService sessionService;
  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private AgencyServiceHelper agencyServiceHelper;
  @Mock
  private LogService logService;
  @Mock
  private Now now;
  @Mock
  private AuthenticatedUser authenticatedUser;

  @Before
  public void setUp() {
    CONSULTANT_AGENCY_SET.add(CONSULTANT_AGENCY_1);

    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<String, Object>();
    LinkedHashMap<String, Object> addictiveDrugsMap = new LinkedHashMap<String, Object>();
    drugsMap.put(OTHERS, false);
    addictiveDrugsMap.put(ACLOHOL, true);
    addictiveDrugsMap.put(DRUGS, drugsMap);
    SUCHT_MAP.put(MonitoringType.ADDICTIVE_DRUGS.getKey(), addictiveDrugsMap);
  }

  @Test
  public void getSessionsForConsultant_Should_SessionsSorted() throws Exception {

    // Sorting for COBH-199 is done directly via the Spring CRUD repository using method notation.
    // The test becomes invalid if the method name has been changed.
    // Then you have to check if the sorting still exists.
    Consultant consultant = Mockito.mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<ConsultantAgency>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = Arrays.asList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
            ArgumentMatchers.eq(agencyIds), ArgumentMatchers.eq(SessionStatus.NEW));
  }

  @Test
  public void getSession_Should_ThrowServiceExceptionAndLogDatabaseException_WhenRepositoryFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("Database error") {};
    when(sessionRepository.findById(ENQUIRY_ID)).thenThrow(ex);
    try {
      sessionService.getSession(ENQUIRY_ID);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);

  }

  @Test
  public void getSession_Should_ReturnSession_WhenGetSessionIsSuccessfull() throws Exception {

    Optional<Session> session = Optional.of(SESSION);

    when(sessionRepository.findById(ENQUIRY_ID)).thenReturn(session);

    Optional<Session> result = sessionService.getSession(ENQUIRY_ID);

    assertTrue(result.isPresent());
    assertEquals(SESSION, result.get());
  }

  @Test
  public void updateConsultantAndStatusForSession_Should_ThrowUpdateSessionException_WhenSaveSessionFails() {

    @SuppressWarnings("serial")
    ServiceException ex = new ServiceException("service error") {};
    when(sessionService.saveSession(Mockito.any())).thenThrow(ex);

    try {
      sessionService.updateConsultantAndStatusForSession(SESSION, CONSULTANT, SessionStatus.NEW);
      fail("Expected exception: UpdateSessionException");
    } catch (UpdateSessionException updateSessionException) {
      assertTrue("Excepted UpdateSessionException thrown", true);
    }

  }

  @Test
  public void updateConsultantAndStatusForSession_Should_SaveSession() {

    sessionService.updateConsultantAndStatusForSession(SESSION, CONSULTANT, SessionStatus.NEW);
    verify(sessionRepository, times(1)).save(SESSION);

  }

  @Test
  public void saveSession_Should_ThrowServiceException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {};
    when(sessionRepository.save(Mockito.any())).thenThrow(ex);

    try {
      sessionService.saveSession(SESSION);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

  }

  @Test
  public void deleteSession_Should_ThrowServiceException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {};
    Mockito.doThrow(ex).when(sessionRepository).delete(Mockito.any(Session.class));

    try {
      sessionService.deleteSession(SESSION);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

  }

  @Test
  public void deleteSession_Should_DeleteSession() {

    sessionService.deleteSession(SESSION);
    verify(sessionRepository, times(1)).delete(SESSION);

  }

  @Test
  public void initializeSession_Should_ReturnSession() {

    when(agencyServiceHelper.getAgency(USER_DTO.getAgencyId())).thenReturn(AGENCY_DTO);
    when(sessionRepository.save(Mockito.any())).thenReturn(SESSION);

    Session expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_MONITORING);
    Assert.assertEquals(expectedSession, SESSION);

  }

  @Test
  public void initializeSession_TeamSession_Should_ReturnSession() {

    when(agencyServiceHelper.getAgency(USER_DTO.getAgencyId())).thenReturn(AGENCY_DTO);
    when(sessionRepository.save(Mockito.any())).thenReturn(SESSION);

    Session expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_MONITORING);
    Assert.assertEquals(expectedSession, SESSION);

  }

  @Test
  public void getSessionsForUserId_Should_ThrowServiceExceptionAndLogExceptionOnDatabaseError()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("Database error") {};

    when(sessionRepository.findByUser_UserIdAndEnquiryMessageDateIsNotNull(USER_ID)).thenThrow(ex);

    try {
      sessionService.getSessionsForUserId(USER_ID);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  @Test
  public void getSessionsForUserId_Should_ThrowServiceExceptionAndLogExceptionOnAgencyServiceError()
      throws Exception {

    AgencyServiceHelperException ex =
        new AgencyServiceHelperException(new Exception("AgencyService error"));
    List<Session> sessions = new ArrayList<Session>();
    sessions.add(ACCEPTED_SESSION);

    when(sessionRepository.findByUser_UserIdAndEnquiryMessageDateIsNotNull(USER_ID))
        .thenReturn(sessions);
    when(agencyServiceHelper.getAgency(Mockito.anyLong())).thenThrow(ex);

    try {
      sessionService.getSessionsForUserId(USER_ID);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logAgencyServiceHelperException(ex);
  }

  @Test
  public void getSessionsForUser_Should_ReturnListOfUserSessionResponseDTO_WhenProvidedWithValidUserId()
      throws Exception {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(ACCEPTED_SESSION);

    when(sessionRepository.findByUser_UserIdAndEnquiryMessageDateIsNotNull(USER_ID))
        .thenReturn(sessions);
    when(agencyServiceHelper.getAgency(Mockito.anyLong())).thenReturn(AGENCY_DTO);

    assertThat(sessionService.getSessionsForUserId(USER_ID),
        everyItem(instanceOf(UserSessionResponseDTO.class)));
  }

  @Test
  public void saveEnquiryMessageDateAndRocketChatGroupId_Should_ThrowEnquiryMessageException_WhenSaveSessionFails()
      throws Exception {

    @SuppressWarnings("serial")
    ServiceException ex = new ServiceException("service error") {};
    when(sessionService.saveSession(Mockito.any())).thenThrow(ex);

    try {
      sessionService.saveEnquiryMessageDateAndRocketChatGroupId(SESSION, RC_GROUP_ID);
      fail("Expected exception: EnquiryMessageException");
    } catch (EnquiryMessageException enquiryMessageException) {
      assertTrue("Excepted EnquiryMessageException thrown", true);
    }
  }

  @Test
  public void saveEnquiryMessageDateAndRocketChatGroupId_Should_SetSessionStatusToNew() {

    Session session = Mockito.mock(Session.class);
    sessionService.saveEnquiryMessageDateAndRocketChatGroupId(session, RC_GROUP_ID);
    verify(session, times(1)).setStatus(SessionStatus.NEW);

  }

  @Test
  public void saveEnquiryMessageDateAndRocketChatGroupId_Should_SetGroupId() {

    Session session = Mockito.mock(Session.class);
    sessionService.saveEnquiryMessageDateAndRocketChatGroupId(session, RC_GROUP_ID);
    verify(session, times(1)).setGroupId(RC_GROUP_ID);

  }

  @Test
  public void saveEnquiryMessageDateAndRocketChatGroupId_Should_SetMessageDateToNow() {

    Session session = Mockito.mock(Session.class);
    Date dateNow = new Date();
    when(now.getDate()).thenReturn(dateNow);
    sessionService.saveEnquiryMessageDateAndRocketChatGroupId(session, RC_GROUP_ID);
    verify(now, times(1)).getDate();
    verify(session, times(1)).setEnquiryMessageDate(dateNow);

  }

  /**
   * method: getSessionsForUser
   * 
   */

  @Test
  public void getSessionsForUser_Should_ReturnListOfSessionsForUser() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION);
    sessions.add(SESSION_2);

    when(sessionRepository.findByUser(USER)).thenReturn(sessions);

    List<Session> result = sessionService.getSessionsForUser(USER);

    assertThat(sessions.equals(result)).isTrue();

  }

  @Test
  public void getSessionsForUser_Should_ThrowServiceExceptionAndLogExceptionOnDatabaseError()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("Database error") {};
    when(sessionRepository.findByUser(USER)).thenThrow(ex);
    try {
      sessionService.getSessionsForUser(USER);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  /**
   * method: getSessionsForUserByConsultingType
   * 
   */

  @Test
  public void getSessionsForUserByConsultingType_Should_ReturnListOfSessionsForUser() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION);
    sessions.add(SESSION_2);

    when(sessionRepository.findByUserAndConsultingType(USER, ConsultingType.SUCHT))
        .thenReturn(sessions);

    List<Session> result =
        sessionService.getSessionsForUserByConsultingType(USER, ConsultingType.SUCHT);

    assertThat(sessions.equals(result)).isTrue();
    assertThat(result.get(0), instanceOf(Session.class));
  }

  @Test
  public void getSessionsForUserByConsultingType_Should_ThrowServiceExceptionAndLogExceptionOnDatabaseError()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("Database error") {};
    when(sessionRepository.findByUserAndConsultingType(USER, ConsultingType.SUCHT)).thenThrow(ex);
    try {
      sessionService.getSessionsForUserByConsultingType(USER, ConsultingType.SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  /**
   * method: getSessionsForConsultant
   * 
   */

  @Test
  public void getSessionsForConsultant_Should_ReturnServiceExceptionOnDatabaseErrorAndLogException()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("reason") {};
    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository.findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
        Mockito.any(), Mockito.any())).thenThrow(ex);

    try {
      sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  @Test
  public void getSessionsForConsultant_Should_ReturnWrongParameterExceptionAndLogException_WhenStatusParameterIsInvalid()
      throws Exception {

    try {
      sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_INVALID);
      fail("Expected exception: WrongParameterException");
    } catch (WrongParameterException wrongParameterException) {
      assertTrue("Excepted WrongParameterException thrown", true);
    }
  }

  @Test
  public void getSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusNew()
      throws Exception {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository.findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
        Mockito.any(), Mockito.any())).thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  public void getSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusInProgress()
      throws Exception {

    when(sessionRepository.findByConsultantAndStatus(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  /**
   * 
   * Method: getSessionByGroupIdAndUserId Role: user
   * 
   */

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowServiceExceptionOnDatabaseErrorAndLogException_AsUserAuthority()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("reason") {};

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any())).thenThrow(ex);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowServiceExceptionOnCorruptData_AsUserAuthority()
      throws Exception {

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void getSessionByGroupIdAndUserId_Should_ReturnSession_WhenProvidedWithValidGroupIdAndUserId_AsUserAuthority() {

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST_SINGLE);

    assertThat(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES),
        instanceOf(Session.class));
  }

  @Test
  public void getSessionByGroupIdAndUserId_Should_ReturnNull_WhenNoSessionFound_AsUserAuthority()
      throws Exception {

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any()))
        .thenReturn(null);

    assertNull(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES));
  }

  /**
   * 
   * Method: getSessionByGroupIdAndUserId Role: consultant
   * 
   */

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowServiceExceptionOnDatabaseErrorAndLogException_AsConsultantAuthority()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("reason") {};

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenThrow(ex);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowServiceExceptionOnCorruptData_AsConsultantAuthority()
      throws Exception {

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void getSessionByGroupIdAndUserId_Should_ReturnSession_WhenProvidedWithValidGroupIdAndUserId_AsConsultantAuthority() {

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST_SINGLE);

    assertThat(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES),
        instanceOf(Session.class));
  }

  @Test
  public void getSessionByGroupIdAndUserId_Should_ReturnNull_WhenNoSessionFound_AsConsultantAuthority()
      throws Exception {

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenReturn(null);

    assertNull(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES));
  }

  /**
   * method: getTeamSessionsForConsultant
   * 
   */

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnServiceExceptionOnDatabaseErrorAndLogException()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR_MSG) {};
    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(ex);

    try {
      sessionService.getTeamSessionsForConsultant(consultant);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(logService, times(1)).logDatabaseError(ex);
  }

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultant()
      throws Exception {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getTeamSessionsForConsultant(consultant),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithConsultant_WhenProvidedWithValidConsultant()
      throws Exception {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    SessionConsultantForConsultantDTO sessionDTO =
        (SessionConsultantForConsultantDTO) sessionService.getTeamSessionsForConsultant(consultant)
            .get(0).getConsultant();

    assertTrue(sessionDTO.getId() != null && sessionDTO.getFirstName() != null
        && sessionDTO.getLastName() != null);
  }

  @Test
  public void updateFeedbackGroupId_Should_ThrowUpdateFeedbackGroupIdExceptionAndLogError_WhenSaveSessionFails() {

    @SuppressWarnings("serial")
    ServiceException ex = new ServiceException(ERROR_MSG) {};
    when(sessionService.saveSession(Mockito.any())).thenThrow(ex);

    try {
      sessionService.updateFeedbackGroupId(Optional.of(SESSION), RC_GROUP_ID);
      fail("Expected exception: UpdateFeedbackGroupIdException");
    } catch (UpdateFeedbackGroupIdException updateFeedbackGroupIdException) {
      assertTrue("Excepted UpdateFeedbackGroupIdException thrown", true);
      verify(logService, times(1)).logDatabaseError(Mockito.anyString(), Mockito.eq(ex));
    }

  }

  @Test
  public void updateFeedbackGroupId_Should_SaveSession() {

    sessionService.updateFeedbackGroupId(Optional.of(SESSION), RC_GROUP_ID);
    verify(sessionRepository, times(1)).save(SESSION);

  }
}
