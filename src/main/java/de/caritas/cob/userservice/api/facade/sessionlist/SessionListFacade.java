package de.caritas.cob.userservice.api.facade.sessionlist;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionListService;
import de.caritas.cob.userservice.api.service.sessionlist.UserSessionListService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to get the session list for a user or consultant (read sessions
 * from database and get unread messages status from Rocket.Chat).
 */
@Service
public class SessionListFacade {

  private final UserSessionListService userSessionListService;
  private final ConsultantSessionListService consultantSessionListService;

  @Autowired
  public SessionListFacade(UserSessionListService userSessionListService,
      ConsultantSessionListService consultantSessionListService) {
    this.userSessionListService = requireNonNull(userSessionListService);
    this.consultantSessionListService = requireNonNull(consultantSessionListService);
  }

  /**
   * Returns a list of {@link UserSessionListResponseDTO} for the specified user ID with the session
   * list sorted by last message date descending.
   *
   * @param userId                the user ID
   * @param rocketChatCredentials the rocket chat credentials
   * @return {@link UserSessionListResponseDTO}
   */
  public UserSessionListResponseDTO retrieveSortedSessionsForAuthenticatedUser(String userId,
      RocketChatCredentials rocketChatCredentials) {

    List<UserSessionResponseDTO> userSessions = userSessionListService
        .retrieveSessionsForAuthenticatedUser(userId, rocketChatCredentials);
    userSessions.sort(Comparator.comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    return new UserSessionListResponseDTO().sessions(userSessions);

  }

  /**
   * Returns a {@link ConsultantSessionResponseDTO} with the session list for the specified
   * consultant with consideration of the query parameters.
   *
   * @param consultant                {@link Consultant}
   * @param rcAuthToken               Rocket.Chat Token
   * @param sessionListQueryParameter session list query parameters as {@link SessionListQueryParameter}
   * @return the response dto
   */
  public ConsultantSessionListResponseDTO retrieveSessionsDtoForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> consultantSessions = consultantSessionListService
        .retrieveSessionsForAuthenticatedConsultant(consultant, rcAuthToken,
            sessionListQueryParameter);

    /* Sort the session list by latest Rocket.Chat message if session is in progress (no enquiry).
     * The latest answer is on top.
     *
     * Please note: Enquiry message sessions are being sorted by the repository (via
     * SessionService). Here the latest enquiry message is on the bottom.
     */
    if (SessionStatus.isStatusValueInProgress(sessionListQueryParameter.getSessionStatus())) {
      sortSessionsByLastMessageDateDesc(consultantSessions);
    }

    if (isFeedbackFilter(sessionListQueryParameter)) {
      removeAllChatsAndSessionsWithoutUnreadFeedback(consultantSessions);
    }

    List<ConsultantSessionResponseDTO> consultantSessionsSublist = new ArrayList<>();
    if (areMoreConsultantSessionsAvailable(sessionListQueryParameter.getOffset(),
        consultantSessions)) {
      consultantSessionsSublist = retrieveConsultantSessionsSublist(sessionListQueryParameter,
          consultantSessions);
    }

    return new ConsultantSessionListResponseDTO()
        .sessions(consultantSessionsSublist)
        .offset(sessionListQueryParameter.getOffset())
        .count(consultantSessionsSublist.size())
        .total(consultantSessions.size());
  }

  private boolean isFeedbackFilter(SessionListQueryParameter sessionListQueryParameter) {
    return sessionListQueryParameter.getSessionFilter().equals(SessionFilter.FEEDBACK);
  }

  private List<ConsultantSessionResponseDTO> retrieveConsultantSessionsSublist(
      SessionListQueryParameter sessionListQueryParameter,
      List<ConsultantSessionResponseDTO> consultantSessions) {
    int queryParameterOffset = sessionListQueryParameter.getOffset();
    int queryParameterCount = sessionListQueryParameter.getCount();
    int queryParameterSum = queryParameterOffset + queryParameterCount;
    int consultantSessionSize = consultantSessions.size();
    int subListMax = Math.min(queryParameterSum, consultantSessionSize);

    List<ConsultantSessionResponseDTO> consultantSessionsSubList;

    try {

      consultantSessionsSubList = consultantSessions
          .subList(queryParameterOffset, subListMax);
    } catch (Exception e) {
      LogService.logInternalServerError(String.format(
          "Error while processing retrieveConsultantSessionsSublist with Parameters (queryParameterOffset: %s) - (queryParameterCount: %s) - (queryParameterSum: %s) - (consultantSessionSize: %s) - (subListMax: %s)",
          queryParameterOffset, queryParameterCount, queryParameterSum, consultantSessionSize,
          subListMax), e);
      consultantSessionsSubList = new ArrayList<>();
    }

    return consultantSessionsSubList;
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} with team sessions for the specified
   * consultant id.
   *
   * @param consultant                the {@link Consultant}
   * @param rcAuthToken               the Rocket.Chat auth token
   * @param sessionListQueryParameter session list query parameters as {@link SessionListQueryParameter}
   * @return a {@link ConsultantSessionListResponseDTO} with a {@link List} of {@link
   * ConsultantSessionResponseDTO}
   */
  public ConsultantSessionListResponseDTO retrieveTeamSessionsDtoForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> teamSessions = consultantSessionListService
        .retrieveTeamSessionsForAuthenticatedConsultant(consultant, rcAuthToken,
            sessionListQueryParameter);

    List<ConsultantSessionResponseDTO> teamSessionsSublist = new ArrayList<>();
    if (areMoreConsultantSessionsAvailable(sessionListQueryParameter.getOffset(), teamSessions)) {
      teamSessionsSublist =
          retrieveConsultantSessionsSublist(sessionListQueryParameter, teamSessions);
    }

    return new ConsultantSessionListResponseDTO()
        .sessions(teamSessionsSublist)
        .offset(sessionListQueryParameter.getOffset())
        .count(teamSessionsSublist.size())
        .total(teamSessions.size());
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

  private boolean areMoreConsultantSessionsAvailable(
      int offset, List<ConsultantSessionResponseDTO> consultantSessions) {
    return
        CollectionUtils.isNotEmpty(consultantSessions)
            && offset < consultantSessions.size();
  }

}
