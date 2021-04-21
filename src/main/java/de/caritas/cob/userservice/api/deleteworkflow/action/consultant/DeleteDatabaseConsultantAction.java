package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.LAST;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link Consultant} in database.
 */
@Component
@RequiredArgsConstructor
public class DeleteDatabaseConsultantAction implements DeleteConsultantAction {

  private final @NonNull ConsultantRepository consultantRepository;

  /**
   * Deletes the given {@link Consultant} in database.
   *
   * @param consultant the {@link Consultant} to delete
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(Consultant consultant) {
    try {
      this.consultantRepository.delete(consultant);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(DATABASE)
              .identifier(consultant.getId())
              .reason("Unable to delete consultant in database")
              .timestamp(nowInUtc())
              .build()
      );
    }
    return emptyList();
  }

  /**
   * Provides the execution order.
   *
   * @return the value for the execution order
   */
  @Override
  public int getOrder() {
    return LAST.getOrder();
  }
}
