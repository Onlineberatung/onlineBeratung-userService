package de.caritas.cob.userservice.api.actions.registry;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Container class to collect fluently actions to peform.
 *
 * @param <T> the type the actions are implemented for
 */
@RequiredArgsConstructor
public class ActionContainer<T> {

  private final @NonNull Set<ActionCommand<T>> allActionsByType;
  private final Set<ActionCommand<T>> actionsToExecute = new LinkedHashSet<>();

  /**
   * Adds the {@link ActionCommand} to the current instance.
   *
   * @param actionToAdd the {@link ActionCommand} to add
   * @return the current {@link ActionContainer}
   */
  public ActionContainer<T> addActionToExecute(Class<? extends ActionCommand<T>> actionToAdd) {
    actionsToExecute.add(
        allActionsByType.stream()
            .filter(actionCommand -> actionCommand.getClass().equals(actionToAdd))
            .findFirst()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        String.format(
                            "ActionCommand class %s does not exist or is not implemented yet",
                            actionToAdd.getSimpleName()))));
    return this;
  }

  /**
   * Executes the collected {@link ActionCommand}s on given execution target.
   *
   * @param executionTarget the execution target
   */
  public void executeActions(T executionTarget) {
    actionsToExecute.forEach(sessionActionCommand -> sessionActionCommand.execute(executionTarget));
  }
}
