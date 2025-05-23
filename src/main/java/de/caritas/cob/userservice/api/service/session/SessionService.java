package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import com.google.api.client.util.Lists;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionConsultantForUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionTopicDTO;
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
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.SessionTopic;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import jakarta.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Service for sessions */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull AgencyService agencyService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserService userService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @Nullable ConsultantSessionTopicEnrichmentService sessionTopicEnrichmentService;

  @Value("${feature.topics.enabled}")
  private boolean topicsFeatureEnabled;

  /**
   * Returns the sessions for a user
   *
   * @return the sessions
   */
  public List<Session> getSessionsForUser(User user) {
    return sessionRepository.findByUser(user);
  }

  /**
   * Returns the session for the provided sessionId.
   *
   * @param sessionId the session ID
   * @return {@link Session}
   */
  public Optional<Session> getSession(Long sessionId) {
    return sessionRepository.findById(sessionId);
  }

  /**
   * Returns the sessions for the given user and consultingType.
   *
   * @param user {@link User}
   * @return list of {@link Session}
   */
  public List<Session> getSessionsForUserByConsultingTypeId(User user, int consultingTypeId) {
    return sessionRepository.findByUserAndConsultingTypeId(user, consultingTypeId);
  }

  /**
   * Updates the given session by assigning the provided consultant and {@link SessionStatus}.
   *
   * @param session the session
   * @param consultant the consultant
   * @param status the status of the session
   */
  public void updateConsultantAndStatusForSession(
      Session session, Consultant consultant, SessionStatus status) {
    session.setConsultant(consultant);
    session.setStatus(status);
    saveSession(session);
  }

  /**
   * Updates the feedback group id of the given {@link Session}.
   *
   * @param session an optional session
   * @param feedbackGroupId the ID of the feedback group
   */
  public void updateFeedbackGroupId(Session session, String feedbackGroupId)
      throws UpdateFeedbackGroupIdException {
    try {
      session.setFeedbackGroupId(feedbackGroupId);
      saveSession(session);

    } catch (InternalServerErrorException serviceException) {
      throw new UpdateFeedbackGroupIdException(
          String.format(
              "Could not update feedback group id %s for session %s",
              feedbackGroupId, session.getId()),
          serviceException);
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
    List<UserSessionResponseDTO> sessionResponseDTOs = new ArrayList<>();
    List<Session> sessions = sessionRepository.findByUserUserId(userId);
    if (isNotEmpty(sessions)) {
      List<Long> agencyIds =
          sessions.stream()
              .map(Session::getAgencyId)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
      List<AgencyDTO> agencies = agencyService.getAgencies(agencyIds);
      sessionResponseDTOs = convertToUserSessionResponseDTO(sessions, agencies);
    }
    return sessionResponseDTOs;
  }

  /**
   * Initialize a {@link Session} and assign given consultant directly.
   *
   * @param user the user
   * @param userDto the dto of the user
   * @return the initialized session
   */
  public Session initializeDirectSession(
      Consultant consultant, User user, UserDTO userDto, boolean isTeamSession) {
    var session =
        initializeSession(
            user, userDto, isTeamSession, RegistrationType.REGISTERED, SessionStatus.INITIAL);
    session.setConsultant(consultant);
    return saveSession(session);
  }

  /**
   * Initialize a {@link Session} as initial registered enquiry.
   *
   * @param user the user
   * @param userDto the dto of the user
   * @return the initialized session
   */
  public Session initializeSession(User user, UserDTO userDto, boolean isTeamSession) {
    return initializeSession(
        user, userDto, isTeamSession, RegistrationType.REGISTERED, SessionStatus.INITIAL);
  }

  /**
   * Initialize a {@link Session}.
   *
   * @param user {@link User}
   * @param userDto {@link UserDTO}
   * @param isTeamSession is team session flag
   * @param registrationType {@link RegistrationType}
   * @param sessionStatus {@link SessionStatus}
   * @return the initialized {@link Session}
   */
  public Session initializeSession(
      User user,
      UserDTO userDto,
      boolean isTeamSession,
      RegistrationType registrationType,
      SessionStatus sessionStatus) {
    var extendedConsultingTypeResponseDTO = obtainConsultingTypeSettings(userDto);

    var session =
        Session.builder()
            .user(user)
            .consultingTypeId(obtainCheckedConsultingTypeId(extendedConsultingTypeResponseDTO))
            .registrationType(registrationType)
            .postcode(userDto.getPostcode())
            .agencyId(userDto.getAgencyId())
            .languageCode(LanguageCode.de)
            .status(sessionStatus)
            .teamSession(isTeamSession)
            .isPeerChat(isTrue(extendedConsultingTypeResponseDTO.getIsPeerChat()))
            .createDate(nowInUtc())
            .updateDate(nowInUtc())
            .mainTopicId(userDto.getMainTopicId())
            .userGender(userDto.getUserGender())
            .userAge(userDto.getUserAge())
            .counsellingRelation(userDto.getCounsellingRelation())
            .referer(userDto.getReferer())
            .isConsultantDirectlySet(false)
            .build();
    session.setTenantId(TenantContext.getCurrentTenant());
    session.setSessionTopics(createSessionTopics(userDto.getTopicIds(), session));
    return saveSession(session);
  }

  private List<SessionTopic> createSessionTopics(
      Collection<Long> topicsOfInterest, Session session) {
    if (topicsOfInterest != null) {
      return topicsOfInterest.stream()
          .map(topicId -> createNewSessionTopic(session, topicId))
          .collect(Collectors.toList());
    } else {
      return Lists.newArrayList();
    }
  }

  private SessionTopic createNewSessionTopic(Session session, Long topicId) {
    return SessionTopic.builder()
        .topicId(topicId)
        .session(session)
        .createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now())
        .build();
  }

  private ExtendedConsultingTypeResponseDTO obtainConsultingTypeSettings(UserDTO userDTO) {
    return consultingTypeManager.getConsultingTypeSettings(userDTO.getConsultingType());
  }

  private Integer obtainCheckedConsultingTypeId(
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {
    var consultingTypeId = extendedConsultingTypeResponseDTO.getId();
    if (isNull(consultingTypeId)) {
      throw new BadRequestException("Consulting type id must not be null");
    }
    return consultingTypeId;
  }

  /**
   * Save a {@link Session} to the database.
   *
   * @param session the session
   * @return the {@link Session}
   */
  public Session saveSession(Session session) {
    return sessionRepository.save(session);
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

    Set<ConsultantAgency> consultantAgencies = consultant.getConsultantAgencies();
    if (nonNull(consultantAgencies)) {
      List<Long> consultantAgencyIds =
          consultantAgencies.stream()
              .map(ConsultantAgency::getAgencyId)
              .collect(Collectors.toList());

      sessions =
          sessionRepository
              .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
                  consultantAgencyIds, consultant, SessionStatus.IN_PROGRESS, true);
    }

    return mapSessionsToConsultantSessionDto(sessions);
  }

  /**
   * Retrieves all related registered enquiries of given {@link Consultant}.
   *
   * @param consultant the consultant
   * @return the related {@link ConsultantSessionResponseDTO}s
   */
  public List<ConsultantSessionResponseDTO> getRegisteredEnquiriesForConsultant(
      Consultant consultant) {
    Set<ConsultantAgency> consultantAgencies = consultant.getConsultantAgencies();
    if (isNotEmpty(consultantAgencies)) {
      return retrieveRegisteredEnquiriesForConsultantAgencies(consultantAgencies);
    }
    return emptyList();
  }

  private List<ConsultantSessionResponseDTO> retrieveRegisteredEnquiriesForConsultantAgencies(
      Set<ConsultantAgency> consultantAgencies) {
    List<Long> consultantAgencyIds =
        consultantAgencies.stream().map(ConsultantAgency::getAgencyId).collect(Collectors.toList());
    final List<Session> sessions = retrieveRegisteredSessions(consultantAgencyIds);
    return mapSessionsToConsultantSessionDto(sessions);
  }

  private List<Session> retrieveRegisteredSessions(List<Long> consultantAgencyIds) {
    return this.sessionRepository
        .findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
            consultantAgencyIds, SessionStatus.NEW, RegistrationType.REGISTERED);
  }

  /**
   * Retrieves all related active sessions of given {@link Consultant}.
   *
   * @param consultant the consultant
   * @return the related {@link ConsultantSessionResponseDTO}s
   */
  public List<ConsultantSessionResponseDTO> getActiveAndDoneSessionsForConsultant(
      Consultant consultant) {
    return Stream.of(
            getSessionsForConsultantByStatus(consultant, SessionStatus.IN_PROGRESS),
            getSessionsForConsultantByStatus(consultant, SessionStatus.DONE))
        .flatMap(Collection::stream)
        .map(session -> new SessionMapper().toConsultantSessionDto(session))
        .collect(Collectors.toList());
  }

  private List<Session> getSessionsForConsultantByStatus(
      Consultant consultant, SessionStatus sessionStatus) {
    return sessionRepository.findByConsultantAndStatus(consultant, sessionStatus);
  }

  private List<UserSessionResponseDTO> convertToUserSessionResponseDTO(
      List<Session> sessions, List<AgencyDTO> agencies) {
    return sessions.stream()
        .map(session -> buildUserSessionDTO(session, agencies))
        .collect(Collectors.toList());
  }

  private UserSessionResponseDTO buildUserSessionDTO(Session session, List<AgencyDTO> agencies) {
    return new UserSessionResponseDTO()
        .session(new SessionMapper().convertToSessionDTO(session))
        .agency(
            agencies.stream()
                .filter(agency -> agency.getId().longValue() == session.getAgencyId().longValue())
                .findAny()
                .orElse(null))
        .consultant(
            nonNull(session.getConsultant())
                ? convertToSessionConsultantForUserDTO(session.getConsultant())
                : null);
  }

  private SessionConsultantForUserDTO convertToSessionConsultantForUserDTO(Consultant consultant) {
    return new SessionConsultantForUserDTO(
        consultant.getId(),
        consultant.getUsername(),
        consultant.isAbsent(),
        consultant.getAbsenceMessage(),
        null);
  }

  /**
   * Delete a {@link Session}
   *
   * @param session the {@link Session}
   */
  public void deleteSession(Session session) {
    sessionRepository.delete(session);
  }

  /**
   * Retrieves user sessions by user ID and rocket chat group, or feedback group IDs
   *
   * @param userId the user ID
   * @param rcGroupIds rocket chat group or feedback group IDs
   * @param roles the roles of the given user
   * @return {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getSessionsByUserAndGroupOrFeedbackGroupIds(
      String userId, Set<String> rcGroupIds, Set<String> roles) {
    checkForAskerRoles(roles);
    var sessions = sessionRepository.findByGroupOrFeedbackGroupIds(rcGroupIds);
    sessions.forEach(session -> checkAskerPermissionForSession(session, userId, roles));
    List<AgencyDTO> agencies = fetchAgencies(sessions);
    return convertToUserSessionResponseDTO(sessions, agencies);
  }

  /**
   * Retrieves user sessions by user ID and session IDs
   *
   * @param userId the user ID
   * @param sessionIds the session IDs
   * @param roles the roles of the given user
   * @return {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getSessionsByUserAndSessionIds(
      String userId, Set<Long> sessionIds, Set<String> roles) {
    checkForAskerRoles(roles);
    var sessions =
        StreamSupport.stream(sessionRepository.findAllById(sessionIds).spliterator(), false)
            .collect(Collectors.toList());
    sessions.forEach(session -> checkAskerPermissionForSession(session, userId, roles));
    List<AgencyDTO> agencies = fetchAgencies(sessions);
    return convertToUserSessionResponseDTO(sessions, agencies);
  }

  private List<AgencyDTO> fetchAgencies(List<Session> sessions) {
    Set<Long> agencyIds =
        sessions.stream()
            .map(Session::getAgencyId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    return agencyService.getAgencies(new ArrayList<>(agencyIds));
  }

  /**
   * Retrieves consultant sessions by consultant ID and rocket chat group, or feedback group IDs
   *
   * @param consultant the ID of the consultant
   * @param rcGroupIds rocket chat group or feedback group IDs
   * @param roles the roles of the given consultant
   * @return {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO>
      getAllowedSessionsByConsultantAndGroupOrFeedbackGroupIds(
          Consultant consultant, Set<String> rcGroupIds, Set<String> roles) {
    checkForUserOrConsultantRole(roles);
    var sessions = sessionRepository.findByGroupOrFeedbackGroupIds(rcGroupIds);

    List<Session> allowedSessions =
        sessions.stream()
            .filter(session -> isConsultantPermittedToSession(consultant, session))
            .collect(Collectors.toList());

    return mapSessionsToConsultantSessionDto(allowedSessions);
  }

  /**
   * Retrieves consultant sessions by session IDs
   *
   * @param consultant the ID of the consultant
   * @param sessionIds the session IDs
   * @param roles the roles of the given consultant
   * @return {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getSessionsByIds(
      Consultant consultant, Set<Long> sessionIds, Set<String> roles) {
    checkForUserOrConsultantRole(roles);
    var sessions =
        StreamSupport.stream(sessionRepository.findAllById(sessionIds).spliterator(), false)
            .collect(Collectors.toList());
    sessions.forEach(session -> checkConsultantAssignment(consultant, session));
    return mapSessionsToConsultantSessionDto(sessions);
  }

  /**
   * Returns the session for the provided Rocket.Chat group ID. Logs a warning if the given user is
   * not allowed to access this session.
   *
   * @param rcGroupId Rocket.Chat group ID
   * @param userId Rocket.Chat user ID
   * @param roles user roles
   * @return {@link Session}
   */
  public Session getSessionByGroupIdAndUser(String rcGroupId, String userId, Set<String> roles) {
    var session = getSessionByGroupId(rcGroupId);
    checkUserPermissionForSession(session, userId, roles);

    return session;
  }

  public Session getSessionByGroupId(String rcGroupId) {
    return sessionRepository
        .findByGroupId(rcGroupId)
        .orElseThrow(() -> new NotFoundException("Session with groupId %s not found.", rcGroupId));
  }

  private void checkUserPermissionForSession(Session session, String userId, Set<String> roles) {
    checkForUserOrConsultantRole(roles);
    checkIfUserAndNotOwnerOfSession(session, userId, roles);
    checkIfConsultantAndNotAssignedToSessionOrAgency(session, userId, roles);
  }

  private void checkForUserOrConsultantRole(Set<String> roles) {
    if (!roles.contains(UserRole.USER.getValue())
        && !roles.contains(UserRole.CONSULTANT.getValue())) {
      throw new ForbiddenException(
          "No user or consultant role to retrieve sessions", LogService::logForbidden);
    }
  }

  private void checkForAskerRoles(Set<String> roles) {
    if (!roles.contains(UserRole.USER.getValue())
        && !roles.contains(UserRole.ANONYMOUS.getValue())
        && !roles.contains(UserRole.CONSULTANT.getValue())) {
      throw new ForbiddenException(
          "No user or consultant role to retrieve sessions", LogService::logForbidden);
    }
  }

  private void checkAskerPermissionForSession(Session session, String userId, Set<String> roles) {
    if ((roles.contains(UserRole.USER.getValue())
            || session.getRegistrationType() == RegistrationType.ANONYMOUS
                && roles.contains(UserRole.ANONYMOUS.getValue()))
        && session.getUser().getUserId().equals(userId)) {
      return;
    }
    throw new ForbiddenException(
        String.format("Asker %s not allowed to access session with ID %s", userId, session.getId()),
        LogService::logForbidden);
  }

  private void checkIfUserAndNotOwnerOfSession(Session session, String userId, Set<String> roles) {
    if (roles.contains(UserRole.USER.getValue()) && !session.getUser().getUserId().equals(userId)) {
      throw new ForbiddenException(
          String.format("User %s has no permission to access session %s", userId, session.getId()),
          LogService::logForbidden);
    }
  }

  private void checkIfConsultantAndNotAssignedToSessionOrAgency(
      Session session, String userId, Set<String> roles) {
    if (roles.contains(UserRole.CONSULTANT.getValue())) {
      var consultant = loadConsultantOrThrow(userId);
      checkPermissionForConsultantSession(session, consultant);
    }
  }

  private boolean isConsultantPermittedToSession(Consultant consultant, Session session) {
    try {
      checkConsultantAssignment(consultant, session);
    } catch (ForbiddenException e) {
      log.info(e.getMessage());
      return false;
    }
    return true;
  }

  private void checkConsultantAssignment(Consultant consultant, Session session) {
    if (session.isAdvisedBy(consultant)
        || isAllowedToAdvise(consultant, session)
        || isAnonymousEnquiryAndAllowedToAdviseConsultingType(consultant, session)) {
      return;
    }
    throw new ForbiddenException(
        String.format(
            "No permission for session %s by consultant %s", session.getId(), consultant.getId()));
  }

  private boolean isAllowedToAdvise(Consultant consultant, Session session) {
    return isTeamSessionOrNew(session)
        && session.getAgencyId() != null
        && consultant.isInAgency(session.getAgencyId());
  }

  private boolean isAnonymousEnquiryAndAllowedToAdviseConsultingType(
      Consultant consultant, Session session) {
    if (session.getStatus() != SessionStatus.NEW
        || session.getRegistrationType() != RegistrationType.ANONYMOUS) {
      return false;
    }
    var agencyIdsOfConsultant =
        consultant.getConsultantAgencies().stream()
            .map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toList());
    var consultingTypes =
        agencyService.getAgencies(agencyIdsOfConsultant).stream()
            .map(AgencyDTO::getConsultingType)
            .collect(Collectors.toSet());
    return consultingTypes.contains(session.getConsultingTypeId());
  }

  /**
   * Returns the session for the Rocket.Chat feedback group id.
   *
   * @param feedbackGroupId the id of the feedback group
   * @return the session
   */
  public Session getSessionByFeedbackGroupId(String feedbackGroupId) {
    return sessionRepository.findByFeedbackGroupId(feedbackGroupId).orElse(null);
  }

  /**
   * Returns a {@link ConsultantSessionDTO} for a specific session.
   *
   * @param sessionId the session ID to fetch
   * @param consultant the calling consultant
   * @return {@link ConsultantSessionDTO} entity for the specific session
   */
  public ConsultantSessionDTO fetchSessionForConsultant(
      @NonNull Long sessionId, @NonNull Consultant consultant) {

    var session =
        getSession(sessionId)
            .orElseThrow(() -> new NotFoundException("Session with id %s not found.", sessionId));

    checkPermissionForConsultantSession(session, consultant);
    return toConsultantSessionDTO(session);
  }

  private boolean isTeamSessionOrNew(Session session) {
    return session.isTeamSession() || SessionStatus.NEW == session.getStatus();
  }

  private Consultant loadConsultantOrThrow(String userId) {
    return consultantService.getConsultant(userId).orElseThrow(newBadRequestException(userId));
  }

  private Supplier<BadRequestException> newBadRequestException(String userId) {
    return () -> new BadRequestException("Consultant with id %s does not exist".formatted(userId));
  }

  private ConsultantSessionDTO toConsultantSessionDTO(Session session) {

    var consultantSessionDTO =
        new ConsultantSessionDTO()
            .isTeamSession(session.isTeamSession())
            .agencyId(session.getAgencyId())
            .agencyName(agencyService.getAgency(session.getAgencyId()).getName())
            .consultingType(session.getConsultingTypeId())
            .id(session.getId())
            .status(session.getStatus().getValue())
            .askerId(session.getUser().getUserId())
            .askerRcId(session.getUser().getRcUserId())
            .askerUserName(session.getUser().getUsername())
            .feedbackGroupId(session.getFeedbackGroupId())
            .groupId(session.getGroupId())
            .postcode(session.getPostcode())
            .consultantId(nonNull(session.getConsultant()) ? session.getConsultant().getId() : null)
            .consultantRcId(
                nonNull(session.getConsultant()) ? session.getConsultant().getRocketChatId() : null)
            .createDate(String.valueOf(session.getCreateDate()))
            .age(session.getUserAge())
            .gender(session.getUserGender())
            .counsellingRelation(session.getCounsellingRelation())
            .referer(session.getReferer());

    if (topicsFeatureEnabled) {
      consultantSessionDTO
          .mainTopic(new SessionTopicDTO().id(session.getMainTopicId()))
          .topics(
              session.getSessionTopics().stream()
                  .map(topic -> new SessionTopicDTO().id(topic.getTopicId()))
                  .collect(Collectors.toList()));
      sessionTopicEnrichmentService.enrichSessionWithMainTopicData(consultantSessionDTO);
      sessionTopicEnrichmentService.enrichSessionWithTopicsData(consultantSessionDTO);
    }

    return consultantSessionDTO;
  }

  private void checkPermissionForConsultantSession(Session session, Consultant consultant) {
    if (!session.isAdvisedBy(consultant)
        && !(session.isTeamSession() && consultant.isInAgency(session.getAgencyId()))) {
      throw new ForbiddenException(
          String.format(
              "No permission for session %s by consultant %s",
              session.getId(), consultant.getId()));
    }
  }

  /**
   * Retrieves all archived sessions of given {@link Consultant}.
   *
   * @param consultant the consultant
   * @return the related {@link ConsultantSessionResponseDTO}s
   */
  public List<ConsultantSessionResponseDTO> getArchivedSessionsForConsultant(
      Consultant consultant) {
    final List<Session> sessions = retrieveArchivedSessions(consultant);

    return mapSessionsToConsultantSessionDto(sessions);
  }

  private List<Session> retrieveArchivedSessions(Consultant consultant) {
    return this.sessionRepository.findByConsultantAndStatusOrderByUpdateDateDesc(
        consultant, SessionStatus.IN_ARCHIVE);
  }

  /**
   * Retrieves all archived team sessions of given {@link Consultant}.
   *
   * @param consultant the consultant
   * @return the related {@link ConsultantSessionResponseDTO}s
   */
  public List<ConsultantSessionResponseDTO> getArchivedTeamSessionsForConsultant(
      Consultant consultant) {
    final List<Session> sessions = retrieveArchivedTeamSessionsForConsultant(consultant);
    return mapSessionsToConsultantSessionDto(sessions);
  }

  private List<Session> retrieveArchivedTeamSessionsForConsultant(Consultant consultant) {
    Set<ConsultantAgency> consultantAgencies = consultant.getConsultantAgencies();
    if (isNotEmpty(consultantAgencies)) {
      List<Long> consultantAgencyIds =
          consultantAgencies.stream()
              .map(ConsultantAgency::getAgencyId)
              .collect(Collectors.toList());
      return this.sessionRepository
          .findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionIsTrueOrderByUpdateDateDesc(
              consultantAgencyIds, consultant, SessionStatus.IN_ARCHIVE);
    }
    return emptyList();
  }

  private List<ConsultantSessionResponseDTO> mapSessionsToConsultantSessionDto(
      List<Session> sessions) {
    if (nonNull(sessions)) {
      return sessions.stream()
          .map(session -> new SessionMapper().toConsultantSessionDto(session))
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  /**
   * Find one session by assigned consultant and user.
   *
   * @param consultant the consultant
   * @param user the user
   * @param consultingTypeId the id of the consulting type
   * @return an {@link Optional} of the result
   */
  public Optional<Session> findSessionByConsultantAndUserAndConsultingType(
      Consultant consultant, User user, Integer consultingTypeId) {
    if (nonNull(consultant) && nonNull(user)) {
      return sessionRepository.findByConsultantAndUserAndConsultingTypeId(
          consultant, user, consultingTypeId);
    }
    return Optional.empty();
  }

  public String findGroupIdByConsultantAndUser(String consultantId, String askerId) {

    Optional<Consultant> consultant = consultantService.getConsultant(consultantId);
    if (consultant.isEmpty()) {
      throw new BadRequestException("Consultant for given id %s not found".formatted(consultantId));
    }
    Optional<User> user = userService.getUser(askerId);
    if (user.isEmpty()) {
      throw new BadRequestException("Asker for given id %s not found".formatted(askerId));
    }

    List<Session> sessions =
        sessionRepository.findByConsultantAndUser(consultant.get(), user.get());

    if (sessions.size() != 1) {
      throw new BadRequestException(
          "No rocketchat group found for given consultant or consultant is assigned to multiple sessions");
    }

    return sessions.get(0).getGroupId();
  }

  public List<Session> findAllSessions() {
    return Lists.newArrayList(sessionRepository.findAll());
  }
}
