package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.List;

public interface DeleteAskerAction {

  List<DeletionWorkflowError> execute(User userAccount);

  int getOrder();
}
