package de.caritas.cob.UserService.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.UserRole;
import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.exception.EnquiryMessageException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.UserService.api.exception.UpdateSessionException;
import de.caritas.cob.UserService.api.exception.httpresponses.WrongParameterException;
import de.caritas.cob.UserService.api.helper.Helper;
import de.caritas.cob.UserService.api.helper.Now;
import de.caritas.cob.UserService.api.helper.SessionDataHelper;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.UserService.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.UserService.api.model.SessionConsultantForUserDTO;
import de.caritas.cob.UserService.api.model.SessionDTO;
import de.caritas.cob.UserService.api.model.SessionUserDTO;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.model.UserSessionResponseDTO;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionRepository;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

/**
 * Service for Sessions
 */
@Service
public class SessionService {

  private SessionRepository sessionRepository;
  private AgencyServiceHelper agencyServiceHelper;
  private LogService logService;
  private Now now;
  private SessionDataHelper sessionDataHelper;
  private final UserHelper userHelper;

  @Autowired
  public SessionService(SessionRepository sessionRepository,
      AgencyServiceHelper agencyServiceHelper, LogService logService, Now now,
      SessionDataHelper sessionDataHelper, UserHelper userHelper) {
    this.sessionRepository = sessionRepository;
    this.agencyServiceHelper = agencyServiceHelper;
    this.logService = logService;
    this.now = now;
    this.sessionDataHelper = sessionDataHelper;
    this.userHelper = userHelper;
  }

  /**
   * Returns the sessions for a user
   * 
   * @param user
   * @return the sessions
   */
  public List<Session> getSessionsForUser(User user) {

    List<Session> userSessions = null;

    try {
      userSessions = sessionRepository.findByUser(user);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(String.format(
          "Database error while retrieving sessions for user with id %s", user.getUserId()));
    }

    return userSessions;
  }

