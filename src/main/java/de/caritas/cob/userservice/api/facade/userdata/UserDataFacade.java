package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.api.authorization.UserRole.ANONYMOUS;
import static de.caritas.cob.userservice.api.authorization.UserRole.CONSULTANT;
import static de.caritas.cob.userservice.api.authorization.UserRole.USER;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.TwoFactorAuthValidator;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.Arrays;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate getting the user data (e.g. agencies, consulting types, ...) for the
 * corresponding user.
 */
@Service
@RequiredArgsConstructor
public class UserDataFacade {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ValidatedUserAccountProvider userAccountProvider;
  private final @NonNull ConsultantDataProvider consultantDataProvider;
  private final @NonNull AskerDataProvider askerDataProvider;
  private final @NonNull TwoFactorAuthValidator twoFactorAuthValidator;

  /**
   * Returns the user data of the authenticated user preferred by role consultant.
   *
   * @return UserDataResponseDTO {@link UserDataResponseDTO}
   */
  public UserDataResponseDTO buildUserDataByRole() {
    UserDataResponseDTO userDataResponseDTO;
    Set<String> roles = authenticatedUser.getRoles();

    if (userRolesContainAnyRoleOf(roles, CONSULTANT.getValue())) {
      userDataResponseDTO = consultantDataProvider.retrieveData(userAccountProvider.retrieveValidatedConsultant());
    } else if (userRolesContainAnyRoleOf(roles, USER.getValue(), ANONYMOUS.getValue())) {
      userDataResponseDTO = askerDataProvider.retrieveData(userAccountProvider.retrieveValidatedUser());
    } else {
      throw new InternalServerErrorException(
          String.format("User with id %s has neither Consultant-Role, nor User/Anonymous-Role .",
              authenticatedUser.getUserId()));
    }

    userDataResponseDTO.setTwoFactorAuth(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser));
    return userDataResponseDTO;
  }

  private boolean userRolesContainAnyRoleOf(Set<String> userRoles, String... roles) {
    return Arrays.stream(roles)
        .anyMatch(userRoles::contains);
  }

}
