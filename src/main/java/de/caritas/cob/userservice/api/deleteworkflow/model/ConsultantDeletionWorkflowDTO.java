package de.caritas.cob.userservice.api.deleteworkflow.model;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsultantDeletionWorkflowDTO {

  private Consultant consultant;
  private List<DeletionWorkflowError> deletionWorkflowErrors;

}
