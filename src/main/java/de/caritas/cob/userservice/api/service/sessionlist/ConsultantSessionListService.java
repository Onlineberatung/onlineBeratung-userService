package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.session.SessionFilter;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConsultantSessionListService {

  private final @NonNull SessionService sessionService;
  private final @NonNull ChatService chatService;
  private final @NonNull ConsultantSessionEnricher consultantSessionEnricher;
  private final @NonNull ConsultantChatEnricher consultantChatEnricher;

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id and
   * status.
   *
   * @param consultant                {@link Consultant}
   * @param rcAuthToken               Rocket.Chat Token
   * @param sessionListQueryParameter session list query parameters as {@link SessionListQueryParameter}
   * @return the response dto
   */
  public List<ConsultantSessionResponseDTO> retrieveSessionsForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> sessions = retrieveSessionsForStatus(consultant,
        sessionListQueryParameter.getSessionStatus());
    List<ConsultantSessionResponseDTO> chats = new ArrayList<>();

    if (SessionStatus.isStatusValueInProgress(sessionListQueryParameter.getSessionStatus())) {
      chats = chatService.getChatsForConsultant(consultant);
    }

    return mergeConsultantSessionsAndChats(consultant, sessions, chats, rcAuthToken);
  }

  private List<ConsultantSessionResponseDTO> retrieveSessionsForStatus(Consultant consultant,
      Integer status) {
    var sessionStatus = getVerifiedSessionStatus(status);

    if (sessionStatus.equals(SessionStatus.NEW)) {
      return this.sessionService.getRegisteredEnquiriesForConsultant(consultant);
    }
    if (sessionStatus.equals(SessionStatus.IN_PROGRESS)) {
      return this.sessionService.getActiveAndDoneSessionsForConsultant(consultant);
    }
    return emptyList();
  }

  private SessionStatus getVerifiedSessionStatus(Integer status) {
    return SessionStatus.valueOf(status)
        .orElseThrow(() -> new BadRequestException(String.format(
            "Invalid session status %s ", status)));
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id.
   *
   * @param consultant                the {@link Consultant}
   * @param rcAuthToken               the Rocket.Chat auth token
   * @param sessionListQueryParameter session list query parameters as {@link SessionListQueryParameter}
   * @return a {@link ConsultantSessionListResponseDTO} with a {@link List} of {@link
   * ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> retrieveTeamSessionsForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> teamSessions =
        sessionService.getTeamSessionsForConsultant(consultant);

    updateConsultantSessionValues(teamSessions, rcAuthToken, consultant);
    sortSessionsByLastMessageDateDesc(teamSessions);

    if (sessionListQueryParameter.getSessionFilter().equals(SessionFilter.FEEDBACK)) {
      removeAllChatsAndSessionsWithoutUnreadFeedback(teamSessions);
    }

    return teamSessions;
  }

  private List<ConsultantSessionResponseDTO> mergeConsultantSessionsAndChats(
      Consultant consultant, List<ConsultantSessionResponseDTO> sessions,
      List<ConsultantSessionResponseDTO> chats, String rcAuthToken) {
    List<ConsultantSessionResponseDTO> allSessions = new ArrayList<>();

    if (isNotEmpty(sessions)) {
      allSessions.addAll(
          updateConsultantSessionValues(sessions, rcAuthToken, consultant));
    }

    if (isNotEmpty(chats)) {
      allSessions.addAll(
          updateConsultantChatValues(chats, rcAuthToken, consultant));
    }
    return allSessions;
  }

  private void sortSessionsByLastMessageDateDesc(List<ConsultantSessionResponseDTO> sessions) {
    sessions.sort(Comparator.comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());
  }

  private void removeAllChatsAndSessionsWithoutUnreadFeedback(
      List<ConsultantSessionResponseDTO> sessions) {
    sessions.removeIf(
        consultantSessionResponseDTO -> nonNull(consultantSessionResponseDTO.getChat())
            || isTrue(consultantSessionResponseDTO.getSession().getFeedbackRead()));
  }

  private List<ConsultantSessionResponseDTO> updateConsultantSessionValues(
      List<ConsultantSessionResponseDTO> sessions, String rcAuthToken, Consultant consultant) {
    return this.consultantSessionEnricher
        .updateRequiredConsultantSessionValues(sessions, rcAuthToken, consultant);
  }

  private List<ConsultantSessionResponseDTO> updateConsultantChatValues(
      List<ConsultantSessionResponseDTO> chats, String rcAuthToken, Consultant consultant) {
    return this.consultantChatEnricher.updateRequiredConsultantChatValues(chats, rcAuthToken,
        consultant);
  }

}

