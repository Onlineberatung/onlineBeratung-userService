package de.caritas.cob.userservice.api.facade.assignsession;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import lombok.Getter;

/**
 * Provides several conditions used to validate {@link Session} and {@link Consultant} to be
 * updated.
 */
@Getter
public class SessionToConsultantConditionProvider {

  private final Session session;
  private final Consultant consultant;

  public SessionToConsultantConditionProvider(Session session, Consultant consultant) {
    requireNonNull(session);
    requireNonNull(consultant);
    this.session = session;
    this.consultant = consultant;
  }

  /**
   * checks if the {@link Session} is in progress.
   *
   * @return true if the {@link Session} status is IN_PROGRESS
   */
  public boolean isSessionInProgress() {
    return hasSessionStatus(SessionStatus.IN_PROGRESS);
  }

  /**
   * checks if the {@link Session} is new.
   *
   * @return true if the {@link Session} is NEW
   */
  public boolean isNewSession() {
    return hasSessionStatus(SessionStatus.NEW);
  }

  private boolean hasSessionStatus(SessionStatus sessionStatus) {
    return sessionStatus.equals(this.session.getStatus());
  }

  /**
   * checks if the {@link Session} has no {@link Consultant} assigned.
   *
   * @return true if the {@link Session} has no {@link Consultant}
   */
  public boolean hasSessionNoConsultant() {
    return isNull(this.session.getConsultant()) || isBlank(this.session.getConsultant().getId());
  }

  /**
   * checks if the {@link Session} is already assigned to the {@link Consultant}.
   *
   * @return true if the {@link Session} is already assigned to {@link Consultant}
   */
  public boolean isSessionAlreadyAssignedToConsultant() {
    return isSessionInProgress() && this.session.getConsultant().getId()
        .equals(this.consultant.getId());
  }

  /**
   * checks if the {@link Session} has a User without rocked.chat id.
   *
   * @return true if the {@link Session} has a User without rocked.chat id
   */
  public boolean hasSessionUserNoRcId() {
    return nonNull(this.session.getUser()) && isBlank(this.session.getUser().getRcUserId());
  }

  /**
   * checks if the {@link Consultant} has no rocket.chat id.
   *
   * @return true if the {@link Consultant} has no rocket.chat id
   */
  public boolean hasConsultantNoRcId() {
    return isBlank(this.consultant.getRocketChatId());
  }

  /**
   * checks if the agencyId of the {@link Session} is not available in consultants agencies.
   *
   * @return true if agencyId of {@link Session} is not contained in consultants agencies
   */
  public boolean isSessionsAgencyNotAvailableInConsultantAgencies() {
    if (isEmpty(this.consultant.getConsultantAgencies())) {
      return true;
    }
    return this.consultant.getConsultantAgencies().stream()
        .map(ConsultantAgency::getAgencyId)
        .noneMatch(agencyId -> agencyId.equals(this.session.getAgencyId()));
  }

}
