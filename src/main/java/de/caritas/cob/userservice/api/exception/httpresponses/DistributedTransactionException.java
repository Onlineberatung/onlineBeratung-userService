package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.service.LogService;
import org.springframework.http.HttpHeaders;

public class DistributedTransactionException extends CustomHttpStatusException {

  private final HttpHeaders customHttpHeaders;

  public DistributedTransactionException(
      Exception e, DistributedTransactionInfo distributedTransactionInfo) {
    super(
        getFormattedMessageWithDistributedTransactionInfo(distributedTransactionInfo),
        e,
        LogService::logError);
    this.customHttpHeaders =
        buildCustomHeaders(
            "DISTRIBUTED_TRANSACTION_FAILED_ON_STEP_"
                + distributedTransactionInfo.getFailedStep().name());
  }

  private HttpHeaders buildCustomHeaders(String message) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Reason", message);
    return headers;
  }

  private static String getFormattedMessageWithDistributedTransactionInfo(
      DistributedTransactionInfo distributedTransactionInfo) {
    return String.format(
        "Distributed transaction %s failed. Completed transactional operations: %s. Failed step: %s",
        distributedTransactionInfo.getName(),
        distributedTransactionInfo.getCompletedTransactionalOperations(),
        distributedTransactionInfo.getFailedStep());
  }

  public HttpHeaders getCustomHttpHeaders() {
    return customHttpHeaders;
  }
}
