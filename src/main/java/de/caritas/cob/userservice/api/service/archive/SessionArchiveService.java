package de.caritas.cob.userservice.api.service.archive;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.ArchiveStatisticsEvent;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for archive functionality. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionArchiveService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull SessionArchiveValidator sessionArchiveValidator;
  private final @NonNull AccountManager accountManager;
  private final @NonNull StatisticsService statisticsService;

  /**
   * Archive a session.
   *
   * @param sessionId the session id
   */
  public void archiveSession(Long sessionId) {

    Session session = retrieveSession(sessionId);
    changeSessionStatus(
        sessionId,
        SessionStatus.IN_ARCHIVE,
        sessionArchiveValidator::isValidForArchiving,
        rocketChatService::setRoomReadOnly);
    fireArchiveEvent(session);
  }

  private void fireArchiveEvent(Session session) {
    try {
      ArchiveStatisticsEvent archiveStatisticsEvent =
          new ArchiveStatisticsEvent(session.getUser(), session.getId(), LocalDateTime.now());
      statisticsService.fireEvent(archiveStatisticsEvent);
    } catch (Exception e) {
      log.error("Could not create session archive statistics event", e);
    }
  }

  /**
   * Dearchive a session.
   *
   * @param sessionId the session id
   */
  public void dearchiveSession(Long sessionId) {
    changeSessionStatus(
        sessionId,
        SessionStatus.IN_PROGRESS,
        sessionArchiveValidator::isValidForDearchiving,
        rocketChatService::setRoomWriteable);
  }

  private void changeSessionStatus(
      Long sessionId,
      SessionStatus sessionStatusTo,
      Consumer<Session> sessionValidateMethod,
      ThrowingConsumer<String, RocketChatUserNotInitializedException> rcUpdateRoomStateMethod) {

    Session session = retrieveSession(sessionId);
    checkPermission(session);
    sessionValidateMethod.accept(session);
    executeArchiving(sessionStatusTo, rcUpdateRoomStateMethod, session);
  }

  public void checkPermission(Session session) {
    if (!hasConsultantPermission(session) && !session.isAdvised(authenticatedUser.getUserId())) {
      var template = "Archive/reactivate session %s is not allowed for user with id %s";
      var message = String.format(template, session.getId(), authenticatedUser.getUserId());

      throw new ForbiddenException(message);
    }
  }

  private boolean hasConsultantPermission(Session session) {
    var userId = authenticatedUser.getUserId();

    return session.isAdvisedBy(userId) || accountManager.isTeamAdvisedBy(session.getId(), userId);
  }

  private Session retrieveSession(Long sessionId) {
    return sessionRepository
        .findById(sessionId)
        .orElseThrow(() -> new NotFoundException("Session with id %s not found.", sessionId));
  }

  private void executeArchiving(
      SessionStatus sessionStatusTo,
      ThrowingConsumer<String, RocketChatUserNotInitializedException> rcUpdateRoomStateMethod,
      Session session) {
    try {
      setRocketChatRoomState(session.getGroupId(), rcUpdateRoomStateMethod);
      setRocketChatRoomState(session.getFeedbackGroupId(), rcUpdateRoomStateMethod);
      session.setStatus(sessionStatusTo);
      sessionRepository.save(session);
    } catch (InternalServerErrorException | RocketChatUserNotInitializedException ex) {
      throw new InternalServerErrorException(
          String.format(
              "Could not archive/dearchive session %s for user %s",
              session.getId(), authenticatedUser.getUserId()),
          ex);
    }
  }

  private void setRocketChatRoomState(
      String rcRoomId,
      ThrowingConsumer<String, RocketChatUserNotInitializedException> rcUpdateRoomStateMethod)
      throws RocketChatUserNotInitializedException {
    if (isNotBlank(rcRoomId)) {
      rcUpdateRoomStateMethod.accept(rcRoomId);
    }
  }
}
