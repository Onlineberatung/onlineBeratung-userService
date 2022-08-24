package de.caritas.cob.userservice.api.facade.sessionlist;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.GroupSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.session.SessionFilter;
import de.caritas.cob.userservice.api.service.session.SessionMapper;
import de.caritas.cob.userservice.api.service.session.SessionTopicEnrichmentService;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionListService;
import de.caritas.cob.userservice.api.service.sessionlist.UserSessionListService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to get the session list for a user or consultant (read sessions
 * from database and get unread messages status from Rocket.Chat).
 */
@Slf4j
@Service
public class SessionListFacade {

  private final UserSessionListService userSessionListService;
  private final ConsultantSessionListService consultantSessionListService;

  @Value("${feature.topics.enabled}")
  private boolean topicsFeatureEnabled;

  @Autowired(required = false)
  SessionTopicEnrichmentService sessionTopicEnrichmentService;

  @Autowired
  public SessionListFacade(
      UserSessionListService userSessionListService,
      ConsultantSessionListService consultantSessionListService) {
    this.userSessionListService = requireNonNull(userSessionListService);
    this.consultantSessionListService = requireNonNull(consultantSessionListService);
  }

  /**
   * Returns a list of {@link UserSessionListResponseDTO} for the specified user ID with the session
   * list sorted by last message date descending.
   *
   * @param userId the user ID
   * @param rocketChatCredentials the rocket chat credentials
   * @return {@link UserSessionListResponseDTO}
   */
  public UserSessionListResponseDTO retrieveSortedSessionsForAuthenticatedUser(
      String userId, RocketChatCredentials rocketChatCredentials) {

    List<UserSessionResponseDTO> userSessions =
        userSessionListService.retrieveSessionsForAuthenticatedUser(userId, rocketChatCredentials);
    userSessions.sort(comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    return new UserSessionListResponseDTO().sessions(userSessions);
  }

  /**
   * Returns a list of {@link UserSessionListResponseDTO} for the specified user ID and rocket chat
   * group, or feedback group IDs, with the session list sorted by last message date descending.
   *
   * @param userId the user ID
   * @param rcGroupIds the group or feedback group IDs
   * @param rocketChatCredentials the rocket chat credentials
   * @param roles the roles of given user
   * @return {@link UserSessionListResponseDTO}
   */
  public GroupSessionListResponseDTO retrieveSessionsForAuthenticatedUserByGroupIds(
      String userId,
      List<String> rcGroupIds,
      RocketChatCredentials rocketChatCredentials,
      Set<String> roles) {
    List<UserSessionResponseDTO> userSessions =
        userSessionListService.retrieveSessionsForAuthenticatedUserAndGroupIds(
            userId, rcGroupIds, rocketChatCredentials, roles);
    userSessions.sort(comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    SessionMapper sessionMapper = new SessionMapper();
    var sessions =
        userSessions.stream()
            .map(sessionMapper::toGroupSessionResponse)
            .collect(Collectors.toList());

    return new GroupSessionListResponseDTO().sessions(sessions);
  }

  /**
   * Returns a list of {@link UserSessionListResponseDTO} for the specified user ID and session Ids,
   * with the session list sorted by last message date descending.
   *
   * @param userId the user ID
   * @param sessionIds the session IDs
   * @param rocketChatCredentials the rocket chat credentials
   * @param roles the roles of given user
   * @return {@link UserSessionListResponseDTO}
   */
  public GroupSessionListResponseDTO retrieveSessionsForAuthenticatedUserBySessionIds(
      String userId,
      List<Long> sessionIds,
      RocketChatCredentials rocketChatCredentials,
      Set<String> roles) {
    List<UserSessionResponseDTO> userSessions =
        userSessionListService.retrieveSessionsForAuthenticatedUserAndSessionIds(
            userId, sessionIds, rocketChatCredentials, roles);
    userSessions.sort(comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    SessionMapper sessionMapper = new SessionMapper();
    var sessions =
        userSessions.stream()
            .map(sessionMapper::toGroupSessionResponse)
            .collect(Collectors.toList());

    return new GroupSessionListResponseDTO().sessions(sessions);
  }

  public GroupSessionListResponseDTO retrieveChatsForUserByChatIds(
      List<Long> chatIds, RocketChatCredentials rocketChatCredentials) {
    var userChatSessions =
        userSessionListService.retrieveChatsForUserAndChatIds(chatIds, rocketChatCredentials);
    userChatSessions.sort(comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    SessionMapper sessionMapper = new SessionMapper();
    var sessions =
        userChatSessions.stream()
            .map(sessionMapper::toGroupSessionResponse)
            .collect(Collectors.toList());

    return new GroupSessionListResponseDTO().sessions(sessions);
  }

  /**
   * @param consultant the authenticated consultant
   * @param rcGroupIds the group or feedback group IDs
   * @param roles the roles of given consultant
   * @return {@link GroupSessionListResponseDTO}
   */
  public GroupSessionListResponseDTO retrieveSessionsForAuthenticatedConsultantByGroupIds(
      Consultant consultant, List<String> rcGroupIds, Set<String> roles) {
    List<ConsultantSessionResponseDTO> consultantSessions =
        consultantSessionListService.retrieveSessionsForConsultantAndGroupIds(
            consultant, rcGroupIds, roles);
    consultantSessions.sort(comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());

    SessionMapper sessionMapper = new SessionMapper();
    var sessions =
        consultantSessions.stream()
            .map(sessionMapper::toGroupSessionResponse)
            .collect(Collectors.toList());

    return new GroupSessionListResponseDTO().sessions(sessions);
  }

  /**
   * @param consultant the authenticated consultant
   * @param sessionIds the session IDs
   * @param roles the roles of given consultant
   * @return {@link GroupSessionListResponseDTO}
   */
  public GroupSessionListResponseDTO retrieveSessionsForAuthenticatedConsultantBySessionIds(
      Consultant consultant, List<Long> sessionIds, Set<String> roles) {
    List<ConsultantSessionResponseDTO> consultantSessions =
        consultantSessionListService.retrieveSessionsForConsultantAndSessionIds(
            consultant, sessionIds, roles);
    consultantSessions.sort(comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());

    SessionMapper sessionMapper = new SessionMapper();
    var sessions =
        consultantSessions.stream()
            .map(sessionMapper::toGroupSessionResponse)
            .collect(Collectors.toList());

    return new GroupSessionListResponseDTO().sessions(sessions);
  }

  public GroupSessionListResponseDTO retrieveChatsForConsultantByChatIds(
      Consultant consultant, List<Long> chatIds, RocketChatCredentials rocketChatCredentials) {
    List<ConsultantSessionResponseDTO> consultantChatSessions =
        consultantSessionListService.retrieveChatsForConsultantAndChatIds(
            consultant, chatIds, rocketChatCredentials.getRocketChatToken());
    consultantChatSessions.sort(
        comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());

    SessionMapper sessionMapper = new SessionMapper();
    var sessions =
        consultantChatSessions.stream()
            .map(sessionMapper::toGroupSessionResponse)
            .collect(Collectors.toList());

    return new GroupSessionListResponseDTO().sessions(sessions);
  }

  /**
   * Returns a {@link ConsultantSessionResponseDTO} with the session list for the specified
   * consultant with consideration of the query parameters.
   *
   * @param consultant {@link Consultant}
   * @param sessionListQueryParameter session list query parameters as {@link
   *     SessionListQueryParameter}
   * @return the response dto
   */
  public ConsultantSessionListResponseDTO retrieveSessionsDtoForAuthenticatedConsultant(
      Consultant consultant, SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> consultantSessions =
        consultantSessionListService.retrieveSessionsForAuthenticatedConsultant(
            consultant, sessionListQueryParameter);

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
    if (areMoreConsultantSessionsAvailable(
        sessionListQueryParameter.getOffset(), consultantSessions)) {
      consultantSessionsSublist =
          retrieveConsultantSessionsSublist(sessionListQueryParameter, consultantSessions);
    }

    return new ConsultantSessionListResponseDTO()
        .sessions(consultantSessionsSublist)
        .offset(sessionListQueryParameter.getOffset())
        .count(consultantSessionsSublist.size())
        .total(consultantSessions.size());
  }

  private void enrichWithTopicData(List<ConsultantSessionResponseDTO> consultantSessionsSublist) {
    if (consultantSessionsSublist != null) {
      consultantSessionsSublist.stream()
          .map(ConsultantSessionResponseDTO::getSession)
          .forEach(sessionTopicEnrichmentService::enrichSessionWithTopicData);
    }
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

    try {
      return consultantSessions.subList(queryParameterOffset, subListMax);
    } catch (Exception e) {
      log.error(
          "Internal Server Error: Error while processing retrieveConsultantSessionsSublist with "
              + "Parameters (queryParameterOffset: {}) - (queryParameterCount: {}) - "
              + "(queryParameterSum: {}) - (consultantSessionSize: {}) - (subListMax: {})",
          queryParameterOffset,
          queryParameterCount,
          queryParameterSum,
          consultantSessionSize,
          subListMax,
          e);
      return Collections.emptyList();
    }
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} with team sessions for the specified
   * consultant id.
   *
   * @param consultant the {@link Consultant}
   * @param rcAuthToken the Rocket.Chat auth token
   * @param sessionListQueryParameter session list query parameters as {@link
   *     SessionListQueryParameter}
   * @return a {@link ConsultantSessionListResponseDTO} with a {@link List} of {@link
   *     ConsultantSessionResponseDTO}
   */
  public ConsultantSessionListResponseDTO retrieveTeamSessionsDtoForAuthenticatedConsultant(
      Consultant consultant,
      String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> teamSessions =
        consultantSessionListService.retrieveTeamSessionsForAuthenticatedConsultant(
            consultant, rcAuthToken, sessionListQueryParameter);

    List<ConsultantSessionResponseDTO> teamSessionsSublist = new ArrayList<>();
    if (areMoreConsultantSessionsAvailable(sessionListQueryParameter.getOffset(), teamSessions)) {
      teamSessionsSublist =
          retrieveConsultantSessionsSublist(sessionListQueryParameter, teamSessions);
    }

    if (topicsFeatureEnabled) {
      enrichWithTopicData(teamSessionsSublist);
    }

    return new ConsultantSessionListResponseDTO()
        .sessions(teamSessionsSublist)
        .offset(sessionListQueryParameter.getOffset())
        .count(teamSessionsSublist.size())
        .total(teamSessions.size());
  }

  private void sortSessionsByLastMessageDateDesc(List<ConsultantSessionResponseDTO> sessions) {
    sessions.sort(comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());
  }

  private void removeAllChatsAndSessionsWithoutUnreadFeedback(
      List<ConsultantSessionResponseDTO> sessions) {

    sessions.removeIf(
        consultantSessionResponseDTO ->
            nonNull(consultantSessionResponseDTO.getChat())
                || isTrue(consultantSessionResponseDTO.getSession().getFeedbackRead()));
  }

  private boolean areMoreConsultantSessionsAvailable(
      int offset, List<ConsultantSessionResponseDTO> consultantSessions) {
    return CollectionUtils.isNotEmpty(consultantSessions) && offset < consultantSessions.size();
  }
}
