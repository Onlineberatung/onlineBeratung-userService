package de.caritas.cob.userservice.api.service.user;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.json.JsonSerializationUtils;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.DeleteAccountStatisticsEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/** Service class to provide methods to access and modify the currently validated user account. */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountService {

  private final @NonNull UserService userService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull AppointmentService appointmentService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserHelper userHelper;

  private final @NonNull IdentityClientConfig identityClientConfig;

  private final @NonNull StatisticsService statisticsService;

  public Optional<User> findUserByEmail(String email) {
    return this.userService.findUserByEmail(email);
  }

  public Optional<Consultant> findConsultantByEmail(String email) {
    return this.consultantService.findConsultantByEmail(email);
  }

  /**
   * Tries to retrieve the user of the current {@link AuthenticatedUser} and throws an 500 - Server
   * Error if {@link User} is not present.
   *
   * @return the validated {@link User}
   */
  public User retrieveValidatedUser() {
    return this.userService
        .getUser(this.authenticatedUser.getUserId())
        .orElseThrow(
            () ->
                new InternalServerErrorException(
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
    return this.consultantService
        .getConsultant(consultantId)
        .orElseThrow(
            () ->
                new InternalServerErrorException(
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
    throw new ForbiddenException(
        String.format(
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
        identityClient::changeEmailAddress, identityClient::deleteEmailAddress);

    var userId = authenticatedUser.getUserId();
    var email = optionalEmail.orElseGet(() -> userHelper.getDummyEmail(userId));
    consultantService
        .getConsultant(userId)
        .ifPresent(consultant -> updateConsultantEmail(consultant, email));
    userService.getUser(userId).ifPresent(user -> updateUserEmail(user, email));
  }

  private void updateConsultantEmail(Consultant consultant, String email) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(email, true);
    UserUpdateRequestDTO requestDTO =
        new UserUpdateRequestDTO(consultant.getRocketChatId(), userUpdateDataDTO);
    this.rocketChatService.updateUser(requestDTO);

    consultant.setEmail(email);
    this.consultantService.saveConsultant(consultant);
  }

  void updateUserEmail(User user, String email) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(email, true);
    UserUpdateRequestDTO requestDTO =
        new UserUpdateRequestDTO(user.getRcUserId(), userUpdateDataDTO);
    if (user.getRcUserId() != null) {
      this.rocketChatService.updateUser(requestDTO);
    } else {
      log.warn(
          "Skip update user email in RocketChat because user does not have rcUserId (maybe a newly registered user?)");
    }
    this.appointmentService.updateAskerEmail(user.getUserId(), email);
    setInitialEmailNotificationsSettingsForNewEmailAddress(user, email);
    user.setEmail(email);
    this.userService.saveUser(user);
  }

  private void setInitialEmailNotificationsSettingsForNewEmailAddress(User user, String email) {
    if (isBlankOrInitialDummyEmail(user) && email != null) {
      user.setNotificationsEnabled(true);
      user.setNotificationsSettings(
          JsonSerializationUtils.serializeToJsonString(activeNotificationsForAdviceSeeker()));
    }
  }

  private boolean isBlankOrInitialDummyEmail(User user) {
    return StringUtils.isBlank(user.getEmail()) || hasInitiallySetDummyEmail(user);
  }

  private boolean hasInitiallySetDummyEmail(User user) {
    return identityClientConfig.getEmailDummySuffix() != null
        && user.getEmail().endsWith(identityClientConfig.getEmailDummySuffix());
  }

  private NotificationsSettingsDTO activeNotificationsForAdviceSeeker() {
    NotificationsSettingsDTO notificationsSettingsDTO = new NotificationsSettingsDTO();
    notificationsSettingsDTO.setReassignmentNotificationEnabled(true);
    notificationsSettingsDTO.setAppointmentNotificationEnabled(true);
    notificationsSettingsDTO.setNewChatMessageNotificationEnabled(true);
    return notificationsSettingsDTO;
  }

  /**
   * Deactivates the Keycloak account of the currently authenticated user and flags this account for
   * deletion.
   */
  public void deactivateAndFlagUserAccountForDeletion() {
    User user = retrieveValidatedUser();
    this.identityClient.deactivateUser(user.getUserId());
    user.setDeleteDate(nowInUtc());
    userService.saveUser(user);
    fireAccountDeletionStatisticsEvent(user);
  }

  private void fireAccountDeletionStatisticsEvent(User user) {
    try {
      DeleteAccountStatisticsEvent deleteAccountStatisticsEvent =
          new DeleteAccountStatisticsEvent(user, LocalDateTime.now());
      log.debug("Firing account deletion statistics event: {}", deleteAccountStatisticsEvent);
      statisticsService.fireEvent(deleteAccountStatisticsEvent);
    } catch (Exception e) {
      log.error("Could not create account deletion statistics event", e);
    }
  }

  /**
   * Updates or sets the mobile client token of the current authenticated user in database.
   *
   * @param mobileToken the new mobile device identifier token
   */
  public void updateUserMobileToken(String mobileToken) {
    this.userService
        .getUser(this.authenticatedUser.getUserId())
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
