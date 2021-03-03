package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.SECOND;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteDatabaseConsultantAgencyAction implements DeleteConsultantAction {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;

  /**
   * Deletes all {@link Consultant} regarding {@link ConsultantAgency} relations.
   *
   * @param consultant the {@link Consultant}
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(Consultant consultant) {
    try {
      List<ConsultantAgency> consultantAgencies = this.consultantAgencyRepository
          .findByConsultantId(consultant.getId());
      this.consultantAgencyRepository.deleteAll(consultantAgencies);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(DATABASE)
              .identifier(consultant.getId())
              .reason("Could not delete consultant agency relations")
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
    return SECOND.getOrder();
  }
}
