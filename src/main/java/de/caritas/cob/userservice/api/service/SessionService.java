package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.userservice.api.exception.UpdateSessionException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.user.SessionConsultantForUserDTO;
import de.caritas.cob.userservice.api.model.user.SessionUserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Service for sessions
 */
@Service
@RequiredArgsConstructor
public class SessionService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull SessionDataHelper sessionDataHelper;
  private final @NonNull UserHelper userHelper;

  /**
   * Returns the sessions for a user
   *
   * @return the sessions
   */
  public List<Session> getSessionsForUser(User user) {

    List<Session> userSessions;

    try {
      userSessions = sessionRepository.findByUser(user);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException(String.format(
          "Database error while retrieving sessions for user with id %s", user.getUserId()));
    }

    return userSessions;
  }

  /**
   * Returns the session for the provided sessionId.
   *
   * @param sessionId the session id
   * @return {@link Session}
   */
  public Optional<Session> getSession(Long sessionId) {
    Optional<Session> session;

    try {
      session = sessionRepository.findById(sessionId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Database error while retrieving session with id %s", sessionId),
          LogService::logDatabaseError);
    }

    return session;
  }

  /**
   * Returns the sessions for the given user and consultingType.
   *
   * @param user {@link User}
   * @return list of {@link Session}
   */
  public List<Session> getSessionsForUserByConsultingType(User user,
      ConsultingType consultingType) {

    List<Session> userSessions;

    try {
      userSessions = sessionRepository.findByUserAndConsultingType(user, consultingType);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while retrieving user sessions",
          LogService::logDatabaseError);
    }

    return userSessions != null ? userSessions : Collections.emptyList();
  }

  /**
   * Updates the given session by assigning the provided consultant and {@link SessionStatus}.
   *
   * @param session    the session
   * @param consultant the consultant
   * @param status     s´the status of the session
   */
  public void updateConsultantAndStatusForSession(Session session, Consultant consultant,
      SessionStatus status) throws UpdateSessionException {

    try {
      session.setConsultant(consultant);
      session.setStatus(status);
      saveSession(session);
    } catch (InternalServerErrorException serviceException) {
      throw new UpdateSessionException(serviceException);
    }
  }

  /**
   * Updates the feedback group id of the given {@link Session}.
   *
   * @param session         an optional session
   * @param feedbackGroupId the id of the feedback group
   */
  public void updateFeedbackGroupId(Optional<Session> session, String feedbackGroupId)
      throws UpdateFeedbackGroupIdException {
    try {
      session.get().setFeedbackGroupId(feedbackGroupId);
      saveSession(session.get());

    } catch (InternalServerErrorException serviceException) {
      throw new UpdateFeedbackGroupIdException(
          String.format("Could not update feedback group id %s for session %s", feedbackGroupId,
              session.get().getId()), serviceException);
    }
  }

  /**
   * Returns a list of current sessions (no matter if an enquiry message has been written or not)
   * for the provided user ID.
   *
   * @param userId Keycloak/MariaDB user ID
   * @return {@link List} of {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getSessionsForUserId(String userId) {

    try {
      List<UserSessionResponseDTO> sessionResponseDTOs = new ArrayList<>();
      List<Session> sessions = sessionRepository.findByUserUserId(userId);
      if (isNotEmpty(sessions)) {
        List<AgencyDTO> agencies = agencyServiceHelper.getAgencies(
            sessions.stream().map(Session::getAgencyId).collect(Collectors.toList()));
        sessionResponseDTOs = convertToUserSessionResponseDTO(sessions, agencies);
      }
      return sessionResponseDTOs;
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String.format(
          "Database error while retrieving the sessions for the user with id %s", userId),
          LogService::logInternalServerError);

    } catch (AgencyServiceHelperException helperEx) {
      throw new InternalServerErrorException(String.format(
          "AgencyService error while retrieving the agency for the session for user %s", userId),
          LogService::logAgencyServiceHelperException);
    }
  }

  /**
   * Initialize a {@link Session}.
   *
   * @param user                   the user
   * @param userDto                the dto of the user
   * @param consultingTypeSettings flag to initialize monitoring
   * @return the initialized session
   */
  public Session initializeSession(User user, UserDTO userDto, boolean isTeamSession,
      ConsultingTypeSettings consultingTypeSettings) {
    Session session = new Session(user, consultingTypeSettings.getConsultingType(),
        userDto.getPostcode(), userDto.getAgencyId(), SessionStatus.INITIAL,
        isTeamSession, consultingTypeSettings.isMonitoring());
    session.setCreateDate(nowInUtc());
    session.setUpdateDate(nowInUtc());
    return saveSession(session);
  }

  /**
   * Save a {@link Session} to the database.
   *
   * @param session the session
   * @return the {@link Session}
   */
  public Session saveSession(Session session) {
    try {
      return sessionRepository.save(session);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Database error while saving session with id %s", session.getId()),
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} containing team sessions excluding
   * sessions which are taken by the consultant.
   *
   * @param consultant the consultant
   * @return A list of {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getTeamSessionsForConsultant(Consultant consultant) {

    List<Session> sessions = null;

    try {
      Set<ConsultantAgency> consultantAgencies = consultant.getConsultantAgencies();
      if (consultantAgencies != null) {
        List<Long> consultantAgencyIds = consultantAgencies.stream()
            .map(ConsultantAgency::getAgencyId).collect(Collectors.toList());

        sessions = sessionRepository
            .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
                consultantAgencyIds, consultant, SessionStatus.IN_PROGRESS, true);
      }

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String.format(
          "Database error while getting the team sessions for consultant %s", consultant.getId()),
          LogService::logDatabaseError);

    }

    List<ConsultantSessionResponseDTO> sessionDTOs = null;

    if (nonNull(sessions)) {
      sessionDTOs = sessions.stream().map(this::convertToConsultantSessionResponseDTO)
          .collect(Collectors.toList());
    }

    return sessionDTOs;
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the given consultant and session
   * status.
   *
   * @param status The submitted {@link SessionStatus}
   * @return A list of {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getSessionsForConsultant(Consultant consultant,
      Integer status) {

    List<Session> sessions = null;
    Optional<SessionStatus> sessionStatus;
    List<ConsultantSessionResponseDTO> sessionDTOs = null;

    try {
      sessionStatus = SessionStatus.valueOf(status);

      if (sessionStatus.isPresent()) {
        switch (sessionStatus.get()) {
          case NEW:

            Set<ConsultantAgency> consultantAgencies = consultant.getConsultantAgencies();
            if (nonNull(consultantAgencies)) {
              List<Long> consultantAgencyIds = consultantAgencies.stream()
                  .map(ConsultantAgency::getAgencyId).collect(Collectors.toList());

              sessions = sessionRepository
                  .findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
                      consultantAgencyIds, SessionStatus.NEW);
            }
            break;

          case IN_PROGRESS:
            sessions =
                sessionRepository.findByConsultantAndStatus(consultant, SessionStatus.IN_PROGRESS);
            break;

          default:
            break;
        }
      } else {
        throw new BadRequestException(String.format(
            "Invalid session status %s submitted for consultant %s", status, consultant.getId()),
            LogService::logBadRequestException);
      }
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error", LogService::logDatabaseError);
    }

    if (nonNull(sessions)) {
      sessionDTOs = sessions.stream().map(this::convertToConsultantSessionResponseDTO)
          .collect(Collectors.toList());
    }

    return sessionDTOs;
  }

  private List<UserSessionResponseDTO> convertToUserSessionResponseDTO(List<Session> sessions,
      List<AgencyDTO> agencies) {

    List<UserSessionResponseDTO> userSessionList = new ArrayList<>();

    for (Session session : sessions) {
      userSessionList.add(new UserSessionResponseDTO()
          .session(convertToSessionDTO(session))
          .agency(agencies.stream()
              .filter(agency -> agency.getId().longValue() == session.getAgencyId().longValue())
              .findAny().get())
          .consultant(nonNull(session.getConsultant()) ? convertToSessionConsultantForUserDTO(
              session.getConsultant()) : null));
    }

    return userSessionList;
  }

  private ConsultantSessionResponseDTO convertToConsultantSessionResponseDTO(Session session) {
    return new ConsultantSessionResponseDTO()
        .session(convertToSessionDTO(session))
        .user(convertToSessionUserDTO(session))
        .consultant(convertToSessionConsultantForConsultantDTO(session.getConsultant()));
  }

  private SessionDTO convertToSessionDTO(Session session) {
    return new SessionDTO()
        .id(session.getId())
        .agencyId(session.getAgencyId())
        .consultingType(session.getConsultingType().getValue())
        .status(session.getStatus().getValue())
        .postcode(session.getPostcode())
        .groupId(session.getGroupId())
        .feedbackGroupId(
            nonNull(session.getFeedbackGroupId()) ? session.getFeedbackGroupId() : null)
        .askerRcId(nonNull(session.getUser()) && nonNull(session.getUser().getRcUserId())
            ? session.getUser().getRcUserId()
            : null)
        .messageDate(Helper.getUnixTimestampFromDate(session.getEnquiryMessageDate()))
        .isTeamSession(session.isTeamSession())
        .monitoring(session.isMonitoring());
  }

  private SessionConsultantForUserDTO convertToSessionConsultantForUserDTO(Consultant consultant) {

    return new SessionConsultantForUserDTO(consultant.getUsername(), consultant.isAbsent(),
        consultant.getAbsenceMessage());
  }

  private SessionConsultantForConsultantDTO convertToSessionConsultantForConsultantDTO(
      Consultant consultant) {

    return nonNull(consultant) ? new SessionConsultantForConsultantDTO()
        .id(consultant.getId())
        .firstName(consultant.getFirstName())
        .lastName(consultant.getLastName()) : null;
  }

  private SessionUserDTO convertToSessionUserDTO(Session session) {

    if (nonNull(session.getUser()) && nonNull(session.getSessionData())) {
      SessionUserDTO sessionUserDto = new SessionUserDTO();
      sessionUserDto.setUsername(userHelper.decodeUsername(session.getUser().getUsername()));
      sessionUserDto.setSessionData(sessionDataHelper.getSessionDataMapFromSession(session));
      return sessionUserDto;
    }

    return null;
  }

  /**
   * Delete a {@link Session}
   *
   * @param session the {@link Session}
   */
  public void deleteSession(Session session) {
    try {
      sessionRepository.delete(session);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Deletion of session with id %s failed", session.getId()),
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns the session for the specified user id and Rocket.Chat group id depending on the user's
   * role.
   *
   * @param rcGroupId Rocket.Chat group id
   * @param userId    Rocket.Chat user id
   * @param roles     user roles
   * @return {@link Session}
   */
  public Session getSessionByGroupIdAndUserId(String rcGroupId, String userId, Set<String> roles) {

    List<Session> userSessions = null;

    try {
      if (roles.contains(UserRole.USER.getValue())) {
        userSessions = sessionRepository.findByGroupIdAndUserUserId(rcGroupId, userId);
      }
      if (roles.contains(UserRole.CONSULTANT.getValue())) {
        userSessions = sessionRepository.findByGroupIdAndConsultantId(rcGroupId, userId);
      }
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Database error while retrieving user sessions by groupId %s and userId %s",
              rcGroupId, userId), LogService::logDatabaseError);
    }

    if (nonNull(userSessions) && !userSessions.isEmpty()) {
      if (userSessions.size() == 1) {
        // There should be only one session with this Rocket.Chat group id and user id combination
        return userSessions.get(0);
      }
      throw new InternalServerErrorException(String.format(
          "More than one matching session found by groupId %s and userId %s in database. Aborting due to corrupt data.",
          rcGroupId, userId));
    }

    return null;
  }

  /**
   * Returns the session for the Rocket.Chat feedback group id.
   *
   * @param feedbackGroupId the id of the feedback group
   * @return the session
   */
  public Session getSessionByFeedbackGroupId(String feedbackGroupId) {
    try {
      return sessionRepository.findByFeedbackGroupId(feedbackGroupId).orElse(null);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String.format(
          "Database error while retrieving session by feedbackGroupId %s", feedbackGroupId),
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns a {@link ConsultantSessionDTO} for a specific session.
   *
   * @param sessionId  the session id to fetch
   * @param consultant the calling consultant
   * @return {@link ConsultantSessionDTO} entity for the specific session
   */
  public ConsultantSessionDTO fetchSessionForConsultant(@NonNull Long sessionId,
      @NonNull Consultant consultant) {

    Session session = getSession(sessionId)
        .orElseThrow(
            () -> new NotFoundException(String.format("Session with id %s not found.", sessionId)));
    checkPermissionForConsultantSession(session, consultant);
    return toConsultantSessionDTO(session);
  }

  private ConsultantSessionDTO toConsultantSessionDTO(Session session) {

    return new ConsultantSessionDTO()
        .isTeamSession(session.isTeamSession())
        .agencyId(session.getAgencyId())
        .consultingType(session.getConsultingType().getValue())
        .id(session.getId())
        .status(session.getStatus().getValue())
        .askerId(session.getUser().getUserId())
        .askerRcId(session.getUser().getRcUserId())
        .askerUserName(session.getUser().getUsername())
        .feedbackGroupId(session.getFeedbackGroupId())
        .groupId(session.getGroupId())
        .isMonitoring(session.isMonitoring())
        .postcode(session.getPostcode())
        .consultantId(nonNull(session.getConsultant()) ? session.getConsultant().getId() : null)
        .consultantRcId(
            nonNull(session.getConsultant()) ? session.getConsultant().getRocketChatId() : null);
  }

  private void checkPermissionForConsultantSession(Session session, Consultant consultant) {

    if (!isConsultantAssignedToSession(session, consultant)
        && !(session.isTeamSession() && isConsultantAssignedToSessionAgency(consultant, session))) {
      throw new ForbiddenException(String
          .format("No permission for session %s by consultant %s", session.getId(),
              consultant.getId()));
    }
  }

  private boolean isConsultantAssignedToSession(Session session, Consultant consultant) {
    return nonNull(session.getConsultant())
        && session.getConsultant().getId().equals(consultant.getId());
  }

  private boolean isConsultantAssignedToSessionAgency(Consultant consultant, Session session) {
    return consultant.getConsultantAgencies().stream()
        .anyMatch(consultantAgency -> consultantAgency.getAgencyId().equals(session.getAgencyId()));
  }

}
