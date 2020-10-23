package de.caritas.cob.userservice.api.service;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.userservice.api.exception.UpdateSessionException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.Now;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForUserDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.SessionUserDTO;
import de.caritas.cob.userservice.api.model.UserDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;

/**
 * Service for Sessions
 */
@Service
public class SessionService {

  private SessionRepository sessionRepository;
  private AgencyServiceHelper agencyServiceHelper;
  private Now now;
  private SessionDataHelper sessionDataHelper;
  private final UserHelper userHelper;

  @Autowired
  public SessionService(SessionRepository sessionRepository,
      AgencyServiceHelper agencyServiceHelper, Now now, SessionDataHelper sessionDataHelper,
      UserHelper userHelper) {
    this.sessionRepository = sessionRepository;
    this.agencyServiceHelper = agencyServiceHelper;
    this.now = now;
    this.sessionDataHelper = sessionDataHelper;
    this.userHelper = userHelper;
  }

  /**
   * Returns the sessions for a user
   *
   * @return the sessions
   */
  public List<Session> getSessionsForUser(User user) {

    List<Session> userSessions = null;

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
   * @param session the session
   * @param consultant the consultant
   * @param status s´the status of the session
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
   * @param session an optional session
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
      List<Session> sessions = sessionRepository.findByUser_UserId(userId);
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
   * @param user the user
   * @param userDto the dto of the user
   * @param monitoring flag to initialize monitoring
   * @return the initialized session
   */
  public Session initializeSession(User user, UserDTO userDto, boolean monitoring)
      throws AgencyServiceHelperException {
    AgencyDTO agencyDTO = agencyServiceHelper.getAgency(userDto.getAgencyId());
    return saveSession(
        new Session(user, ConsultingType.values()[Integer.parseInt(userDto.getConsultingType())],
            userDto.getPostcode(), userDto.getAgencyId(), SessionStatus.INITIAL,
            agencyDTO.isTeamAgency(), monitoring));
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

    if (sessions != null) {
      sessionDTOs = sessions.stream().map(session -> convertToConsultantSessionReponseDTO(session))
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
            if (consultantAgencies != null) {
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
            "Invalid session status %s submitted for consultant %s", status, consultant.getId()), LogService::logBadRequestException);
      }
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error", LogService::logDatabaseError);
    }

    if (sessions != null) {
      sessionDTOs = sessions.stream().map(this::convertToConsultantSessionReponseDTO)
          .collect(Collectors.toList());
    }

    return sessionDTOs;
  }

  /**
   * Converts a {@link List} of {@link Session}s to a {@link List} of {@link UserSessionResponseDTO}
   * and adds the corresponding agency information to the session from the provided {@link List} of
   * {@link AgencyDTO}s
   *
   * @param sessions {@link List} of {@link Session}
   * @param agencies {@link List} of {@link AgencyDTO}
   * @return {@link List} of {@link UserSessionResponseDTO>}
   */
  private List<UserSessionResponseDTO> convertToUserSessionResponseDTO(List<Session> sessions,
      List<AgencyDTO> agencies) {

    List<UserSessionResponseDTO> userSessionList = new ArrayList<>();

    for (Session session : sessions) {
      userSessionList.add(new UserSessionResponseDTO(convertToSessionDTO(session),
          agencies.stream()
              .filter(agency -> agency.getId().longValue() == session.getAgencyId().longValue())
              .findAny().get(),
          session.getConsultant() == null ? null
              : convertToSessionConsultantForUserDTO(session.getConsultant())));
    }

    return userSessionList;
  }

  /**
   * Converts a {@link Session} to a {@link ConsultantSessionResponseDTO}
   */
  private ConsultantSessionResponseDTO convertToConsultantSessionReponseDTO(Session session) {
    return new ConsultantSessionResponseDTO(convertToSessionDTO(session),
        convertToSessionUserDTO(session),
        convertToSessionConsultantForConsultantDTO(session.getConsultant()));
  }

  /**
   * Converts a {@link Session} to a {@link SessionDTO}
   */
  private SessionDTO convertToSessionDTO(Session session) {
    return new SessionDTO(session.getId(), session.getAgencyId(),
        session.getConsultingType().getValue(), session.getStatus().getValue(),
        session.getPostcode(), session.getGroupId(),
        session.getFeedbackGroupId() != null ? session.getFeedbackGroupId() : null,
        session.getUser() != null && session.getUser().getRcUserId() != null
            ? session.getUser().getRcUserId()
            : null,
        Helper.getUnixTimestampFromDate(session.getEnquiryMessageDate()), session.isTeamSession(),
        session.isMonitoring());
  }

  /**
   * Converts a {@link Consultant} to a {@link SessionConsultantForUserDTO}
   */
  private SessionConsultantForUserDTO convertToSessionConsultantForUserDTO(Consultant consultant) {
    return new SessionConsultantForUserDTO(consultant.getUsername(), consultant.isAbsent(),
        consultant.getAbsenceMessage());
  }

  /**
   * Converts a {@link Consultant} to a {@link SessionConsultantForConsultantDTO}. Only returns the
   * object if the currently authenticated user has the authority to view all peer session (is main
   * consultant).
   */
  private SessionConsultantForConsultantDTO convertToSessionConsultantForConsultantDTO(
      Consultant consultant) {

    return consultant != null
        ? new SessionConsultantForConsultantDTO(consultant.getId(), consultant.getFirstName(),
        consultant.getLastName())
        : null;
  }

  /**
   * Converts a {@link Session} to a {@link SessionUserDTO}
   */
  private SessionUserDTO convertToSessionUserDTO(Session session) {

    if (session.getUser() != null && session.getSessionData() != null) {
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
   * @param userId Rocket.Chat user id
   * @param roles user roles
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

    if (userSessions != null && !userSessions.isEmpty()) {
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
   * @param feedbackGroupId the id of the feedbackgroup
   * @return the session
   */
  public Session getSessionByFeedbackGroupId(String feedbackGroupId) {

    List<Session> sessions;

    try {
      sessions = sessionRepository.findByFeedbackGroupId(feedbackGroupId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String.format(
          "Database error while retrieving session by feedbackGroupId %s", feedbackGroupId),
          LogService::logDatabaseError);
    }

    if (sessions != null && !sessions.isEmpty()) {
      if (sessions.size() == 1) {
        // There should be only one session with this Rocket.Chat feedback group id and user id
        // combination
        return sessions.get(0);
      }
      throw new InternalServerErrorException(String.format(
          "More than one matching session found by feedbackGroupId %s in database. Aborting due to corrupt data.",
          feedbackGroupId));
    }

    return null;
  }

}
