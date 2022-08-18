package de.caritas.cob.userservice.api.actions.registry;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Registry to provide {@link ActionContainer} for {@link ActionCommand}s by given type. */
@Component
@RequiredArgsConstructor
public class ActionsRegistry {

  private final @NonNull ApplicationContext applicationContext;

  /**
   * Builds an {@link ActionContainer} for all available {@link ActionCommand} beans with type of
   * the given class.
   *
   * @return the {@link ActionContainer}
   */
  public <T> ActionContainer<T> buildContainerForType(Class<T> type) {
    return new ActionContainer<>(getActionsForType(type));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> Set<ActionCommand<T>> getActionsForType(Class<T> type) {
    return new HashSet(
        this.applicationContext.getBeansOfType(ActionCommand.class).values().stream()
            .filter(actionCommand -> byClassType(actionCommand, type))
            .collect(Collectors.toUnmodifiableList()));
  }

  private <T> boolean byClassType(ActionCommand<T> actionCommand, Class<T> type) {
    return Arrays.stream(actionCommand.getClass().getGenericInterfaces())
        .map(Type::getTypeName)
        .anyMatch(typeName -> typeName.contains(type.getTypeName()));
  }
}
