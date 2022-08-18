package de.caritas.cob.userservice.api.workflow.delete.model;

import de.caritas.cob.userservice.api.model.Consultant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsultantDeletionWorkflowDTO {

  private Consultant consultant;
  private List<DeletionWorkflowError> deletionWorkflowErrors;
}
