package de.caritas.cob.userservice.api.deactivateworkflow;

import java.util.List;

public interface WorkflowAction<T, E> {

  List<E> execute(T workflowTarget);
}
