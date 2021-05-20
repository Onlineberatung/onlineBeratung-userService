package de.caritas.cob.userservice.api.deactivateworkflow;

import java.util.List;

public interface WorkflowAction<TargetT, ErrorT> {

  List<ErrorT> execute(TargetT workflowTarget);
}
