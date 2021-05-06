package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;

/**
 * Verifier class to check input {@link Session} and {@link Consultant} data to be valid for session
 * assignments.
 */
public class SessionToConsultantVerifier {

  private final SessionToConsultantConditionProvider conditionProvider;

  public SessionToConsultantVerifier(Session session, Consultant consultant) {
    requireNonNull(session);
    requireNonNull(consultant);
    this.conditionProvider = new SessionToConsultantConditionProvider(session, consultant);
  }

  /**
   * verifies if {@link Session} is not already in progress.
   */
  public void verifySessionIsNotInProgress() {
    if (this.conditionProvider.isSessionInProgress()) {
      var message = String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  /**
   * verifies necessary input data of {@link Session} and {@link Consultant}.
   */
  public void verifyPreconditionsForAssignment() {
    verifyIfSessionIsAlreadyAssignedToConsultant();
    verifyUserAndConsultantHaveRocketChatId();
    if (REGISTERED.equals(conditionProvider.getSession().getRegistrationType())) {
      verifyIfConsultantIsAssignedToAgency();
    }
    if (ANONYMOUS.equals(conditionProvider.getSession().getRegistrationType())) {
      verifyIfConsultantHasConsultingTypeOfSession();
    }
  }

  private void verifyIfSessionIsAlreadyAssignedToConsultant() {

    if (this.conditionProvider.hasSessionNoConsultant()) {
      return;
    }

    if (this.conditionProvider.isNewSession()) {
      var message = String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }

    if (this.conditionProvider.isSessionAlreadyAssignedToConsultant()) {
      var message = String.format(
          "Session %s is already assigned to this consultant. Assignment to consultant %s is not possible.",
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  private void verifyUserAndConsultantHaveRocketChatId() {

    if (this.conditionProvider.hasSessionUserNoRcId()) {
      var message = String.format(
          "The provided user with id %s does not have a Rocket.Chat id assigned in the database.",
          conditionProvider.getSession().getUser().getUserId());

      throw new InternalServerErrorException(message, LogService::logAssignSessionFacadeError);
    }

    if (this.conditionProvider.hasConsultantNoRcId()) {
      var message = String.format(
          "The provided consultant with id %s does not have a Rocket.Chat id assigned in the database.",
          conditionProvider.getConsultant().getId());

      throw new InternalServerErrorException(message, LogService::logAssignSessionFacadeError);
    }
  }

  private void verifyIfConsultantIsAssignedToAgency() {
    if (this.conditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies()) {
      var message = String.format("Agency %s of session %s is not assigned to consultant %s.",
          conditionProvider.getSession().getAgencyId().toString(),
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ForbiddenException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  private void verifyIfConsultantHasConsultingTypeOfSession() {
    if (this.conditionProvider.isSessionsConsultingTypeNotAvailableForConsultant()) {
      var message = String.format("Cosnulting type %s of session %s is not available for "
              + "consultant %s.",
          conditionProvider.getSession().getConsultingType(),
          conditionProvider.getSession().getId().toString(),
          conditionProvider.getConsultant().getId());

      throw new ForbiddenException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

}
