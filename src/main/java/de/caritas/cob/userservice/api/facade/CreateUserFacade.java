package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.AgencyHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.CreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.UserService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
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

  private static final int USERNAME_NOT_AVAILABLE = 0;
  private static final int EMAIL_AVAILABLE = 1;

  private final @NonNull KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final @NonNull UserService userService;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UserHelper userHelper;
  private final @NonNull AgencyHelper agencyHelper;
  private final @NonNull CreateNewConsultingTypeFacade createNewConsultingTypeFacade;

  /**
   * Creates a user in Keycloak and MariaDB. Then creates a session or chat account depending on the
   * provided {@link ConsultingType}.
   *
   * @param userDTO {@link UserDTO}
   * @return {@link KeycloakCreateUserResponseDTO}
   */
  public KeycloakCreateUserResponseDTO createUserAndInitializeAccount(final UserDTO userDTO) {

    if (!userHelper.isUsernameAvailable(userDTO.getUsername())) {
      return new KeycloakCreateUserResponseDTO(HttpStatus.CONFLICT,
          new CreateUserResponseDTO().usernameAvailable(USERNAME_NOT_AVAILABLE)
              .emailAvailable(EMAIL_AVAILABLE), null);
    }

    ConsultingType consultingType =
        ConsultingType.values()[Integer.parseInt(userDTO.getConsultingType())];

    checkIfConsultingTypeMatchesToAgency(userDTO, consultingType);

    KeycloakCreateUserResponseDTO response = createKeycloakUser(userDTO);
    if (response.getStatus().equals(HttpStatus.CONFLICT)) {
      return response;
    }

    updateKeycloakAccountAndCreateDatabaseUserAccount(response.getUserId(), userDTO,
        consultingType);

    return new KeycloakCreateUserResponseDTO(HttpStatus.CREATED);
  }

  private KeycloakCreateUserResponseDTO createKeycloakUser(UserDTO userDTO) {
    try {
      // Create the user in Keycloak
      return keycloakAdminClientHelper.createKeycloakUser(userDTO);
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
      keycloakAdminClientHelper.updateUserRole(userId);
      keycloakAdminClientHelper.updatePassword(userId, userDTO.getPassword());

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

  private String returnDummyEmailIfNoneGiven(UserDTO userDTO, String userId) throws Exception {
    if (isBlank(userDTO.getEmail())) {
      return keycloakAdminClientHelper.updateDummyEmail(userId, userDTO);
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
