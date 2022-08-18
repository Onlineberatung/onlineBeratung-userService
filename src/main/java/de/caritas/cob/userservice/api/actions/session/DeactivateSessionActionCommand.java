package de.caritas.cob.userservice.api.actions.session;

import static de.caritas.cob.userservice.api.model.Session.SessionStatus.DONE;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.session.SessionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deactivate action to set the status of a session to done. */
@Component
@RequiredArgsConstructor
public class DeactivateSessionActionCommand implements ActionCommand<Session> {

  private final @NonNull SessionService sessionService;

  /**
   * Sets a session to done.
   *
   * @param session the session id to change.
   */
  @Override
  public void execute(Session session) {
    if (DONE.equals(session.getStatus())) {
      throw new ConflictException(
          String.format("Session with id %s is already marked as done", session.getId()));
    }
    session.setStatus(DONE);
    sessionService.saveSession(session);
  }
}
