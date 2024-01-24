package de.caritas.cob.userservice.api;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.admin.service.consultant.TransactionalStep;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionException;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionInfo;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatchConsultantSagaRollbackHandler {

  private final MessageClient messageClient;

  public void rollbackUpdateUserInRocketchat(Consultant savedConsultant) {
    try {
      var originalDisplayName =
          messageClient
              .findUser(savedConsultant.getRocketChatId())
              .get()
              .get("displayName")
              .toString();
      messageClient.updateUser(savedConsultant.getRocketChatId(), originalDisplayName);
    } catch (Exception e) {
      log.error("Error while rolling back consultant", e);
      throw new DistributedTransactionException(
          e,
          DistributedTransactionInfo.builder()
              .completedTransactionalOperations(Lists.newArrayList())
              .name("patchConsultant")
              .failedStep(TransactionalStep.ROLLBACK_UPDATE_ROCKET_CHAT_USER_DISPLAY_NAME)
              .build());
    }
  }
}
