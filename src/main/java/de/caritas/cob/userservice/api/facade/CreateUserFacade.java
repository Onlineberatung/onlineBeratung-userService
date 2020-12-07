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
   * @param user {@link UserDTO}
   * @return {@link KeycloakCreateUserResponseDTO}
   * 
   */
  public KeycloakCreateUserResponseDTO createUserAndInitializeAccount(final UserDTO user) {

    if (!userHelper.isUsernameAvailable(user.getUsername())) {
      return new KeycloakCreateUserResponseDTO(HttpStatus.CONFLICT,
          new CreateUserResponseDTO().usernameAvailable(USERNAME_NOT_AVAILABLE)
              .emailAvailable(EMAIL_AVAILABLE), null);
    }

    ConsultingType consultingType =
        ConsultingType.values()[Integer.parseInt(user.getConsultingType())];

    checkIfConsultingTypeMatchesToAgency(user, consultingType);

    KeycloakCreateUserResponseDTO response = createKeycloakUser(user);
    if (response.getStatus().equals(HttpStatus.CONFLICT)) {
      return response;
    }

    updateKeycloakAccountAndCreateDatabaseUserAccount(response.getUserId(), user, consultingType);

    return new KeycloakCreateUserResponseDTO(HttpStatus.CREATED);
  }

  private KeycloakCreateUserResponseDTO createKeycloakUser(UserDTO user) {
    KeycloakCreateUserResponseDTO response;

    try {
      // Create the user in Keycloak
      response = keycloakAdminClientHelper.createKeycloakUser(user);
    } catch (Exception ex) {
      throw new InternalServerErrorException(
          String.format("Could not create Keycloak account for: %s", user.toString()));
    }

    return response;
  }

  private void checkIfConsultingTypeMatchesToAgency(UserDTO user, ConsultingType consultingType) {
    if (!agencyHelper.doesConsultingTypeMatchToAgency(user.getAgencyId(), consultingType)) {
      throw new BadRequestException(String.format("Agency with id %s does not match to consulting"
          + " type %s", user.getAgencyId(), consultingType.getValue()));
    }
  }

  private void updateKeycloakAccountAndCreateDatabaseUserAccount(String userId, UserDTO user,
      ConsultingType consultingType) {

    checkIfUserIdNotNull(userId, user);

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(consultingType);
    User dbUser;

    try {
      // We need to set the user roles and password and (dummy) e-mail address after the user was
      // created in Keycloak
      keycloakAdminClientHelper.updateUserRole(userId);
      keycloakAdminClientHelper.updatePassword(userId, user.getPassword());

      dbUser = userService
          .createUser(userId, user.getUsername(), returnDummyEmailIfNoneGiven(user, userId),
              consultingTypeSettings.isLanguageFormal());

    } catch (Exception ex) {
      rollbackFacade
          .rollBackUserAccount(RollbackUserAccountInformation.builder().userId(userId).build());
      throw new InternalServerErrorException(
          String.format("Could not update account data on registration for: %s", user.toString()));
    }

    createNewConsultingTypeFacade.initializeNewConsultingType(user, dbUser, consultingTypeSettings);
  }

  private String returnDummyEmailIfNoneGiven(UserDTO user, String userId) throws Exception {
    if (isBlank(user.getEmail())) {
      return keycloakAdminClientHelper.updateDummyEmail(userId, user);
    }

    return user.getEmail();
  }

  private void checkIfUserIdNotNull(String userId, UserDTO user) {
    if (isNull(userId)) {
      throw new InternalServerErrorException(
          String.format("Could not create Keycloak account for: %s", user.toString()));
    }
  }

}
