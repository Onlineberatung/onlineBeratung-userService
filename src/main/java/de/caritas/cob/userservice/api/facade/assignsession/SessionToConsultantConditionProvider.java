package de.caritas.cob.userservice.api.facade.assignsession;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides several conditions used to validate {@link Session} and {@link Consultant} to be
 * updated.
 */
@Service
@RequiredArgsConstructor
public class SessionToConsultantConditionProvider {

  private final @NonNull AgencyService agencyService;

  /**
   * checks if the {@link Session} is in progress.
   *
   * @return true if the {@link Session} status is IN_PROGRESS
   */
  public boolean isSessionInProgress(Session session) {
    return hasSessionStatus(SessionStatus.IN_PROGRESS, session);
  }

  private boolean hasSessionStatus(SessionStatus sessionStatus, Session session) {
    return sessionStatus.equals(session.getStatus());
  }

  /**
   * checks if the {@link Session} is new.
   *
   * @return true if the {@link Session} is NEW
   */
  public boolean isNewSession(Session session) {
    return hasSessionStatus(SessionStatus.NEW, session);
  }

  /**
   * checks if the {@link Session} has no {@link Consultant} assigned.
   *
   * @return true if the {@link Session} has no {@link Consultant}
   */
  public boolean hasSessionNoConsultant(Session session) {
    return isNull(session.getConsultant()) || isBlank(session.getConsultant().getId());
  }

  /**
   * checks if the {@link Session} is already assigned to the {@link Consultant}.
   *
   * @return true if the {@link Session} is already assigned to {@link Consultant}
   */
  public boolean isSessionAlreadyAssignedToConsultant(Consultant consultant, Session session) {
    return isSessionInProgress(session)
        && session.getConsultant().getId().equals(consultant.getId());
  }

  /**
   * checks if the {@link Session} has a User without rocked.chat id.
   *
   * @return true if the {@link Session} has a User without rocked.chat id
   */
  public boolean hasSessionUserNoRcId(Session session) {
    return nonNull(session.getUser()) && isBlank(session.getUser().getRcUserId());
  }

  /**
   * checks if the {@link Consultant} has no rocket.chat id.
   *
   * @return true if the {@link Consultant} has no rocket.chat id
   */
  public boolean hasConsultantNoRcId(Consultant consultant) {
    return isBlank(consultant.getRocketChatId());
  }

  /**
   * checks if the agencyId of the {@link Session} is not available in consultants agencies.
   *
   * @return true if agencyId of {@link Session} is not contained in consultants agencies
   */
  public boolean isSessionsAgencyNotAvailableInConsultantAgencies(
      Consultant consultant, Session session) {
    if (isEmpty(consultant.getConsultantAgencies())) {
      return true;
    }
    return consultant.getConsultantAgencies().stream()
        .map(ConsultantAgency::getAgencyId)
        .noneMatch(agencyId -> agencyId.equals(session.getAgencyId()));
  }

  /**
   * Checks if the consulting type of the {@link Session} is available for the {@link Consultant}.
   *
   * @return true if consuling type is available for {@link Consultant}
   */
  public boolean isSessionsConsultingTypeNotAvailableForConsultant(
      Consultant consultant, Session session) {
    if (isEmpty(consultant.getConsultantAgencies())) {
      return true;
    }
    return consultant.getConsultantAgencies().stream()
        .map(agency -> agencyService.getAgency(agency.getAgencyId()))
        .map(AgencyDTO::getConsultingType)
        .noneMatch(consultingType -> session.getConsultingTypeId() == consultingType);
  }
}
