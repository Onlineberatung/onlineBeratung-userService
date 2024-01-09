package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.admin.service.consultant.TransactionalStep;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@AllArgsConstructor
public class DistributedTransactionInfo {

  String name;
  List<TransactionalStep> completedTransactionalOperations;
  TransactionalStep failedStep;
}
