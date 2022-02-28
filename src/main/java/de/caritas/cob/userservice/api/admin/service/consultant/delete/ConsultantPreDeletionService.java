package de.caritas.cob.userservice.api.admin.service.consultant.delete;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_HAS_ACTIVE_SESSIONS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyDeletionValidationService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Executes pre deletion steps like validation for consultant to be deleted and deactivates the
 * account in keycloak.
 */
@Service
@RequiredArgsConstructor
public class ConsultantPreDeletionService {

  private final @NonNull ConsultantAgencyDeletionValidationService agencyDeletionValidationService;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  /**
   * Validates if {@link Consultant} can be deleted and marks the account as inactive in keycloak.
   *
   * @param consultant the {@link Consultant} to be deleted
   */
  public void performPreDeletionSteps(Consultant consultant) {
    if (hasConsultantActiveSessions(consultant)) {
      throw new CustomValidationHttpStatusException(CONSULTANT_HAS_ACTIVE_SESSIONS);
    }
    if (nonNull(consultant.getConsultantAgencies())) {
      consultant.getConsultantAgencies()
          .forEach(agencyDeletionValidationService::validateForDeletion);
    }
    this.keycloakAdminClientService.deactivateUser(consultant.getId());
  }

  private boolean hasConsultantActiveSessions(Consultant consultant) {
    return !this.sessionRepository.findByConsultantAndStatus(consultant, IN_PROGRESS).isEmpty();
  }

}
