package de.caritas.cob.userservice.api.facade.rollback;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUserAccountService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * Facade for capsuling the steps to roll back an user account.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RollbackFacade {

  private final @NonNull IdentityClient identityClient;
  private final @NonNull UserAgencyService userAgencyService;
  private final @NonNull SessionService sessionService;
  private final @NonNull UserService userService;

  private final @NonNull DeleteUserAccountService deleteUserAccountService;

  public void rollbackConsultantAccount(Consultant consultant) {
    log.info(
        "Initiating rollback of consultant account. Consultant id: {}",
        consultant.getId(),
        consultant.getUsername());
    consultant.setDeleteDate(LocalDateTime.now());
    List<DeletionWorkflowError> deletionWorkflowErrors =
        deleteUserAccountService.performConsultantDeletion(consultant);
    if (nonNull(deletionWorkflowErrors) && !deletionWorkflowErrors.isEmpty()) {
      deletionWorkflowErrors.stream()
          .forEach(e -> log.error("Consultant delete error during rollback: ", e));
    }
  }
  /**
   * Deletes the provided user in Keycloak, MariaDB and its related session or user-chat/agency
   * relations depending on the provided {@link RollbackUserAccountInformation}.
   *
   * @param rollbackUser {@link RollbackUserAccountInformation}
   */
  public void rollBackUserAccount(RollbackUserAccountInformation rollbackUser) {
    rollbackUserAgency(rollbackUser);
    rollbackSession(rollbackUser);
    rollbackKeycloakAndMariaDbAccount(rollbackUser);
  }

  private void rollbackUserAgency(RollbackUserAccountInformation rollbackUser) {
    if (nonNull(rollbackUser.getUserAgency())) {
      userAgencyService.deleteUserAgency(rollbackUser.getUserAgency());
    }
  }

  private void rollbackSession(RollbackUserAccountInformation rollbackUser) {
    if (nonNull(rollbackUser.getSession())) {
      sessionService.deleteSession(rollbackUser.getSession());
    }
  }

  private void rollbackKeycloakAndMariaDbAccount(RollbackUserAccountInformation rollbackUser) {
    if (rollbackUser.isRollBackUserAccount()) {
      if (nonNull(rollbackUser.getUserId())) {
        identityClient.rollBackUser(rollbackUser.getUserId());
      }
      if (nonNull(rollbackUser.getUser())) {
        userService.deleteUser(rollbackUser.getUser());
      }
    }
  }
}
