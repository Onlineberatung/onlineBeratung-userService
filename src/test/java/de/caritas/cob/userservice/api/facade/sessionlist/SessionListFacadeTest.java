package de.caritas.cob.userservice.api.facade.sessionlist;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_FEEDBACK;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.COUNT_0;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.COUNT_1;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.COUNT_10;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.OFFSET_0;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_SESSION_RESPONSE_SESSION_CHAT_DTO_LIST;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.service.session.SessionFilter;
import de.caritas.cob.userservice.api.service.session.SessionTopicEnrichmentService;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionListService;
import de.caritas.cob.userservice.api.service.sessionlist.UserSessionListService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SessionListFacadeTest {

  @InjectMocks private SessionListFacade sessionListFacade;
  @Mock private ConsultantSessionListService consultantSessionListService;
  @Mock private UserSessionListService userSessionListService;
  @Mock private SessionTopicEnrichmentService sessionTopicEnrichmentService;

  /** Method: retrieveSessionsForAuthenticatedUser */
  @Before
  public void setUp() {
    ReflectionTestUtils.setField(
        sessionListFacade, "sessionTopicEnrichmentService", sessionTopicEnrichmentService);
  }

  @Test
  public void retrieveSessionsForAuthenticatedUser_Should_ReturnCorrectlySortedSessionList() {

    when(userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS))
        .thenReturn(USER_SESSION_RESPONSE_SESSION_CHAT_DTO_LIST);

    UserSessionListResponseDTO result =
        sessionListFacade.retrieveSortedSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO dto : result.getSessions()) {
      Long previousDate =
          (nonNull(dto.getSession()))
              ? dto.getSession().getMessageDate()
              : dto.getChat().getMessageDate();
      if (nonNull(dto.getSession())) {
        assertTrue(previousDate <= dto.getSession().getMessageDate());
      } else {
        assertTrue(previousDate <= dto.getChat().getMessageDate());
      }
    }
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectlySortedSessionList() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());

    for (ConsultantSessionResponseDTO dto : result.getSessions()) {
      Long previousDate =
          (nonNull(dto.getSession()))
              ? dto.getSession().getMessageDate()
              : dto.getChat().getMessageDate();
      if (nonNull(dto.getSession())) {
        assertTrue(previousDate <= dto.getSession().getMessageDate());
      } else {
        assertTrue(previousDate <= dto.getChat().getMessageDate());
      }
    }
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_EnrichWithTopicDataIfTopicsFeatureEnabled() {

    ReflectionTestUtils.setField(sessionListFacade, "topicsFeatureEnabled", true);

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());

    ReflectionTestUtils.setField(sessionListFacade, "topicsFeatureEnabled", false);
  }

  @Test
  public void
      retrieveTeamSessionsDtoForAuthenticatedConsultant_Should_EnrichWithTopicDataIfTopicsFeatureEnabled() {

    ReflectionTestUtils.setField(sessionListFacade, "topicsFeatureEnabled", true);

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());

    for (ConsultantSessionResponseDTO dto : result.getSessions()) {
      Long previousDate = dto.getSession().getMessageDate();
      assertTrue(previousDate <= dto.getSession().getMessageDate());
    }

    Mockito.verify(
            sessionTopicEnrichmentService,
            Mockito.times(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size()))
        .enrichSessionWithTopicData(Mockito.any(SessionDTO.class));

    ReflectionTestUtils.setField(sessionListFacade, "topicsFeatureEnabled", false);
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_When_CountIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(Integer.valueOf(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size()), result.getTotal());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_When_CountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() - 1, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(Integer.valueOf(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size()), result.getTotal());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectOffset() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(OFFSET_0, result.getOffset());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnNoSessions_When_OffsetIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(
            OFFSET_0 + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 1,
            COUNT_10,
            SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(COUNT_0, result.getSessions().size());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_When_CountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_1, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(COUNT_1, result.getSessions().size());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_When_CountIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 5, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnFilteredSessionList_When_FeedbackFilterIsSet() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.FEEDBACK);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_FEEDBACK);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, sessionListQueryParameter);

    assertEquals(COUNT_1, result.getSessions().size());
    assertFalse(result.getSessions().get(0).getSession().getFeedbackRead());
  }

  /** Method: retrieveTeamSessionsForAuthenticatedConsultant */
  @Test
  public void
      retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectlySortedSessionList() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());

    for (ConsultantSessionResponseDTO dto : result.getSessions()) {
      Long previousDate = dto.getSession().getMessageDate();
      assertTrue(previousDate <= dto.getSession().getMessageDate());
    }
  }

  @Test
  public void
      retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_When_CountIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST.size(), result.getSessions().size());
    assertEquals(
        result.getTotal(), Integer.valueOf(CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST.size()));
  }

  @Test
  public void
      retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_When_CountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());
    assertEquals(Integer.valueOf(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size()), result.getTotal());
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectOffset() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(OFFSET_0, result.getOffset());
  }

  @Test
  public void
      retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnNoSessionsIfOffsetIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(
            OFFSET_0 + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 1,
            COUNT_10,
            SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(COUNT_0, result.getSessions().size());
  }

  @Test
  public void
      retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_When_CountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(OFFSET_0, COUNT_1, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(COUNT_1, result.getSessions().size());
  }

  private SessionListQueryParameter createStandardSessionListQueryParameterObject(
      int offset, int count, SessionFilter sessionFilter) {
    return SessionListQueryParameter.builder()
        .sessionStatus(SESSION_STATUS_IN_PROGRESS)
        .offset(offset)
        .count(count)
        .sessionFilter(sessionFilter)
        .build();
  }
}
