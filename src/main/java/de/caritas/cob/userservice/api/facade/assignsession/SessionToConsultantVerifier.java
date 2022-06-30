package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Verifier class to check input {@link Session} and {@link Consultant} data to be valid for session
 * assignments.
 */
@Service
@RequiredArgsConstructor
public class SessionToConsultantVerifier {

  private final @NonNull SessionToConsultantConditionProvider conditionProvider;

  /**
   * verifies if {@link Session} is not already in progress.
   */
  public void verifySessionIsNotInProgress(ConsultantSessionDTO consultantSessionDTO) {
    if (this.conditionProvider.isSessionInProgress(consultantSessionDTO.getSession())) {
      var message = String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          consultantSessionDTO.getSession().getId().toString(),
          consultantSessionDTO.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  public void verifySessionIsNew(ConsultantSessionDTO consultantSessionDTO) {
    if (!conditionProvider.isNewSession(consultantSessionDTO.getSession())) {
      var message = String.format(
          "Session %s is not new and cannot be accepted by consultant %s.",
          consultantSessionDTO.getSession().getId().toString(),
          consultantSessionDTO.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  /**
   * verifies necessary input data of {@link Session} and {@link Consultant}.
   */
  public void verifyPreconditionsForAssignment(ConsultantSessionDTO consultantSessionDTO) {
    verifyIfSessionIsAlreadyAssignedToConsultant(consultantSessionDTO);
    verifyUserAndConsultantHaveRocketChatId(consultantSessionDTO);
    if (REGISTERED.equals(consultantSessionDTO.getSession().getRegistrationType())) {
      verifyIfConsultantIsAssignedToAgency(consultantSessionDTO);
    }
    if (ANONYMOUS.equals(consultantSessionDTO.getSession().getRegistrationType())) {
      verifyIfConsultantHasConsultingTypeOfSession(consultantSessionDTO);
    }
  }

  private void verifyIfSessionIsAlreadyAssignedToConsultant(
      ConsultantSessionDTO consultantSessionDTO) {

    if (this.conditionProvider.hasSessionNoConsultant(consultantSessionDTO.getSession())) {
      return;
    }

    if (this.conditionProvider.isNewSession(consultantSessionDTO.getSession())) {
      var message = String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          consultantSessionDTO.getSession().getId().toString(),
          consultantSessionDTO.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }

    if (this.conditionProvider
        .isSessionAlreadyAssignedToConsultant(consultantSessionDTO.getConsultant(),
            consultantSessionDTO.getSession())) {
      var message = String.format(
          "Session %s is already assigned to this consultant. Assignment to consultant %s is not possible.",
          consultantSessionDTO.getSession().getId().toString(),
          consultantSessionDTO.getConsultant().getId());

      throw new ConflictException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  private void verifyUserAndConsultantHaveRocketChatId(ConsultantSessionDTO consultantSessionDTO) {

    if (this.conditionProvider.hasSessionUserNoRcId(consultantSessionDTO.getSession())) {
      var message = String.format(
          "The provided user with id %s does not have a Rocket.Chat id assigned in the database.",
          consultantSessionDTO.getSession().getUser().getUserId());

      throw new InternalServerErrorException(message, LogService::logAssignSessionFacadeError);
    }

    if (this.conditionProvider.hasConsultantNoRcId(consultantSessionDTO.getConsultant())) {
      var message = String.format(
          "The provided consultant with id %s does not have a Rocket.Chat id assigned in the database.",
          consultantSessionDTO.getConsultant().getId());

      throw new InternalServerErrorException(message, LogService::logAssignSessionFacadeError);
    }
  }

  private void verifyIfConsultantIsAssignedToAgency(ConsultantSessionDTO consultantSessionDTO) {
    if (this.conditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies(
        consultantSessionDTO.getConsultant(), consultantSessionDTO.getSession())) {
      var message = String.format("Agency %s of session %s is not assigned to consultant %s.",
          consultantSessionDTO.getSession().getAgencyId().toString(),
          consultantSessionDTO.getSession().getId().toString(),
          consultantSessionDTO.getConsultant().getId());

      throw new ForbiddenException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

  private void verifyIfConsultantHasConsultingTypeOfSession(
      ConsultantSessionDTO consultantSessionDTO) {
    if (this.conditionProvider.isSessionsConsultingTypeNotAvailableForConsultant(
        consultantSessionDTO.getConsultant(), consultantSessionDTO.getSession())) {
      var message = String.format(
          "Consulting type %s of session %s is not available for consultant %s.",
          consultantSessionDTO.getSession().getConsultingTypeId(),
          consultantSessionDTO.getSession().getId().toString(),
          consultantSessionDTO.getConsultant().getId());

      throw new ForbiddenException(message, LogService::logAssignSessionFacadeWarning);
    }
  }

}
