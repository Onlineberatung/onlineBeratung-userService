package de.caritas.cob.userservice.api.actions;

import static org.mockito.Mockito.mock;

import de.caritas.cob.userservice.api.actions.registry.ActionContainer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class ActionCommandMockProvider {

  private final Map<Class<? extends ActionCommand<?>>, ActionCommand<?>> actionMocks =
      new HashMap<>();

  @SuppressWarnings("unchecked")
  public ActionCommandMockProvider() {
    var reflections = new Reflections("de.caritas.cob.userservice.api");
    reflections.getSubTypesOf(ActionCommand.class)
        .forEach(actionClass -> actionMocks
            .put((Class<? extends ActionCommand<?>>) actionClass, mock(actionClass)));
  }

  @SuppressWarnings("unchecked")
  public <T> ActionCommand<T> getActionMock(Class<? extends ActionCommand<T>> actionClass) {
    return (ActionCommand<T>) this.actionMocks.get(actionClass);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> ActionContainer<T> getActionContainer(Class<T> classType) {
    HashSet<ActionCommand<T>> actionCommands = new HashSet(this.actionMocks.keySet().stream()
        .filter(actionClass -> byClassType(actionClass, classType))
        .map(this.actionMocks::get)
        .collect(Collectors.toUnmodifiableList()));
    return new ActionContainer(actionCommands);
  }

  private boolean byClassType(Class<?> actionCommand, Class<?> type) {
    return Arrays.stream(actionCommand.getGenericInterfaces())
        .map(Type::getTypeName)
        .anyMatch(typeName -> typeName.contains(type.getTypeName()));
  }

  public void setCustomClassForAction(Class<? extends ActionCommand<?>> key,
      ActionCommand<?> value) {
    this.actionMocks.remove(key);
    this.actionMocks.put(key, value);
  }

}
