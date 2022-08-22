package de.caritas.cob.userservice.api.workflow.delete.model;

import de.caritas.cob.userservice.api.model.Session;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionDeletionWorkflowDTO {

  private Session session;
  private List<DeletionWorkflowError> deletionWorkflowErrors;
}
