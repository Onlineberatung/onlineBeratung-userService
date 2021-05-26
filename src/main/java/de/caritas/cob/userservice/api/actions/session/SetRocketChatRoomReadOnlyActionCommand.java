package de.caritas.cob.userservice.api.actions.session;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sets the rooms to read only in Rocket.Chat.
 */
@Component
@RequiredArgsConstructor
public class SetRocketChatRoomReadOnlyActionCommand implements ActionCommand<Session> {

  /**
   * Sets the Rocket.Chat rooms to read only.
   *
   * @param session the session with groups to deactivate in Rocket.Chat.
   */
  @Override
  public void execute(Session session) {
    LogService.logDebug("Nothing to do in DeactivateRocketChatUserAction");
  }

}
