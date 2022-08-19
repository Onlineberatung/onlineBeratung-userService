package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.INITIAL;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.NEW;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantAgencyDeletionValidationService {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyService agencyService;
  private final @NonNull SessionRepository sessionRepository;

  /**
   * Validates if the given {@link ConsultantAgency} is valid to be deleted.
   *
   * @param consultantAgency the {@link ConsultantAgency} to be deleted
   */
  public void validateAndMarkForDeletion(ConsultantAgency consultantAgency) {
    if (isTheLastConsultantInAgency(consultantAgency)) {
      if (isAgencyStillActive(consultantAgency)) {
        throw new CustomValidationHttpStatusException(
            CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE);
      }
      if (hasOpenEnquiries(consultantAgency)) {
        throw new CustomValidationHttpStatusException(
            CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES);
      }
    }
    consultantAgency.setDeleteDate(nowInUtc());
    consultantAgencyRepository.save(consultantAgency);
  }

  private boolean isTheLastConsultantInAgency(ConsultantAgency consultantAgency) {
    return this.consultantAgencyRepository
        .findByAgencyIdAndDeleteDateIsNull(consultantAgency.getAgencyId())
        .stream()
        .filter(relation -> isNull(relation.getDeleteDate()))
        .allMatch(sameConsultantAgencyRelation(consultantAgency));
  }

  private Predicate<ConsultantAgency> sameConsultantAgencyRelation(
      ConsultantAgency consultantAgency) {
    return relation -> relation.equals(consultantAgency);
  }

  private boolean isAgencyStillActive(ConsultantAgency consultantAgency) {
    AgencyDTO agency = this.agencyService.getAgencyWithoutCaching(consultantAgency.getAgencyId());
    return isFalse(agency.getOffline());
  }

  private boolean hasOpenEnquiries(ConsultantAgency consultantAgency) {
    Long agencyId = consultantAgency.getAgencyId();
    return hasSessionWithStatus(agencyId, NEW) || hasSessionWithStatus(agencyId, INITIAL);
  }

  private boolean hasSessionWithStatus(Long agencyId, SessionStatus status) {
    return !this.sessionRepository
        .findByAgencyIdAndStatusAndConsultantIsNull(agencyId, status)
        .isEmpty();
  }
}
