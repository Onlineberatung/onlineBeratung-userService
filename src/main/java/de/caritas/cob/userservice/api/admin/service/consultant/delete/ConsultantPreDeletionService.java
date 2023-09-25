package de.caritas.cob.userservice.api.admin.service.consultant.delete;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_HAS_ACTIVE_OR_ARCHIVE_SESSIONS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_ARCHIVE;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyDeletionValidationService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
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
  private final @NonNull IdentityClient identityClient;

  /**
   * Validates if {@link Consultant} can be deleted and marks the account as inactive in keycloak.
   *
   * @param consultant the {@link Consultant} to be deleted
   * @param forceDeleteSessions
   */
  public void performPreDeletionSteps(Consultant consultant, Boolean forceDeleteSessions) {

    if (isNotTrue(forceDeleteSessions) && hasConsultantActiveSessions(consultant)) {
      throw new CustomValidationHttpStatusException(CONSULTANT_HAS_ACTIVE_OR_ARCHIVE_SESSIONS);
    }
    if (nonNull(consultant.getConsultantAgencies())) {
      consultant
          .getConsultantAgencies()
          .forEach(agencyDeletionValidationService::validateAndMarkForDeletion);
    }
    this.identityClient.deactivateUser(consultant.getId());
  }

  private boolean hasConsultantActiveSessions(Consultant consultant) {
    return !this.sessionRepository
        .findByConsultantAndStatusIn(consultant, Lists.newArrayList(IN_PROGRESS, IN_ARCHIVE))
        .isEmpty();
  }
}
