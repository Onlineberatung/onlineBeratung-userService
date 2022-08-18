package de.caritas.cob.userservice.api.workflow.delete.model;

import de.caritas.cob.userservice.api.model.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AskerDeletionWorkflowDTO {

  private User user;
  private List<DeletionWorkflowError> deletionWorkflowErrors;
}
