package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ROLES;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_WITH_AGENCY_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ENQUIRY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ENQUIRY_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ROLES;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_WITH_RC_ID;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static liquibase.util.BooleanUtils.isTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.testHelper.TestConstants;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.ArrayList;
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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

  public static final long AGENCY_3 = 3L;
  private final Consultant CONSULTANT =
      new Consultant(
          CONSULTANT_ID,
          ROCKETCHAT_ID,
          "consultant",
          "first name",
          "last name",
          "consultant@cob.de",
          false,
          false,
          null,
          false,
          null,
          null,
          null,
          null,
          null,
          nowInUtc(),
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final User USER = new User(USER_ID, null, "username", "name@domain.de", false);
  private final Session SESSION = TestConstants.SESSION;

  private final Session SESSION_2 =
      Session.builder()
          .id(ENQUIRY_ID_2)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("99999")
          .status(SessionStatus.NEW)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(false)
          .isPeerChat(false)
          .build();

  private final Session SESSION_WITH_CONSULTANT =
      Session.builder()
          .id(ENQUIRY_ID)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .consultant(CONSULTANT)
          .registrationType(REGISTERED)
          .postcode("99999")
          .status(SessionStatus.NEW)
          .languageCode(LanguageCode.de)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(false)
          .isPeerChat(false)
          .build();

  private final Session ACCEPTED_SESSION =
      Session.builder()
          .id(ENQUIRY_ID)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .consultant(CONSULTANT)
          .registrationType(REGISTERED)
          .agencyId(1L)
          .postcode("99999")
          .status(SessionStatus.NEW)
          .languageCode(LanguageCode.de)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(false)
          .isPeerChat(false)
          .build();

  private final ConsultantAgency CONSULTANT_AGENCY_1 =
      new ConsultantAgency(1L, CONSULTANT, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final Set<ConsultantAgency> CONSULTANT_AGENCY_SET = new HashSet<>();
  private final List<Session> SESSION_LIST_WITH_CONSULTANT = singletonList(SESSION_WITH_CONSULTANT);
  private final String ERROR_MSG = "error";
  private final UserDTO USER_DTO =
      new UserDTO(
          USERNAME,
          POSTCODE,
          AGENCY_ID,
          "XXX",
          "x@y.de",
          null,
          null,
          null,
          CONSULTING_TYPE_ID_SUCHT + "",
          "",
          true,
          null,
          null,
          null,
          null,
          null,
          null,
          null);

  @InjectMocks private SessionService sessionService;
  @Mock private SessionRepository sessionRepository;
  @Mock private AgencyService agencyService;
  @Mock private Logger logger;
  @Mock private ConsultantService consultantService;
  @Mock private UserService userService;
  @Mock private ConsultingTypeManager consultingTypeManager;

  private final EasyRandom easyRandom = new EasyRandom();

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

    sessionService.getRegisteredEnquiriesForConsultant(consultant);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.NEW, REGISTERED);
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.INITIAL, REGISTERED);
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.IN_PROGRESS, REGISTERED);
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.IN_ARCHIVE, REGISTERED);
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.DONE, REGISTERED);
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

    Session expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_TEAM_SESSION);

    assertEquals(expectedSession, SESSION);
  }

  @Test
  void initializeSession_TeamSession_Should_ReturnSession() {
    when(sessionRepository.save(any())).thenReturn(SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    Session expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_TEAM_SESSION);

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

    assertThat(
        sessionService.getSessionsForUserId(USER_ID),
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

    when(sessionRepository.findByUserAndConsultingTypeId(USER, CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(sessions);

    List<Session> result =
        sessionService.getSessionsForUserByConsultingTypeId(USER, CONSULTING_TYPE_ID_SUCHT);

    assertEquals(sessions, result);
    assertThat(result.get(0), instanceOf(Session.class));
  }

  @Test
  void
      getEnquiriesForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusNew() {

    Consultant consultant = mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
            .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
                any(), any(), any()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(
        sessionService.getRegisteredEnquiriesForConsultant(consultant),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));
  }

  @Test
  void
      getEnquiriesForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultantAndStatusInProgress() {

    when(sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(
        sessionService.getActiveAndDoneSessionsForConsultant(CONSULTANT),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));

    verify(sessionRepository, times(1))
        .findByConsultantAndStatus(any(), eq(SessionStatus.IN_PROGRESS));
    verify(sessionRepository, times(1)).findByConsultantAndStatus(any(), eq(SessionStatus.DONE));
    verify(sessionRepository, never())
        .findByConsultantAndStatus(any(), eq(SessionStatus.IN_ARCHIVE));
    verify(sessionRepository, never()).findByConsultantAndStatus(any(), eq(SessionStatus.INITIAL));
    verify(sessionRepository, never()).findByConsultantAndStatus(any(), eq(SessionStatus.NEW));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ReturnSession_WhenAskerIsSessionOwner() {
    Session session = easyRandom.nextObject(Session.class);
    session.getUser().setUserId(USER_ID);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    Session result = sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES);

    assertThat(result, instanceOf(Session.class));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ReturnSession_WhenConsultantIsAssignedToSession() {
    Session session = easyRandom.nextObject(Session.class);
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
    Session session = easyRandom.nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));
    when(consultantService.getConsultant(anyString()))
        .thenReturn(Optional.of(session.getConsultant()));
    session.setAgencyId(AGENCY_ID);
    session
        .getConsultant()
        .getConsultantAgencies()
        .forEach(consultantAgency -> consultantAgency.setAgencyId(AGENCY_ID));

    Session result =
        sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES);

    assertThat(result, instanceOf(Session.class));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowNotFoundException_When_SessionDoesNotExist() {
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_AskerIsNotOwnerOfSession() {
    Session session = easyRandom.nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    assertThrows(
        ForbiddenException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES));
  }

  @Test
  void
      getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_ConsultantIsNotAssignedToSessionOrToSessionsAgency() {
    Session session = easyRandom.nextObject(Session.class);
    session.getConsultant().setId("notDirectlyAssignedId");
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));

    assertThrows(
        ForbiddenException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, CONSULTANT_ROLES));
  }

  @Test
  void getSessionByGroupIdAndUser_Should_ThrowForbiddenException_When_NotAskerOrConsultantRole() {
    var session = easyRandom.nextObject(Session.class);
    when(sessionRepository.findByGroupId(any())).thenReturn(Optional.of(session));

    var roles = new HashSet<>(singletonList("no-role"));
    assertThrows(
        ForbiddenException.class,
        () -> sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, roles));
  }

  /** method: getTeamSessionsForConsultant */
  @Test
  void
      getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTO_WhenProvidedWithValidConsultant() {

    Consultant consultant = mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
            .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
                any(), any(), any(), anyBoolean()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    assertThat(
        sessionService.getTeamSessionsForConsultant(consultant),
        everyItem(instanceOf(ConsultantSessionResponseDTO.class)));

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), eq(SessionStatus.IN_PROGRESS), anyBoolean());
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), eq(SessionStatus.INITIAL), anyBoolean());
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), eq(SessionStatus.NEW), anyBoolean());
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), eq(SessionStatus.DONE), anyBoolean());
    verify(sessionRepository, never())
        .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
            any(), any(), eq(SessionStatus.IN_ARCHIVE), anyBoolean());
  }

  @Test
  void
      getTeamSessionsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithConsultant_WhenProvidedWithValidConsultant() {

    Consultant consultant = mock(Consultant.class);

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(sessionRepository
            .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
                any(), any(), any(), anyBoolean()))
        .thenReturn(SESSION_LIST_WITH_CONSULTANT);

    SessionConsultantForConsultantDTO sessionDTO =
        sessionService.getTeamSessionsForConsultant(consultant).get(0).getConsultant();

    assertTrue(
        sessionDTO.getId() != null
            && sessionDTO.getFirstName() != null
            && sessionDTO.getLastName() != null);
  }

  @Test
  void updateFeedbackGroupId_Should_ThrowUpdateFeedbackGroupIdException_WhenSaveSessionFails() {

    InternalServerErrorException ex = new InternalServerErrorException(ERROR_MSG) {};
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

    assertThrows(
        NotFoundException.class,
        () -> sessionService.fetchSessionForConsultant(SESSION_ID, CONSULTANT));
  }

  @Test
  void fetchSessionForConsultant_Should_Return_ValidConsultantSessionDTO() {

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    AgencyDTO agencyDTO = Mockito.mock(AgencyDTO.class);
    when(agencyService.getAgency(session.getAgencyId())).thenReturn(agencyDTO);

    ConsultantSessionDTO result =
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY);

    assertEquals(session.getId(), result.getId());
    assertEquals(session.isTeamSession(), result.getIsTeamSession());
    assertEquals(session.getAgencyId(), result.getAgencyId());
    assertEquals(session.getConsultant().getId(), result.getConsultantId());
    assertEquals(session.getConsultant().getRocketChatId(), result.getConsultantRcId());
    assertEquals(session.getUser().getUserId(), result.getAskerId());
    assertEquals(session.getUser().getRcUserId(), result.getAskerRcId());
    assertEquals(session.getPostcode(), result.getPostcode());
    assertEquals(session.getStatus().getValue(), result.getStatus().intValue());
    assertEquals(session.getGroupId(), result.getGroupId());
    assertEquals(session.getFeedbackGroupId(), result.getFeedbackGroupId());
    assertEquals(session.getConsultingTypeId(), result.getConsultingType().intValue());
    assertEquals(session.getUserAge(), result.getAge());
    assertEquals(session.getUserGender(), result.getGender());
    assertEquals(session.getCounsellingRelation(), result.getCounsellingRelation());
    assertEquals(session.getReferer(), result.getReferer());
    assertEquals(String.valueOf(session.getCreateDate()), result.getCreateDate());
  }

  @Test
  void
      fetchSessionForConsultant_Should_NotEnrichTopicsData_When_TopicsFeatureEnabledIsNotEnabled() {
    var sessionTopicEnrichmentService = mock(ConsultantSessionTopicEnrichmentService.class);
    ReflectionTestUtils.setField(
        sessionService, "sessionTopicEnrichmentService", sessionTopicEnrichmentService);

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    AgencyDTO agencyDTO = Mockito.mock(AgencyDTO.class);
    when(agencyService.getAgency(session.getAgencyId())).thenReturn(agencyDTO);

    ConsultantSessionDTO result =
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY);

    assertEquals(session.getId(), result.getId());

    verifyNoInteractions(sessionTopicEnrichmentService);
    assertNull(result.getMainTopic());
    assertThat(result.getTopics()).isEmpty();
  }

  @Test
  void
      fetchSessionForConsultant_Should_CallSessionTopicEnrichmentService_When_TopicsFeatureEnabledIsEnabled() {
    ReflectionTestUtils.setField(sessionService, "topicsFeatureEnabled", true);
    var sessionTopicEnrichmentService = mock(ConsultantSessionTopicEnrichmentService.class);
    ReflectionTestUtils.setField(
        sessionService, "sessionTopicEnrichmentService", sessionTopicEnrichmentService);

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    AgencyDTO agencyDTO = Mockito.mock(AgencyDTO.class);
    when(agencyService.getAgency(session.getAgencyId())).thenReturn(agencyDTO);

    sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY);

    verify(sessionTopicEnrichmentService)
        .enrichSessionWithTopicsData(Mockito.any(ConsultantSessionDTO.class));
    verify(sessionTopicEnrichmentService)
        .enrichSessionWithTopicsData(Mockito.any(ConsultantSessionDTO.class));

    ReflectionTestUtils.setField(sessionService, "topicsFeatureEnabled", false);
  }

  @Test
  void
      fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_containing_createDate_and_agencyName() {

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.name("TestName");
    when(agencyService.getAgency(session.getAgencyId())).thenReturn(agencyDTO);

    ConsultantSessionDTO result =
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY);

    assertNotNull(result.getAgencyName());
    assertEquals("TestName", result.getAgencyName());
    assertEquals(String.valueOf(session.getCreateDate()), result.getCreateDate());
  }

  @Test
  void fetchSessionForConsultant_Should_ThrowForbiddenException_When_NoPermission() {

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY_2);
    session.setUser(USER_WITH_RC_ID);
    session.setTeamSession(true);
    session.setAgencyId(AGENCY_3);
    Long sessionId = session.getId();
    when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

    assertThrows(
        ForbiddenException.class,
        () -> sessionService.fetchSessionForConsultant(sessionId, CONSULTANT_WITH_AGENCY));
  }

  @Test
  void fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_WhenConsultantIsAssigned() {

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY);
    session.setUser(USER_WITH_RC_ID);
    session.setAgencyId(
        CONSULTANT_WITH_AGENCY.getConsultantAgencies().iterator().next().getAgencyId());
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    AgencyDTO agencyDTO = Mockito.mock(AgencyDTO.class);
    when(agencyService.getAgency(session.getAgencyId())).thenReturn(agencyDTO);

    assertNotNull(
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY));
  }

  @Test
  void
      fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_WhenConsultantIsNotAssignedButInAgency() {

    Session session = easyRandom.nextObject(Session.class);
    session.setConsultant(CONSULTANT_WITH_AGENCY_2);
    session.setUser(USER_WITH_RC_ID);
    session.setTeamSession(true);
    session.setAgencyId(
        CONSULTANT_WITH_AGENCY.getConsultantAgencies().iterator().next().getAgencyId());
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
    AgencyDTO agencyDTO = Mockito.mock(AgencyDTO.class);
    when(agencyService.getAgency(session.getAgencyId())).thenReturn(agencyDTO);

    assertNotNull(
        sessionService.fetchSessionForConsultant(session.getId(), CONSULTANT_WITH_AGENCY));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getEnquiriesForConsultant_Should_returnEmptyList_When_consultantHasNoAgencyAssigned(
      Set<ConsultantAgency> emptyConsultantAgencies) {
    Consultant consultant = mock(Consultant.class);
    when(consultant.getConsultantAgencies()).thenReturn(emptyConsultantAgencies);

    List<ConsultantSessionResponseDTO> enquiriesForConsultant =
        this.sessionService.getRegisteredEnquiriesForConsultant(consultant);

    assertThat(enquiriesForConsultant, hasSize(0));
  }

  @Test
  void
      getEnquiriesForConsultant_Should_use_registryTypeAwareRepositoryMethod_When_RegistryTypeNotNull() {
    Consultant consultant = mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = singletonList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getRegisteredEnquiriesForConsultant(consultant);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.NEW, REGISTERED);
  }

  @Test
  void
      getEnquiriesForConsultant_Should_use_registryTypeUnawareRepositoryMethod_When_RegistryTypeIsNull() {
    Consultant consultant = mock(Consultant.class);
    Set<ConsultantAgency> agencySet = new HashSet<>();
    agencySet.add(CONSULTANT_AGENCY_1);
    List<Long> agencyIds = singletonList(CONSULTANT_AGENCY_1.getAgencyId());

    when(consultant.getConsultantAgencies()).thenReturn(agencySet);

    sessionService.getRegisteredEnquiriesForConsultant(consultant);

    verify(sessionRepository, times(1))
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            agencyIds, SessionStatus.NEW, REGISTERED);
  }

  @Test
  void
      getSessionsForUser_Should_ReturnListOfUserSessionResponseDTOWithoutAgency_When_sessionHasNoAgencyAssigned() {
    Session session = easyRandom.nextObject(Session.class);
    session.setAgencyId(null);
    when(sessionRepository.findByUserUserId(USER_ID)).thenReturn(singletonList(session));

    List<UserSessionResponseDTO> sessionsForUserId = sessionService.getSessionsForUserId(USER_ID);

    assertNull(sessionsForUserId.iterator().next().getAgency());
  }

  @Test
  void
      getActiveAndDoneSessionsForConsultant_Should_ReturnListOfActiveAndDoneSessions_When_statusInProgress() {
    Session session = easyRandom.nextObject(Session.class);
    when(sessionRepository.findByConsultantAndStatus(any(), eq(SessionStatus.IN_PROGRESS)))
        .thenReturn(List.of(session));
    when(sessionRepository.findByConsultantAndStatus(any(), eq(SessionStatus.DONE)))
        .thenReturn(List.of(session));

    var activeAndDoneSessionsForConsultant =
        sessionService.getActiveAndDoneSessionsForConsultant(CONSULTANT);

    assertThat(activeAndDoneSessionsForConsultant, hasSize(2));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @NullSource
  void initializeSession_Should_initializePeerChat_When_consultingTypeSettingsHasPeerChat(
      Boolean isPeerChat) {
    var consultingTypeResponse = easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
    consultingTypeResponse.setIsPeerChat(isPeerChat);
    when(sessionRepository.save(any())).then(answer -> answer.getArgument(0, Session.class));
    when(consultingTypeManager.getConsultingTypeSettings(any())).thenReturn(consultingTypeResponse);

    var expectedSession = sessionService.initializeSession(USER, USER_DTO, IS_TEAM_SESSION);

    assertThat(expectedSession.isPeerChat(), is(isTrue(isPeerChat)));
  }

  @Test
  void
      getAllowedSessionsByConsultantAndGroupOrFeedbackGroupIds_should_find_new_anonymous_enquiry_if_consultant_may_advise_consulting_type() {
    Session anonymousEnquiry =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    when(sessionRepository.findByGroupOrFeedbackGroupIds(singleton("rcGroupId")))
        .thenReturn(singletonList(anonymousEnquiry));
    when(agencyService.getAgencies(singletonList(4711L))).thenReturn(AGENCY_DTO_LIST);
    ConsultantAgency agency = new ConsultantAgency();
    agency.setAgencyId(4711L);
    var consultant = createConsultantWithAgencies(agency);

    var sessionResponse =
        sessionService.getAllowedSessionsByConsultantAndGroupOrFeedbackGroupIds(
            consultant, singleton("rcGroupId"), singleton(UserRole.CONSULTANT.getValue()));

    assertEquals(1, sessionResponse.size());
  }

  @Test
  void
      getAllowedSessionsByConsultantAndGroupOrFeedbackGroupIds_should_only_return_the_sessions_the_consultant_can_see() {
    // given
    List<Session> sessions = new ArrayList<>();
    ConsultantAgency agency = new ConsultantAgency();
    agency.setAgencyId(4711L);
    var consultant = createConsultantWithAgencies(agency);
    var allowedSession = giveAllowedSessionWithID(1L, consultant);
    sessions.add(giveAllowedSessionWithID(2L, null));
    sessions.add(allowedSession);
    when(sessionRepository.findByGroupOrFeedbackGroupIds(singleton("rcGroupId")))
        .thenReturn(sessions);
    // when
    var sessionResponse =
        sessionService.getAllowedSessionsByConsultantAndGroupOrFeedbackGroupIds(
            consultant, singleton("rcGroupId"), singleton(UserRole.CONSULTANT.getValue()));
    // then
    assertThat(sessionResponse).hasSize(1);
    assertThat(sessionResponse.get(0).getSession().getId()).isEqualTo(allowedSession.getId());
  }

  @Test
  void
      getSessionsByIds_should_find_new_anonymous_enquiry_if_consultant_may_advise_consulting_type() {
    Session anonymousEnquiry =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    when(sessionRepository.findAllById(singleton(anonymousEnquiry.getId())))
        .thenReturn(singletonList(anonymousEnquiry));
    when(agencyService.getAgencies(singletonList(4711L))).thenReturn(AGENCY_DTO_LIST);
    ConsultantAgency agency = new ConsultantAgency();
    agency.setAgencyId(4711L);
    var consultant = createConsultantWithAgencies(agency);

    var sessionResponse =
        sessionService.getSessionsByIds(
            consultant,
            singleton(anonymousEnquiry.getId()),
            singleton(UserRole.CONSULTANT.getValue()));

    assertEquals(1, sessionResponse.size());
  }

  @Test
  void
      getSessionsByUserAndGroupOrFeedbackGroupIds_should_find_session_for_anonymous_user_of_session() {
    Session anonymousEnquiry =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    anonymousEnquiry.setUser(USER);
    when(sessionRepository.findByGroupOrFeedbackGroupIds(singleton("rcGroupId")))
        .thenReturn(singletonList(anonymousEnquiry));

    var sessionResponse = getSessionsByUserAndGroupOrFeedbackGroupIds(USER_ID);

    assertEquals(1, sessionResponse.size());
  }

  @Test
  void getSessionsByUserAndGroupOrFeedbackGroupIds_should_fail_if_user_is_not_owner_of_session() {
    Session anonymousEnquiry =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    anonymousEnquiry.setUser(USER);
    when(sessionRepository.findByGroupOrFeedbackGroupIds(singleton("rcGroupId")))
        .thenReturn(singletonList(anonymousEnquiry));

    assertThrows(
        ForbiddenException.class, () -> getSessionsByUserAndGroupOrFeedbackGroupIds("someOtherId"));
  }

  private List<UserSessionResponseDTO> getSessionsByUserAndGroupOrFeedbackGroupIds(
      String someOtherId) {
    return sessionService.getSessionsByUserAndGroupOrFeedbackGroupIds(
        someOtherId, singleton("rcGroupId"), singleton(UserRole.ANONYMOUS.getValue()));
  }

  @Test
  void getSessionsByUserAndSessionIds_should_find_session_for_anonymous_user_of_session() {
    Session anonymousEnquiry =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    anonymousEnquiry.setUser(USER);
    when(sessionRepository.findAllById(singleton(anonymousEnquiry.getId())))
        .thenReturn(singletonList(anonymousEnquiry));

    var sessionResponse = getSomeUserId(USER_ID, anonymousEnquiry);

    assertEquals(1, sessionResponse.size());
  }

  @Test
  void getSessionsByUserAndSessionIds_should_fail_if_user_is_not_owner_of_session() {
    Session anonymousEnquiry =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    anonymousEnquiry.setUser(USER);
    when(sessionRepository.findAllById(singleton(anonymousEnquiry.getId())))
        .thenReturn(singletonList(anonymousEnquiry));

    assertThrows(ForbiddenException.class, () -> getSomeUserId("someUserId", anonymousEnquiry));
  }

  private List<UserSessionResponseDTO> getSomeUserId(String someUserId, Session anonymousEnquiry) {
    return sessionService.getSessionsByUserAndSessionIds(
        someUserId, singleton(anonymousEnquiry.getId()), singleton(UserRole.ANONYMOUS.getValue()));
  }

  private Session giveAllowedSessionWithID(Long id, Consultant consultant) {
    Session allowedSession =
        createAnonymousNewEnquiryWithConsultingType(AGENCY_DTO_SUCHT.getConsultingType());
    allowedSession.setId(id);
    allowedSession.setConsultant(consultant);
    return allowedSession;
  }

  private Session createAnonymousNewEnquiryWithConsultingType(int consultingTypeId) {
    var session = easyRandom.nextObject(Session.class);
    session.setAgencyId(null);
    session.setTeamSession(false);
    session.setConsultant(null);
    session.setConsultingTypeId(consultingTypeId);
    session.setStatus(SessionStatus.NEW);
    session.setRegistrationType(ANONYMOUS);
    return session;
  }

  Consultant createConsultantWithAgencies(ConsultantAgency... agencies) {
    return new Consultant(
        CONSULTANT_ID,
        ROCKETCHAT_ID,
        "consultant",
        "first name",
        "last name",
        "consultant@cob.de",
        false,
        false,
        null,
        false,
        null,
        null,
        null,
        Set.of(agencies),
        null,
        nowInUtc(),
        null,
        null,
        true,
        true,
        true,
        true,
        null,
        null,
        ConsultantStatus.CREATED,
        false,
        LanguageCode.de,
        null,
        null,
        false,
        null);
  }
}
