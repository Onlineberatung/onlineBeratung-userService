package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE;
import static de.caritas.cob.userservice.api.repository.session.SessionStatus.INITIAL;
import static de.caritas.cob.userservice.api.repository.session.SessionStatus.NEW;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantAgencyDeletionValidationService {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull SessionRepository sessionRepository;

  /**
   * Validates if the given {@link ConsultantAgency} is valid to be deleted.
   *
   * @param consultantAgency the {@link ConsultantAgency} to be deleted
   */
  public void validateForDeletion(ConsultantAgency consultantAgency) {
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
  }

  private boolean isTheLastConsultantInAgency(ConsultantAgency consultantAgency) {
    return this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(consultantAgency.getAgencyId())
        .stream()
        .filter(relation -> isNull(relation.getDeleteDate()))
        .allMatch(sameConsultantAgencyRelation(consultantAgency));
  }

  private Predicate<ConsultantAgency> sameConsultantAgencyRelation(
      ConsultantAgency consultantAgency) {
    return relation -> relation.equals(consultantAgency);
  }

  private boolean isAgencyStillActive(ConsultantAgency consultantAgency) {
    try {
      AgencyDTO agency = this.agencyServiceHelper.getAgency(consultantAgency.getAgencyId());
      return isFalse(agency.getOffline());
    } catch (AgencyServiceHelperException e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  private boolean hasOpenEnquiries(ConsultantAgency consultantAgency) {
    Long agencyId = consultantAgency.getAgencyId();
    return hasSessionWithStatus(agencyId, NEW) || hasSessionWithStatus(agencyId, INITIAL);
  }

  private boolean hasSessionWithStatus(Long agencyId, SessionStatus status) {
    return !this.sessionRepository.findByAgencyIdAndStatusAndConsultantIsNull(agencyId, status)
        .isEmpty();
  }

}
