package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.AgencyVerifier;
import de.caritas.cob.userservice.api.helper.UserVerifier;
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
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to initialize an user account.
 */
@Service
@RequiredArgsConstructor
public class CreateUserFacade {

  private final @NonNull UserVerifier userVerifier;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull UserService userService;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull AgencyVerifier agencyVerifier;
  private final @NonNull CreateNewConsultingTypeFacade createNewConsultingTypeFacade;

  /**
   * Creates an user in Keycloak and MariaDB. Then creates a session or chat account depending on
   * the provided {@link ConsultingType}.
   *
   * @param userDTO {@link UserDTO}
   */
  public void createUserAccountWithInitializedConsultingType(final UserDTO userDTO) {

    userVerifier.checkIfUsernameIsAvailable(userDTO);
    agencyVerifier.checkIfConsultingTypeMatchesToAgency(userDTO);

    KeycloakCreateUserResponseDTO response = keycloakAdminClientService.createKeycloakUser(userDTO);
    var user = updateKeycloakAccountAndCreateDatabaseUserAccount(response.getUserId(), userDTO,
        UserRole.USER);
    createNewConsultingTypeFacade
        .initializeNewConsultingType(userDTO, user, obtainConsultingTypeSettings(userDTO));
  }

  /**
   * Updates Keycloak role and password and creates an user account in MariaDB.
   *
   * @param userId Keycloak user ID
   * @param userDTO {@link UserDTO}
   * @return {@link User}
   */
  public User updateKeycloakAccountAndCreateDatabaseUserAccount(String userId, UserDTO userDTO,
      UserRole role) {

    User user = null;
    try {
      updateKeycloakRoleAndPassword(userId, userDTO, role);

      user = userService
          .createUser(userId, userDTO.getUsername(), returnDummyEmailIfNoneGiven(userDTO, userId),
              obtainConsultingTypeSettings(userDTO).isLanguageFormal());

    } catch (Exception ex) {
      rollBackAccountInitialization(userId, userDTO);
    }

    return user;
  }

  private ConsultingTypeSettings obtainConsultingTypeSettings(UserDTO userDTO) {
    return consultingTypeManager.getConsultingTypeSettings(
            ConsultingType.values()[Integer.parseInt(userDTO.getConsultingType())]);
  }

  private void updateKeycloakRoleAndPassword(String userId, UserDTO userDTO, UserRole role) {
    checkIfUserIdNotNull(userId, userDTO);
    keycloakAdminClientService.updateRole(userId, role);
    keycloakAdminClientService.updatePassword(userId, userDTO.getPassword());
  }

  private void checkIfUserIdNotNull(String userId, UserDTO userDTO) {
    if (isNull(userId)) {
      throw new InternalServerErrorException(
          String.format("Could not create Keycloak account for: %s", userDTO.toString()));
    }
  }

  private String returnDummyEmailIfNoneGiven(UserDTO userDTO, String userId) {
    if (isBlank(userDTO.getEmail())) {
      return keycloakAdminClientService.updateDummyEmail(userId, userDTO);
    }

    return userDTO.getEmail();
  }

  private void rollBackAccountInitialization(String userId, UserDTO userDTO) {
    rollbackFacade
        .rollBackUserAccount(RollbackUserAccountInformation.builder().userId(userId)
            .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted())).build());
    throw new InternalServerErrorException(
        String.format("Could not update account data on registration for: %s", userDTO));
  }
}
