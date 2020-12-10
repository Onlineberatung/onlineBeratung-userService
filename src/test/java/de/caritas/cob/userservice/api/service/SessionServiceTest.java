package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACLOHOL;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.CITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ROLES;
import static de.caritas.cob.userservice.testHelper.TestConstants.DESCRIPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.DRUGS;
import static de.caritas.cob.userservice.testHelper.TestConstants.ENQUIRY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ENQUIRY_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NOT_OFFLINE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NO_TEAM_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.OTHERS;
import static de.caritas.cob.userservice.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_INVALID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_NEW;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

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
  private final ConsultantAgency CONSULTANT_AGENCY_1 = new ConsultantAgency(1L, CONSULTANT, 1L,
      LocalDateTime
          .now(), LocalDateTime.now());
  private final Set<ConsultantAgency> CONSULTANT_AGENCY_SET = new HashSet<>();
  private final List<Session> SESSION_LIST = Arrays.asList(SESSION, SESSION_2);
  private final List<Session> SESSION_LIST_SINGLE = Collections.singletonList(SESSION);
  private final List<Session> SESSION_LIST_WITH_CONSULTANT = Collections
      .singletonList(SESSION_WITH_CONSULTANT);
  private final AgencyDTO AGENCY_DTO = new AgencyDTO()
      .id(AGENCY_ID)
      .name(AGENCY_NAME)
      .postcode(POSTCODE)
      .city(CITY)
      .description(DESCRIPTION)
      .teamAgency(IS_NO_TEAM_AGENCY)
      .offline(IS_NOT_OFFLINE)
      .consultingType(ConsultingType.SUCHT);
  private final String ERROR_MSG = "error";
  private final UserDTO USER_DTO = new UserDTO(USERNAME, POSTCODE, AGENCY_ID, "XXX", "x@y.de", null,
      null, null, null, null, null, ConsultingType.SUCHT.getValue() + "");
  private LinkedHashMap<String, Object> SUCHT_MAP = new LinkedHashMap<>();

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

  @Before
  public void setUp() {
    CONSULTANT_AGENCY_SET.add(CONSULTANT_AGENCY_1);

    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<>();
    LinkedHashMap<String, Object> addictiveDrugsMap = new LinkedHashMap<>();
    drugsMap.put(OTHERS, false);
    addictiveDrugsMap.put(ACLOHOL, true);
    addictiveDrugsMap.put(DRUGS, drugsMap);
    SUCHT_MAP.put(MonitoringType.ADDICTIVE_DRUGS.getKey(), addictiveDrugsMap);
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
        .findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
            ArgumentMatchers.eq(agencyIds), ArgumentMatchers.eq(SessionStatus.NEW));
  }

  @Test
  public void getSession_Should_ThrowInternalServerErrorException_WhenRepositoryFails() {

    @SuppressWarnings("serial")
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
  public void getSession_Should_ReturnSession_WhenGetSessionIsSuccessfull() {

    Optional<Session> session = Optional.of(SESSION);

    when(sessionRepository.findById(ENQUIRY_ID)).thenReturn(session);

    Optional<Session> result = sessionService.getSession(ENQUIRY_ID);

    assertTrue(result.isPresent());
    assertEquals(SESSION, result.get());
  }

  @Test
  public void updateConsultantAndStatusForSession_Should_ThrowUpdateSessionException_WhenSaveSessionFails() {

    @SuppressWarnings("serial")
    InternalServerErrorException ex = new InternalServerErrorException("service error") {
    };
    when(sessionService.saveSession(Mockito.any())).thenThrow(ex);

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

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {
    };
    when(sessionRepository.save(Mockito.any())).thenThrow(ex);

    try {
      sessionService.saveSession(SESSION);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void deleteSession_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {
    };
    Mockito.doThrow(ex).when(sessionRepository).delete(Mockito.any(Session.class));

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
  public void initializeSession_Should_ReturnSession() throws AgencyServiceHelperException {

    when(agencyServiceHelper.getAgency(USER_DTO.getAgencyId())).thenReturn(AGENCY_DTO);
    when(sessionRepository.save(Mockito.any())).thenReturn(SESSION);

    Session expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_MONITORING);
    Assert.assertEquals(expectedSession, SESSION);

  }

  @Test
  public void initializeSession_TeamSession_Should_ReturnSession()
      throws AgencyServiceHelperException {

    when(agencyServiceHelper.getAgency(USER_DTO.getAgencyId())).thenReturn(AGENCY_DTO);
    when(sessionRepository.save(Mockito.any())).thenReturn(SESSION);

    Session expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_MONITORING);
    Assert.assertEquals(expectedSession, SESSION);

  }

  @Test
  public void getSessionsForUserId_Should_ThrowInternalServerErrorException_OnDatabaseError() {

    @SuppressWarnings("serial")
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
    when(agencyServiceHelper.getAgencies(Mockito.any())).thenThrow(ex);

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
    when(agencyServiceHelper.getAgencies(Mockito.any())).thenReturn(AGENCY_DTO_LIST);

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

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("reason") {
    };
    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository.findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
        Mockito.any(), Mockito.any())).thenThrow(ex);

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
        Mockito.any(), Mockito.any())).thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getSessionsForConsultant(consultant, SESSION_STATUS_NEW),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  public void getSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusInProgress() {

    when(sessionRepository.findByConsultantAndStatus(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  /**
   * Method: getSessionByGroupIdAndUserId Role: user
   */

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowInternalServerErrorExceptionOnDatabaseError_AsUserAuthority() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("reason") {
    };

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any())).thenThrow(ex);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowInternalServerErrorExceptionOnCorruptData_AsUserAuthority() {

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
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
  public void getSessionByGroupIdAndUserId_Should_ReturnNull_WhenNoSessionFound_AsUserAuthority() {

    when(sessionRepository.findByGroupIdAndUserUserId(Mockito.any(), Mockito.any()))
        .thenReturn(null);

    assertNull(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES));
  }

  /**
   * Method: getSessionByGroupIdAndUserId Role: consultant
   */

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowInternalServerErrorExceptionOnDatabaseError_AsConsultantAuthority() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("reason") {
    };

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenThrow(ex);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSessionByGroupIdAndUserId__Should_ThrowInternalServerErrorExceptionOnCorruptData_AsConsultantAuthority() {

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST);

    try {
      sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
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
  public void getSessionByGroupIdAndUserId_Should_ReturnNull_WhenNoSessionFound_AsConsultantAuthority() {

    when(sessionRepository.findByGroupIdAndConsultantId(Mockito.any(), Mockito.any()))
        .thenReturn(null);

    assertNull(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES));
  }

  /**
   * method: getTeamSessionsForConsultant
   */

  @Test
  public void getTeamSessionsForConsultant_Should_ReturnInternalServerErrorExceptionOnDatabaseError() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR_MSG) {
    };
    Consultant consultant = Mockito.mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(ex);

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
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
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
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    SessionConsultantForConsultantDTO sessionDTO =
        sessionService.getTeamSessionsForConsultant(consultant)
            .get(0).getConsultant();

    assertTrue(sessionDTO.getId() != null && sessionDTO.getFirstName() != null
        && sessionDTO.getLastName() != null);
  }

  @Test
  public void updateFeedbackGroupId_Should_ThrowUpdateFeedbackGroupIdException_WhenSaveSessionFails() {

    @SuppressWarnings("serial")
    InternalServerErrorException ex = new InternalServerErrorException(ERROR_MSG) {
    };
    when(sessionService.saveSession(Mockito.any())).thenThrow(ex);

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
}
