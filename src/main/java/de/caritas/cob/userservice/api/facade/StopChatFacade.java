package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.actions.chat.StopChatActionCommand;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/*
 * Facade to encapsulate the steps to stop a running chat session.
 */
@Service
@RequiredArgsConstructor
public class StopChatFacade {

  private final @NonNull ChatPermissionVerifier chatPermissionVerifier;
  private final @NonNull ActionsRegistry actionsRegistry;

  /**
   * Stops the given {@link Chat} and resets or deletes it depending on if it's repetitive or not.
   *
   * @param chat {@link Chat}
   * @param consultant {@link Consultant}
   */
  public void stopChat(Chat chat, Consultant consultant) {
    checkConsultantChatPermission(chat, consultant);

    this.actionsRegistry
        .buildContainerForType(Chat.class)
        .addActionToExecute(StopChatActionCommand.class)
        .executeActions(chat);
  }

  private void checkConsultantChatPermission(Chat chat, Consultant consultant) {
    if (!chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant)) {
      throw new ForbiddenException(
          String.format(
              "Consultant with id %s has no permission to stop chat with id %s",
              consultant.getId(), chat.getId()));
    }
  }
}
