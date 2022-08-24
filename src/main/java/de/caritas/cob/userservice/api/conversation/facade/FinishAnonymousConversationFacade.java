package de.caritas.cob.userservice.api.conversation.facade;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.actions.session.PostConversationFinishedAliasMessageActionCommand;
import de.caritas.cob.userservice.api.actions.session.SendFinishedAnonymousConversationEventActionCommand;
import de.caritas.cob.userservice.api.actions.session.SetRocketChatRoomReadOnlyActionCommand;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.session.SessionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate logic to finish an anonymous conversation. */
@Service
@RequiredArgsConstructor
public class FinishAnonymousConversationFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull ActionsRegistry actionsRegistry;

  /**
   * Finishes the anonymous session with given id.
   *
   * @param sessionId the session id
   */
  public void finishConversation(Long sessionId) {
    var session =
        this.sessionService
            .getSession(sessionId)
            .orElseThrow(
                () -> new NotFoundException("Session with id %s does not exist", sessionId));

    this.actionsRegistry
        .buildContainerForType(User.class)
        .addActionToExecute(DeactivateKeycloakUserActionCommand.class)
        .executeActions(session.getUser());

    this.actionsRegistry
        .buildContainerForType(Session.class)
        .addActionToExecute(DeactivateSessionActionCommand.class)
        .addActionToExecute(PostConversationFinishedAliasMessageActionCommand.class)
        .addActionToExecute(SetRocketChatRoomReadOnlyActionCommand.class)
        .addActionToExecute(SendFinishedAnonymousConversationEventActionCommand.class)
        .executeActions(session);
  }
}
