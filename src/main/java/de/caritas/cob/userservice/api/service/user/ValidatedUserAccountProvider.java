package de.caritas.cob.userservice.api.service.user;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.adapters.web.dto.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PasswordDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.rocketchat.dto.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.service.rocketchat.dto.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.service.user.validation.UserAccountValidator;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to provide methods to access and modify the currently validated user account.
 */
@Service
@RequiredArgsConstructor
public class ValidatedUserAccountProvider {

  private final @NonNull UserService userService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserAccountValidator userAccountValidator;
  private final @NonNull UserHelper userHelper;

  /**
   * Tries to retrieve the user of the current {@link AuthenticatedUser} and throws an 500 - Server
   * Error if {@link User} is not present.
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
   * Tries to retrieve the consultant by given id and throws an 500 - Server Error if {@link
   * Consultant} is not present.
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
   * Updates the email address of current authenticated user in Keycloak, Rocket.Chat and database.
   *
   * @param optionalEmail the new email address, potentially empty
   */
  public void changeUserAccountEmailAddress(Optional<String> optionalEmail) {
    optionalEmail.ifPresentOrElse(
        identityClient::changeEmailAddress,
        identityClient::deleteEmailAddress
    );

    var userId = authenticatedUser.getUserId();
    var email = optionalEmail.orElseGet(() -> userHelper.getDummyEmail(userId));
    consultantService.getConsultant(userId).ifPresent(consultant ->
        updateConsultantEmail(consultant, email)
    );
    userService.getUser(userId).ifPresent(user ->
        updateUserEmail(user, email)
    );
  }

  private void updateConsultantEmail(Consultant consultant, String email) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(email, consultant.getFullName());
    UserUpdateRequestDTO requestDTO = new UserUpdateRequestDTO(consultant.getRocketChatId(),
        userUpdateDataDTO);
    this.rocketChatService.updateUser(requestDTO);

    consultant.setEmail(email);
    this.consultantService.saveConsultant(consultant);
  }

  private void updateUserEmail(User user, String email) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(email, user.getUsername());
    UserUpdateRequestDTO requestDTO = new UserUpdateRequestDTO(user.getRcUserId(),
        userUpdateDataDTO);
    this.rocketChatService.updateUser(requestDTO);

    user.setEmail(email);
    this.userService.saveUser(user);
  }

  /**
   * Updates the password of the currently authenticated user and checks if the given old password
   * is correct.
   *
   * @param passwordDTO {@link PasswordDTO}
   */
  public void changePassword(PasswordDTO passwordDTO) {
    userAccountValidator
        .checkPasswordValidity(authenticatedUser.getUsername(), passwordDTO.getOldPassword());

    if (!identityClient.changePassword(authenticatedUser.getUserId(),
        passwordDTO.getNewPassword())) {
      throw new InternalServerErrorException(
          String.format("Could not update password of user %s", authenticatedUser.getUserId()));
    }
  }

  /**
   * Deactivates the Keycloak account of the currently authenticated user and flags this account for
   * deletion if the provided password is valid.
   *
   * @param deleteUserAccountDTO {@link DeleteUserAccountDTO}
   */
  public void deactivateAndFlagUserAccountForDeletion(DeleteUserAccountDTO deleteUserAccountDTO) {
    User user = retrieveValidatedUser();
    this.userAccountValidator
        .checkPasswordValidity(user.getUsername(), deleteUserAccountDTO.getPassword());
    this.identityClient.deactivateUser(user.getUserId());
    user.setDeleteDate(nowInUtc());
    userService.saveUser(user);
  }

  /**
   * Updates or sets the mobile client token of the current authenticated user in database.
   *
   * @param mobileToken the new mobile device identifier token
   */
  public void updateUserMobileToken(String mobileToken) {
    this.userService.getUser(this.authenticatedUser.getUserId())
        .ifPresent(user -> updateMobileToken(user, mobileToken));
  }

  private void updateMobileToken(User user, String mobileToken) {
    user.setMobileToken(mobileToken);
    this.userService.saveUser(user);
  }

  /**
   * Adds a mobile client token of the current authenticated user in database.
   *
   * @param mobileToken the mobile device identifier token to be added
   */
  public void addMobileAppToken(String mobileToken) {
    this.userService.addMobileAppToken(this.authenticatedUser.getUserId(), mobileToken);
    this.consultantService.addMobileAppToken(this.authenticatedUser.getUserId(), mobileToken);
  }

}
