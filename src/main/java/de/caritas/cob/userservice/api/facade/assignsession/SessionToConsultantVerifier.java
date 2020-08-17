package de.caritas.cob.userservice.api.facade.assignsession;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;

/**
 * Verifier class to check input {@link Session} and {@link Consultant} data to be valid for
 * session assignments.
 */
public class SessionToConsultantVerifier {

  private final SessionToConsultantConditionProvider conditionProvider;
  private final LogService logService;

  public SessionToConsultantVerifier(Session session, Consultant consultant,
      LogService logService) {
    requireNonNull(session);
    requireNonNull(consultant);
    requireNonNull(logService);
    this.conditionProvider = new SessionToConsultantConditionProvider(session, consultant);
    this.logService = logService;
  }

  /**
   * verifies if {@link Session} is not already in progress.
   * @throws {@link ConflictException} if session is in progress
   */
  public void verifySessionIsNotInProgress() {
    if (this.conditionProvider.isSessionInProgress()) {
      String message = String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ConflictException(message, logService::logAssignSessionFacadeWarning);
    }
  }

  /**
   * verifies necessary input data of {@link Session} and {@link Consultant}.
   * @throws {@link ConflictException} if session is new or already assigned to given consultant
   * @throws {@link InternalServerErrorException} if session or consultant has no rc_id
   * @throws {@link ForbiddenException} if sessions agency is not available in consultants agencies
   */
  public void verifyPreconditionsForAssignment() {
    verifyIfSessionIsAlreadyAssignedToConsultant();
    verifyUserAndConsultantHaveRocketChatId();
    verifyIfConsultantIsAssignedToAgency();
  }

  private void verifyIfSessionIsAlreadyAssignedToConsultant() {

    if (this.conditionProvider.hasSessionNoConsultant()) {
      return;
    }

    if (this.conditionProvider.isNewSession()) {
      String message = String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ConflictException(message, logService::logAssignSessionFacadeWarning);
    }

    if (this.conditionProvider.isSessionAlreadyAssignedToConsultant()) {
      String message = String.format(
          "Session %s is already assigned to this consultant. Assignment to consultant %s is not possible.",
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ConflictException(message, logService::logAssignSessionFacadeWarning);
    }
  }

  private void verifyUserAndConsultantHaveRocketChatId() {

    if (this.conditionProvider.hasSessionUserNoRcId()) {
      String message = String.format(
          "The provided user with id %s does not have a Rocket.Chat id assigned in the database.",
          conditionProvider.getSession().getUser().getUserId());

      throw new InternalServerErrorException(message, logService::logAssignSessionFacadeError);
    }

    if (this.conditionProvider.hasConsultantNoRcId()) {
      String message = String.format(
          "The provided consultant with id %s does not have a Rocket.Chat id assigned in the database.",
          conditionProvider.getConsultant().getId());

      throw new InternalServerErrorException(message, logService::logAssignSessionFacadeError);
    }
  }

  private void verifyIfConsultantIsAssignedToAgency() {
    if (this.conditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies()) {
      String message = String.format("Agency %s of session %s is not assigned to consultant %s.",
          conditionProvider.getSession().getAgencyId().toString(),
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ForbiddenException(message, logService::logAssignSessionFacadeWarning);
    }
  }

}
