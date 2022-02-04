package de.caritas.cob.userservice.api.workflow.delete.model;

import de.caritas.cob.userservice.api.repository.user.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AskerDeletionWorkflowDTO {

  private User user;
  private List<DeletionWorkflowError> deletionWorkflowErrors;

}
