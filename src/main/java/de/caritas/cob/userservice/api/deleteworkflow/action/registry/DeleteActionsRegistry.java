package de.caritas.cob.userservice.api.deleteworkflow.action.registry;

import static java.util.Comparator.comparingInt;

import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteConsultantAction;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Registry for all delete action beans.
 */
@Component
@RequiredArgsConstructor
public class DeleteActionsRegistry {

  private final @NonNull ApplicationContext applicationContext;

  /**
   * Builds a ordered list of all available {@link DeleteAskerAction} beans.
   *
   * @return the available ordered {@link DeleteAskerAction} beans
   */
  public List<DeleteAskerAction> getAskerDeleteActions() {
    return getActionsForTypeOrderedBy(DeleteAskerAction.class, DeleteAskerAction::getOrder);
  }

  private <T> List<T> getActionsForTypeOrderedBy(Class<T> input, ToIntFunction<T> comparing) {
    return this.applicationContext.getBeansOfType(input)
        .values()
        .stream()
        .sorted(comparingInt(comparing))
        .collect(Collectors.toList());
  }

  /**
   * Builds a ordered list of all available {@link DeleteConsultantAction} beans.
   *
   * @return the available ordered {@link DeleteConsultantAction} beans
   */
  public List<DeleteConsultantAction> getConsultantDeleteActions() {
    return getActionsForTypeOrderedBy(DeleteConsultantAction.class,
        DeleteConsultantAction::getOrder);
  }

}
