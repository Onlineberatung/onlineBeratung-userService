package de.caritas.cob.userservice.api.deactivateworkflow.registry;

import de.caritas.cob.userservice.api.deactivateworkflow.AbstractDeactivateAction;
import de.caritas.cob.userservice.api.deactivateworkflow.session.DeactivateSessionAction;
import de.caritas.cob.userservice.api.deactivateworkflow.user.DeactivateKeycloakUserAction;
import de.caritas.cob.userservice.api.deactivateworkflow.user.DeactivateRocketChatUserAction;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Registry for all delete action beans.
 */
@Component
@RequiredArgsConstructor
public class DeactivateActionsRegistry {

  private final @NonNull ApplicationContext applicationContext;

  /**
   * Builds an ordered list of all available {@link AbstractDeactivateAction} beans with type of
   * {@link User}.
   *
   * @return the available ordered {@link AbstractDeactivateAction} beans
   */
  public List<AbstractDeactivateAction<User>> getUserDeactivateActions() {
    var actions = new ArrayList<AbstractDeactivateAction<User>>();
    actions.addAll(getActionsForTypeOrderedBy(DeactivateRocketChatUserAction.class));
    actions.addAll(getActionsForTypeOrderedBy(DeactivateKeycloakUserAction.class));
    return actions;
  }

  private <T> List<T> getActionsForTypeOrderedBy(Class<T> input) {
    return new ArrayList<>(this.applicationContext.getBeansOfType(input).values());
  }

  /**
   * Builds an ordered list of all available {@link AbstractDeactivateAction} beans with type of
   * {@link Session}.
   *
   * @return the available ordered {@link AbstractDeactivateAction} beans
   */
  public List<AbstractDeactivateAction<Session>> getSessionDeactivateActions() {
    return new ArrayList<>(getActionsForTypeOrderedBy(DeactivateSessionAction.class));
  }

}
