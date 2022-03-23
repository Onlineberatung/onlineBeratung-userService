package de.caritas.cob.userservice.api.service.archive;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for archive functionality.
 */
@Service
@RequiredArgsConstructor
public class SessionArchiveService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull SessionArchivePermissionChecker sessionArchivePermissionChecker;
  private final @NonNull SessionArchiveValidator sessionArchiveValidator;

  /**
   * Archive a session.
   *
   * @param sessionId the session id
   */
  public void archiveSession(Long sessionId) {
    changeSessionStatus(sessionId,
        SessionStatus.IN_ARCHIVE,
        sessionArchiveValidator::isValidForArchiving,
        rocketChatService::setRoomReadOnly);
  }

  /**
   * Dearchive a session.
   *
   * @param sessionId the session id
   */
  public void dearchiveSession(Long sessionId) {
    changeSessionStatus(sessionId,
        SessionStatus.IN_PROGRESS,
        sessionArchiveValidator::isValidForDearchiving,
        rocketChatService::setRoomWriteable);
  }

  private void changeSessionStatus(Long sessionId, SessionStatus sessionStatusTo,
      Consumer<Session> sessionValidateMethod,
      ThrowingConsumer<String, RocketChatUserNotInitializedException> rcUpdateRoomStateMethod) {

    Session session = retrieveSession(sessionId);
    sessionArchivePermissionChecker.checkPermission(session);
    sessionValidateMethod.accept(session);
    executeArchiving(sessionStatusTo, rcUpdateRoomStateMethod, session);
  }

  private Session retrieveSession(Long sessionId) {
    return sessionRepository.findById(sessionId).orElseThrow(
        () -> new NotFoundException(String.format("Session with id %s not found.", sessionId)));
  }

  private void executeArchiving(SessionStatus sessionStatusTo,
      ThrowingConsumer<String, RocketChatUserNotInitializedException> rcUpdateRoomStateMethod,
      Session session) {
    try {
      setRocketChatRoomState(session.getGroupId(), rcUpdateRoomStateMethod);
      setRocketChatRoomState(session.getFeedbackGroupId(), rcUpdateRoomStateMethod);
      session.setStatus(sessionStatusTo);
      sessionRepository.save(session);
    } catch (InternalServerErrorException | RocketChatUserNotInitializedException ex) {
      throw new InternalServerErrorException(String
          .format("Could not archive/dearchive session %s for user %s",
              session.getId(), authenticatedUser.getUserId()), ex);
    }
  }

  private void setRocketChatRoomState(String rcRoomId,
      ThrowingConsumer<String, RocketChatUserNotInitializedException> rcUpdateRoomStateMethod)
      throws RocketChatUserNotInitializedException {
    if (isNotBlank(rcRoomId)) {
      rcUpdateRoomStateMethod.accept(rcRoomId);
    }
  }

}
