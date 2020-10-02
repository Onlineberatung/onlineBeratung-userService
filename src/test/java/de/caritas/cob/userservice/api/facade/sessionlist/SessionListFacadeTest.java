package de.caritas.cob.userservice.api.facade.sessionlist;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_FEEDBACK;
import static de.caritas.cob.userservice.testHelper.TestConstants.COUNT_0;
import static de.caritas.cob.userservice.testHelper.TestConstants.COUNT_1;
import static de.caritas.cob.userservice.testHelper.TestConstants.COUNT_10;
import static de.caritas.cob.userservice.testHelper.TestConstants.OFFSET_0;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_SESSION_RESPONSE_SESSION_CHAT_DTO_LIST;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionListService;
import de.caritas.cob.userservice.api.service.sessionlist.UserSessionListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionListFacadeTest {

  @InjectMocks
  private SessionListFacade sessionListFacade;
  @Mock
  private ConsultantSessionListService consultantSessionListService;
  @Mock
  private UserSessionListService userSessionListService;

  /**
   * Method: retrieveSessionsForAuthenticatedUser
   */

  @Test
  public void retrieveSessionsForAuthenticatedUser_Should_ReturnCorrectlySortedSessionList() {

    when(userSessionListService.retrieveSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS))
        .thenReturn(USER_SESSION_RESPONSE_SESSION_CHAT_DTO_LIST);

    UserSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO dto : result.getSessions()) {
      Long previousDate = (nonNull(dto.getSession())) ? dto.getSession().getMessageDate()
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

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());

    for (ConsultantSessionResponseDTO dto : result.getSessions()) {
      Long previousDate = (nonNull(dto.getSession())) ? dto.getSession().getMessageDate()
          : dto.getChat().getMessageDate();
      if (nonNull(dto.getSession())) {
        assertTrue(previousDate <= dto.getSession().getMessageDate());
      } else {
        assertTrue(previousDate <= dto.getChat().getMessageDate());
      }
    }
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getTotal());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() - 1,
        SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getTotal());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectOffset() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(OFFSET_0, result.getOffset());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnNoSessions_WhenOffsetIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0 + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 1, COUNT_10,
        SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(COUNT_0, result.getSessions().size());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_1, SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(COUNT_1, result.getSessions().size());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 5,
        SessionFilter.ALL);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_ReturnFilteredSessionList_WhenFeedbackFilterIsSet() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.FEEDBACK);

    when(consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_FEEDBACK);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(COUNT_1, result.getSessions().size());
    assertFalse(result.getSessions().get(0).getSession().isFeedbackRead());
  }

  /**
   * Method: retrieveTeamSessionsForAuthenticatedConsultant
   */

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectlySortedSessionList() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());

    for (ConsultantSessionResponseDTO dto : result.getSessions()) {
      Long previousDate = dto.getSession().getMessageDate();
      assertTrue(previousDate <= dto.getSession().getMessageDate());
    }
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST.size(), result.getSessions().size());
    assertEquals(result.getTotal(), CONSULTANT_SESSION_CHAT_RESPONSE_DTO_LIST.size());
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getSessions().size());
    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.getTotal());
  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectOffset() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_10, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(OFFSET_0, result.getOffset());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnNoSessionsIfOffsetIsGreaterThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0 + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 1, COUNT_10,
        SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(COUNT_0, result.getSessions().size());

  }

  @Test
  public void retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsSmallerThanTotal() {

    SessionListQueryParameter sessionListQueryParameter = createStandardSessionListQueryParameterObject(
        SESSION_STATUS_IN_PROGRESS, OFFSET_0, COUNT_1, SessionFilter.ALL);

    when(consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(CONSULTANT,
        RC_TOKEN, sessionListQueryParameter))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO result =
        sessionListFacade.retrieveTeamSessionsDtoForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            sessionListQueryParameter);

    assertEquals(COUNT_1, result.getSessions().size());
  }

  private SessionListQueryParameter createStandardSessionListQueryParameterObject(
      int sessionStatus, int offset, int count, SessionFilter sessionFilter) {
    return SessionListQueryParameter.builder()
        .sessionStatus(sessionStatus)
        .offset(offset)
        .count(count)
        .sessionFilter(sessionFilter)
        .build();
  }

}
