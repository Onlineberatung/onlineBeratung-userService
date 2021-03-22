package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.List;

public interface DeleteConsultantAction {

  List<DeletionWorkflowError> execute(Consultant userAccount);

  int getOrder();
}
