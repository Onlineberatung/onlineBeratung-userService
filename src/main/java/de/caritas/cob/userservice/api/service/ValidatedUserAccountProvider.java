package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to provide access to user accounts and validate them.
 */
@Service
@RequiredArgsConstructor
public class ValidatedUserAccountProvider {

  private final @NonNull UserService userService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Tries to retrieve the user of the current {@link AuthenticatedUser} and throws an 500 -
   * Server Error if {@link User} is not present.
   *
   * @return the validated {@link User}
   */
  public User retrieveValidatedUser() {
    return this.userService.getUser(this.authenticatedUser.getUserId())
        .orElseThrow(() -> new InternalServerErrorException(
            String.format("User with id %s not found", authenticatedUser.getUserId())));
  }

  /**
   * Tries to retrieve the consultant of the current {@link AuthenticatedUser} and throws an 500 -
   * Server Error if {@link Consultant} is not present.
   *
   * @return the validated {@link Consultant}
   */
  public Consultant retrieveValidatedConsultant() {
    return retrieveValidatedConsultantById(this.authenticatedUser.getUserId());
  }

  /**
   * Tries to retrieve the consultant by given id and throws an 500 -
   * Server Error if {@link Consultant} is not present.
   *
   * @param consultantId the id to search for
   * @return the validated {@link Consultant}
   */
  public Consultant retrieveValidatedConsultantById(String consultantId) {
    return this.consultantService.getConsultant(consultantId)
        .orElseThrow(() -> new InternalServerErrorException(
            String.format("Consultant with id %s not found", consultantId)));
  }

  /**
   * Tries to retrieve the team consultant of the current {@link AuthenticatedUser} and throws an
   * 403 - Forbidden Error if {@link Consultant} is not a team consultant.
   *
   * @return the validated {@link Consultant}
   */
  public Consultant retrieveValidatedTeamConsultant() {
    Consultant consultant = retrieveValidatedConsultant();
    if (consultant.isTeamConsultant()) {
      return consultant;
    }
    throw new ForbiddenException(String.format(
        "Consultant with id %s is no team consultant and therefore not allowed to get team "
            + "sessions.",
        authenticatedUser.getUserId()));
  }

  /**
   * Updates an absent user.
   *
   * @param absence the dto to update the absence
   */
  public void updateConsultantAbsent(AbsenceDTO absence) {
    Consultant consultant = retrieveValidatedConsultant();
    consultantService.updateConsultantAbsent(consultant, absence);
  }

}
