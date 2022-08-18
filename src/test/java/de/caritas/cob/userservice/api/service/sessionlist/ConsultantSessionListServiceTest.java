package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_WITHOUT_FEEDBACK;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_SESSION_RESPONSE_DTO_WITH_FEEDBACK;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.COUNT_10;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.OFFSET_0;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_STATUS_NEW;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.jsoup.helper.Validate.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.session.SessionFilter;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantSessionListServiceTest {

  @InjectMocks private ConsultantSessionListService consultantSessionListService;
  @Mock private SessionService sessionService;
  @Mock private ChatService chatService;
  @Mock private ConsultantSessionEnricher consultantSessionEnricher;
  @Mock private ConsultantChatEnricher consultantChatEnricher;
  @Mock private RocketChatCredentials rocketChatCredentials;

  @Before
  public void setup() {
    when(sessionService.getRegisteredEnquiriesForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(this.consultantSessionEnricher.updateRequiredConsultantSessionValues(
            eq(CONSULTANT_SESSION_RESPONSE_DTO_LIST), any(), any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(this.consultantChatEnricher.updateRequiredConsultantChatValues(
            eq(List.of(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE)), any(), any()))
        .thenReturn(List.of(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE));
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_ReturnOnlySessions_WhenQueryParameterSessionStatusIsNew() {
    when(rocketChatCredentials.getRocketChatToken()).thenReturn(RC_TOKEN);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertFalse(result.isEmpty());
    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), result.size());
    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : result) {
      assertNull(consultantSessionResponseDTO.getChat());
      assertNotNull(consultantSessionResponseDTO.getSession());
    }
    verify(chatService, never()).getChatsForConsultant(CONSULTANT);
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_ShouldNot_SendChatsInEnquiryList() {
    when(rocketChatCredentials.getRocketChatToken()).thenReturn(RC_TOKEN);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, createStandardSessionListQueryParameterObject(SESSION_STATUS_NEW));

    assertNull(result.get(0).getChat());
    verify(chatService, never()).getChatsForConsultant(Mockito.any());
  }

  @Test
  public void retrieveSessionsForAuthenticatedConsultant_Should_MergeSessionsAndChats() {
    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(sessionService.getActiveAndDoneSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatCredentials.getRocketChatToken()).thenReturn(RC_TOKEN);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            CONSULTANT, createStandardSessionListQueryParameterObject(SESSION_STATUS_IN_PROGRESS));

    assertNotNull(result);
    assertEquals(
        result.size(),
        CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE.size()
            + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size());

    for (ConsultantSessionResponseDTO dto :
        CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE) {
      boolean containsChat = false;
      for (ConsultantSessionResponseDTO chat : result) {
        if (nonNull(dto.getChat()) && dto.getChat().equals(chat.getChat())) {
          containsChat = true;
          break;
        }
      }
      if (!containsChat) {
        fail("ResponseList does not contain all expected chats");
      }
    }

    for (ConsultantSessionResponseDTO dto : CONSULTANT_SESSION_RESPONSE_DTO_LIST) {
      boolean containsSession = false;
      for (ConsultantSessionResponseDTO session : result) {
        if (nonNull(dto.getSession()) && dto.getSession().equals(session.getSession())) {
          containsSession = true;
          break;
        }
      }
      if (!containsSession) {
        fail("ResponseList does not contain all expected sessions");
      }
    }
  }

  @Test
  public void
      retrieveTeamSessionsForAuthenticatedConsultant_Should_ReturnFilteredSessionList_WhenFeedbackFilter() {
    List<ConsultantSessionResponseDTO> responseDTOS =
        new ArrayList<>(
            asList(
                CONSULTANT_SESSION_RESPONSE_DTO_WITH_FEEDBACK,
                CONSULTANT_SESSION_RESPONSE_DTO_WITHOUT_FEEDBACK,
                CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE));
    when(sessionService.getTeamSessionsForConsultant(CONSULTANT)).thenReturn(responseDTOS);

    SessionListQueryParameter sessionListQueryParameter =
        SessionListQueryParameter.builder()
            .sessionStatus(SESSION_STATUS_NEW)
            .offset(OFFSET_0)
            .count(COUNT_10)
            .sessionFilter(SessionFilter.FEEDBACK)
            .build();

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getSession().getFeedbackRead());
  }

  @Test
  public void
      retrieveSessionsForAuthenticatedConsultant_Should_returnEmptyList_When_SessionStatusIsInitial() {
    SessionListQueryParameter sessionListQueryParameter =
        createStandardSessionListQueryParameterObject(0);

    List<ConsultantSessionResponseDTO> result =
        consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            CONSULTANT, RC_TOKEN, sessionListQueryParameter);

    assertEquals(0, result.size());
  }

  private SessionListQueryParameter createStandardSessionListQueryParameterObject(
      int sessionStatus) {
    return SessionListQueryParameter.builder()
        .sessionStatus(sessionStatus)
        .offset(OFFSET_0)
        .count(COUNT_10)
        .sessionFilter(SessionFilter.ALL)
        .build();
  }
}
