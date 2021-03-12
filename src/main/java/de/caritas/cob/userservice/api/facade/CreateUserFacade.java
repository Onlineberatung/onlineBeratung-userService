package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.AgencyHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to initialize an user account (create chat/agency relation or a
 * new session).
 */
@Service
@RequiredArgsConstructor
public class CreateUserFacade {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull UserService userService;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull AgencyHelper agencyHelper;
  private final @NonNull CreateNewConsultingTypeFacade createNewConsultingTypeFacade;

  /**
   * Creates a user in Keycloak and MariaDB. Then creates a session or chat account depending on the
   * provided {@link ConsultingType}.
   *
   * @param userDTO {@link UserDTO}
   */
  public void createUserAndInitializeAccount(final UserDTO userDTO) {

    if (!keycloakAdminClientService.isUsernameAvailable(userDTO.getUsername())) {
      throw new CustomValidationHttpStatusException(
          HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }

    ConsultingType consultingType =
        ConsultingType.values()[Integer.parseInt(userDTO.getConsultingType())];
    checkIfConsultingTypeMatchesToAgency(userDTO, consultingType);
    KeycloakCreateUserResponseDTO response = createKeycloakUser(userDTO);
    updateKeycloakAccountAndCreateDatabaseUserAccount(response.getUserId(), userDTO,
        consultingType);
  }

  private KeycloakCreateUserResponseDTO createKeycloakUser(UserDTO userDTO) {
    try {
      // Create the user in Keycloak
      return keycloakAdminClientService.createKeycloakUser(userDTO);
    } catch (Exception ex) {
      throw new InternalServerErrorException(
          String.format("Could not create Keycloak account for: %s", userDTO.toString()));
    }
  }

  private void checkIfConsultingTypeMatchesToAgency(UserDTO user, ConsultingType consultingType) {
    if (!agencyHelper.doesConsultingTypeMatchToAgency(user.getAgencyId(), consultingType)) {
      throw new BadRequestException(String.format("Agency with id %s does not match to consulting"
          + " type %s", user.getAgencyId(), consultingType.getValue()));
    }
  }

  private void updateKeycloakAccountAndCreateDatabaseUserAccount(String userId, UserDTO userDTO,
      ConsultingType consultingType) {

    checkIfUserIdNotNull(userId, userDTO);

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(consultingType);
    User user;

    try {
      // We need to set the user roles and password and (dummy) e-mail address after the user was
      // created in Keycloak
      keycloakAdminClientService.updateUserRole(userId);
      keycloakAdminClientService.updatePassword(userId, userDTO.getPassword());

      user = userService
          .createUser(userId, userDTO.getUsername(), returnDummyEmailIfNoneGiven(userDTO, userId),
              consultingTypeSettings.isLanguageFormal());

    } catch (Exception ex) {
      rollbackFacade
          .rollBackUserAccount(RollbackUserAccountInformation.builder().userId(userId)
              .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted())).build());
      throw new InternalServerErrorException(
          String
              .format("Could not update account data on registration for: %s", userDTO.toString()));
    }

    createNewConsultingTypeFacade
        .initializeNewConsultingType(userDTO, user, consultingTypeSettings);
  }

  private String returnDummyEmailIfNoneGiven(UserDTO userDTO, String userId) {
    if (isBlank(userDTO.getEmail())) {
      return keycloakAdminClientService.updateDummyEmail(userId, userDTO);
    }

    return userDTO.getEmail();
  }

  private void checkIfUserIdNotNull(String userId, UserDTO userDTO) {
    if (isNull(userId)) {
      throw new InternalServerErrorException(
          String.format("Could not create Keycloak account for: %s", userDTO.toString()));
    }
  }

}