  /**
   * Returns the session for the provided sessionId
   * 
   * @param sessionId
   * @return {@link Session}
   */
  public Optional<Session> getSession(Long sessionId) {
    Optional<Session> session;

    try {
      session = sessionRepository.findById(sessionId);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(
          String.format("Database error while retrieving session with id %s", sessionId));
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

    List<Session> userSessions = null;

    try {
      userSessions = sessionRepository.findByUserAndConsultingType(user, consultingType);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error while retrieving user sessions");
    }

    return userSessions != null ? userSessions : Collections.emptyList();
  }

  /**
   * Updates the given session by assigning the provided consultant and {@link SessionStatus}
   * 
   * @param session
   * @param consultant
   * @param status
   */
  public void updateConsultantAndStatusForSession(Session session, Consultant consultant,
      SessionStatus status) {

    try {
      session.setConsultant(consultant);
      session.setStatus(status);
      saveSession(session);
    } catch (ServiceException serviceException) {
      throw new UpdateSessionException(serviceException);
    }
  }

  /**
   * Updates the feedback group id of the given {@link Session}
   * 
   * @param session
   * @param feedbackGroupId
   */
  public void updateFeedbackGroupId(Optional<Session> session, String feedbackGroupId) {
    try {
      session.get().setFeedbackGroupId(feedbackGroupId);
      saveSession(session.get());

    } catch (ServiceException serviceException) {
      logService
          .logDatabaseError(String.format("Could not update feedback group id %s for session %s",
              feedbackGroupId, session.get().getId()), serviceException);
      throw new UpdateFeedbackGroupIdException(serviceException);
    }
  }

  /**
   * Saving the enquiry message and Rocket.Chat group id for a session. The Message will be set to
   * now and the status to {@link SessionStatus#NEW}.
   * 
   * @param session
   * @param rcGroupId
   * @return the {@link Session}
   * @throws EnquiryMessageException
   */
  public Session saveEnquiryMessageDateAndRocketChatGroupId(Session session, String rcGroupId)
      throws EnquiryMessageException {

    session.setGroupId(rcGroupId);
    session.setEnquiryMessageDate(now.getDate());
    session.setStatus(SessionStatus.NEW);
    try {
      saveSession(session);
    } catch (ServiceException serviceException) {
      CreateEnquiryExceptionInformation exceptionInformation =
          CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();
      throw new EnquiryMessageException(serviceException, exceptionInformation);
    }

    return session;
  }

  /**
   * Returns a list of current sessions (no matter if an enquiry message has been written or not)
   * for the provided user ID.
   * 
   * @param userId Keycloak/MariaDB user ID
   * @return {@link List} of {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getSessionsForUserId(String userId) {

    List<Session> sessions = null;
    List<UserSessionResponseDTO> sessionResponseDTOs = new ArrayList<>();

    try {
      sessions = sessionRepository.findByUser_UserId(userId);
      if (sessions != null && sessions.size() > 0) {
        List<AgencyDTO> agencies = agencyServiceHelper.getAgencies(
            sessions.stream().map(session -> session.getAgencyId()).collect(Collectors.toList()));
        sessionResponseDTOs = convertToUserSessionResponseDTO(sessions, agencies);
      }

    } catch (DataAccessException ex) {
      throw new ServiceException(String.format(
          "Database error while retrieving the sessions for the user with id %s", userId), ex);

    } catch (AgencyServiceHelperException helperEx) {
      logService.logAgencyServiceHelperException(helperEx);
      throw new ServiceException(String.format(
          "AgencyService error while retrieving the agency for the session for user %s", userId));
    }

    return sessionResponseDTOs;
  }

  /**
   * Initialize a {@link Session}
   * 
   * @param user
   * @param userDto
   */
  public Session initializeSession(User user, UserDTO userDto, boolean monitoring) {
    AgencyDTO agencyDTO = agencyServiceHelper.getAgency(userDto.getAgencyId());
    return saveSession(
        new Session(user, ConsultingType.values()[Integer.valueOf(userDto.getConsultingType())],
            userDto.getPostcode(), userDto.getAgencyId(), SessionStatus.INITIAL,
            agencyDTO.isTeamAgency(), monitoring));
  }

  /**
   * Save a {@link Session} to the database
   * 
   * @param session
   * @return the {@link Session}
   */
  public Session saveSession(Session session) throws ServiceException {
    try {
      return sessionRepository.save(session);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(
          String.format("Database error while saving session with id %s", session.getId()), ex);
    }
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} containing team sessions excluding
   * sessions which are taken by the consultant
   *
   * @param consultant
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
      logService.logDatabaseError(ex);
      throw new ServiceException(String.format(
          "Database error while getting the team sessions for consultant %s", consultant.getId()));

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
   * @param consultant
   * @param status The submitted {@link SessionStatus}
   * @return A list of {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getSessionsForConsultant(Consultant consultant,
      Integer status) {

    List<Session> sessions = null;
    Optional<SessionStatus> sessionStatus = null;
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
        throw new WrongParameterException(String.format(
            "Invalid session status %s submitted for consultant %s", status, consultant.getId()));
      }
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error");
    }

    if (sessions != null) {
      sessionDTOs = sessions.stream().map(session -> convertToConsultantSessionReponseDTO(session))
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
   * 
   * @param session
   * @return
   */
  private ConsultantSessionResponseDTO convertToConsultantSessionReponseDTO(Session session) {
    return new ConsultantSessionResponseDTO(convertToSessionDTO(session),
        convertToSessionUserDTO(session),
        convertToSessionConsultantForConsultantDTO(session.getConsultant()));
  }

  /**
   * Converts a {@link Session} to a {@link SessionDTO}
   * 
   * @param session
   * @return
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
   * 
   * @param consultant
   * @return
   */
  private SessionConsultantForUserDTO convertToSessionConsultantForUserDTO(Consultant consultant) {
    return new SessionConsultantForUserDTO(consultant.getUsername(), consultant.isAbsent(),
        consultant.getAbsenceMessage());
  }

  /**
   * Converts a {@link Consultant} to a {@link SessionConsultantForConsultantDTO}. Only returns the
   * object if the currently authenticated user has the authority to view all peer session (is main
   * consultant).
   * 
   * @param consultant
   * @return
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
   * 
   * @param session
   * @return
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
      logService.logDatabaseError(ex);
      throw new ServiceException(
          String.format("Deletion of session with id %s failed", session.getId()));
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
      logService.logDatabaseError(ex);
      throw new ServiceException(
          String.format("Database error while retrieving user sessions by groupId %s and userId %s",
              rcGroupId, userId));
    }

    if (userSessions != null && !userSessions.isEmpty()) {
      if (userSessions.size() == 1) {
        // There should be only one session with this Rocket.Chat group id and user id combination
        return userSessions.get(0);
      }
      if (userSessions.size() > 1) {
        throw new ServiceException(String.format(
            "More than one matching session found by groupId %s and userId %s in database. Aborting due to corrupt data.",
            rcGroupId, userId));
      }
    }

    return null;
  }

  /**
   * Returns the session for the Rocket.Chat feedback group id
   * 
   * @param feedbackGroupId
   * @return
   */
  public Session getSessionByFeedbackGroupId(String feedbackGroupId) {

    List<Session> sessions = null;

    try {
      sessions = sessionRepository.findByFeedbackGroupId(feedbackGroupId);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(String.format(
          "Database error while retrieving session by feedbackGroupId %s", feedbackGroupId));
    }

    if (sessions != null && !sessions.isEmpty()) {
      if (sessions.size() == 1) {
        // There should be only one session with this Rocket.Chat feedback group id and user id
        // combination
        return sessions.get(0);
      }
      if (sessions.size() > 1) {
        throw new ServiceException(String.format(
            "More than one matching session found by feedbackGroupId %s in database. Aborting due to corrupt data.",
            feedbackGroupId));
      }
    }

    return null;
  }

}
