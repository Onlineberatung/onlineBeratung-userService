package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.repository.session.ConsultingType.SUCHT;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
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
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ROLES;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_WITH_RC_ID;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, "consultant",
      "first name", "last name", "consultant@cob.de", false, false, null, false, null, null, null,
      null, null, null);
  private final User USER = new User(USER_ID, null, "username", "name@domain.de", false);
  private final Session SESSION = new Session(ENQUIRY_ID, null, null, SUCHT, REGISTERED, "99999",
      1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session SESSION_2 = new Session(ENQUIRY_ID_2, null, null, SUCHT, REGISTERED,
      "99999", 1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session SESSION_WITH_CONSULTANT = new Session(ENQUIRY_ID, null, CONSULTANT,
      SUCHT, REGISTERED, "99999", 1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final Session ACCEPTED_SESSION = new Session(ENQUIRY_ID, null, CONSULTANT,
      SUCHT, REGISTERED, "99999", 1L, SessionStatus.NEW, nowInUtc(), null, null, null,
      false, false, null, null);
  private final ConsultantAgency CONSULTANT_AGENCY_1 = new ConsultantAgency(1L, CONSULTANT, 1L,
      nowInUtc(), nowInUtc(), nowInUtc());
  private final Set<ConsultantAgency> CONSULTANT_AGENCY_SET = new HashSet<>();
  private final List<Session> SESSION_LIST_WITH_CONSULTANT = singletonList(SESSION_WITH_CONSULTANT);
  private final String ERROR_MSG = "error";
  private final UserDTO USER_DTO = new UserDTO(USERNAME, POSTCODE, AGENCY_ID, "XXX", "x@y.de", null,
      null, null, SUCHT.getValue() + "", true);

  @InjectMocks
  private SessionService sessionService;
  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private AgencyService agencyService;
  @Mock
  private Logger logger;
  @Mock
  private SessionDataProvider sessionDataProvider;
  @Mock
  private UserHelper userHelper;
  @Mock
  private ConsultantService consultantService;
  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @BeforeEach
  public void setUp() {
    CONSULTANT_AGENCY_SET.add(CONSULTANT_AGENCY_1);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  void getEnquiriesForConsultant_Should_SessionsSorted() {

    // Sorting for COBH-199 is done directly via the Spring CRUD repository using method notation.
    // The test becomes invalid if the method name has been changed.
    // Then you have to check if the sorting still exists.
    Consultant consultant = mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = singletonList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getEnquiriesForConsultant(consultant);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(agencyIds,
            SessionStatus.NEW);
  }

  @Test
  void getSession_Should_ReturnSession_WhenGetSessionIsSuccessful() {

    Optional<Session> session = Optional.of(SESSION);

    when(sessionRepository.findById(ENQUIRY_ID)).thenReturn(session);

    Optional<Session> result = sessionService.getSession(ENQUIRY_ID);

    assertTrue(result.isPresent());
    assertEquals(SESSION, result.get());
  }

  @Test
  void updateConsultantAndStatusForSession_Should_SaveSession() {

    sessionService.updateConsultantAndStatusForSession(SESSION, CONSULTANT, SessionStatus.NEW);
    verify(sessionRepository, times(1)).save(SESSION);

  }

  @Test
  void deleteSession_Should_DeleteSession() {

    sessionService.deleteSession(SESSION);
    verify(sessionRepository, times(1)).delete(SESSION);

  }

  @Test
  void initializeSession_Should_ReturnSession() {
    when(sessionRepository.save(any())).thenReturn(SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    Session expectedSession = sessionService
        .initializeSession(USER, USER_DTO, IS_TEAM_SESSION);

    assertEquals(expectedSession, SESSION);
  }

  @Test
  void initializeSession_TeamSession_Should_ReturnSession() {
    when(sessionRepository.save(any())).thenReturn(SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    Session expectedSession = sessionService
        .initializeSession(USER, USER_DTO, IS_TEAM_SESSION);

    assertEquals(expectedSession, SESSION);
  }

  @Test
  void getSessionsForUserId_Should_ThrowInternalServerErrorException_OnAgencyServiceHelperError() {
    List<Session> sessions = new ArrayList<>();
    sessions.add(ACCEPTED_SESSION);

    when(sessionRepository.findByUserUserId(USER_ID)).thenReturn(sessions);
    when(agencyService.getAgencies(any())).thenThrow(new InternalServerErrorException(""));

    try {
      sessionService.getSessionsForUserId(USER_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      // As expected
    }
  }

  @Test
  void getSessionsForUser_Should_ReturnListOfUserSessionResponseDTO_When_ProvidedWithValidUserId() {

    List<Session> sessions = new ArrayList<>();
    sessions.add(ACCEPTED_SESSION);

    when(sessionRepository.findByUserUserId(USER_ID)).thenReturn(sessions);
    when(agencyService.getAgencies(any())).thenReturn(AGENCY_DTO_LIST);

    assertThat(sessionService.getSessionsForUserId(USER_ID),
        everyItem(instanceOf(UserSessionResponseDTO.class)));
  }

  @Test
  void getSessionsForUser_Should_ReturnListOfSessionsForUser() {

    List<Session> sessions = new ArrayList<>();
    sessions.add(SESSION);
    sessions.add(SESSION_2);

    when(sessionRepository.findByUser(USER)).thenReturn(sessions);

    List<Session> result = sessionService.getSessionsForUser(USER);

    assertEquals(sessions, result);
  }

  @Test
  void getSessionsForUserByConsultingType_Should_ReturnListOfSessionsForUser() {

    List<Session> sessions = new ArrayList<>();
    sessions.add(SESSION);
    sessions.add(SESSION_2);

    when(sessionRepository.findByUserAndConsultingType(USER, SUCHT))
        .thenReturn(sessions);

    List<Session> result =
        sessionService.getSessionsForUserByConsultingType(USER, SUCHT);

    assertEquals(sessions, result);
    assertThat(result.get(0), instanceOf(Session.class));
  }

  @Test
  void getEnquiriesForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusNew() {

    Consultant consultant = mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository.findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
        any(), any())).thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getEnquiriesForConsultant(consultant),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  void getEnquiriesForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusInProgress() {

    when(sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getActiveSessionsForConsultant(CONSULTANT),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ReturnSession_WhenAskerIsSessionOwner() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.getUser().setUserId(USER_ID);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    Session result = sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES);

    assertThat(result, instanceOf(Session.class));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ReturnSession_WhenConsultantIsAssignedToSession() {
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
  void getSessionByGroupIdAndUser_Should_ReturnSession_WhenConsultantIsAssignedToAgencyOfSession() {
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

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowNotFoundException_When_SessionDoesNotExist() {
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_AskerIsNotOwnerOfSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    assertThrows(ForbiddenException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_ConsultantIsNotAssignedToSessionOrToSessionsAgency() {
    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    session.getConsultant().setId("notDirectlyAssignedId");
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));
    when(consultantService.getConsultant(anyString()))
        .thenReturn(Optional.of(consultant));

    assertThrows(ForbiddenException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_NotAskerOrConsultantRole() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    assertThrows(ForbiddenException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID,
            new HashSet<>(singletonList("no-role"))));
  }

  /**
   * method: getTeamSessionsForConsultant
   */

  @Test
  void getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultant() {

    Consultant consultant = mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), any(), Mockito.anyBoolean()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(sessionService.getTeamSessionsForConsultant(consultant),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  void getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithConsultant_WhenProvidedWithValidConsultant() {

    Consultant consultant = mock(Consultant.class);

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
  void updateFeedbackGroupId_Should_ThrowUpdateFeedbackGroupIdException_WhenSaveSessionFails() {

    InternalServerErrorException ex = new InternalServerErrorException(ERROR_MSG) {
    };
    when(sessionService.saveSession(any())).thenThrow(ex);

    try {
      sessionService.updateFeedbackGroupId(SESSION, RC_GROUP_ID);
      fail("Expected exception: UpdateFeedbackGroupIdException");
    } catch (UpdateFeedbackGroupIdException updateFeedbackGroupIdException) {
      // As expected
    }

  }

  @Test
  void updateFeedbackGroupId_Should_SaveSession() throws UpdateFeedbackGroupIdException {

    sessionService.updateFeedbackGroupId(SESSION, RC_GROUP_ID);
    verify(sessionRepository, times(1)).save(SESSION);
  }

  @Test
  void fetchSessionForConsultant_Should_ThrowNotFoundException_When_SessionIsNotFound() {

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> sessionService.fetchSessionForConsultant(SESSION_ID, CONSULTANT));
  }

  @Test
  void fetchSessionForConsultant_Should_Return_ValidConsultantSessionDTO() {

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

  @Test
  void fetchSessionForConsultant_Should_ThrowForbiddenException_When_NoPermission() {

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

    assertThrows(ForbiddenException.class,
        () -> sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY));
  }

  @Test
  void fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_WhenConsultantIsAssigned() {

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
  void fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_WhenConsultantIsNotAssignedButInAgency() {

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

  @ParameterizedTest
  @NullAndEmptySource
  void getEnquiriesForConsultant_Should_returnEmptyList_When_consultantHasNoAgencyAssigned(
      Set<ConsultantAgency> emptyConsultantAgencies) {
    Consultant consultant = mock(Consultant.class);
    when(consultant.getConsultantAgencies()).thenReturn(emptyConsultantAgencies);

    List<ConsultantSessionResponseDTO> enquiriesForConsultant = this.sessionService
        .getEnquiriesForConsultant(consultant);

    assertThat(enquiriesForConsultant, hasSize(0));
  }

  @Test
  void getEnquiriesForConsultant_Should_use_registryTypeAwareRepositoryMethod_When_RegistryTypeNotNull() {
    Consultant consultant = mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = singletonList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getEnquiriesForConsultant(consultant, REGISTERED);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.NEW, REGISTERED);
  }

  @Test
  void getEnquiriesForConsultant_Should_use_registryTypeUnawareRepositoryMethod_When_RegistryTypeIsNull() {
    Consultant consultant = mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = singletonList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getEnquiriesForConsultant(consultant, null);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.NEW);
  }

  @Test
  void getSessionsForUser_Should_ReturnListOfUserSessionResponseDTOWithoutAgency_When_sessionHasNoAgencyAssigned() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setAgencyId(null);
    when(sessionRepository.findByUserUserId(USER_ID)).thenReturn(singletonList(session));

    List<UserSessionResponseDTO> sessionsForUserId = sessionService.getSessionsForUserId(USER_ID);

    assertNull(sessionsForUserId.iterator().next().getAgency());
  }

}
