package de.caritas.cob.userservice.api.facade.userdata;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.service.ValidatedUserAccountProvider;
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

  /**
   * Returns the user data of the authenticated user preferred by role consultant.
   *
   * @return UserDataResponseDTO {@link UserDataResponseDTO}
   */
  public UserDataResponseDTO buildUserDataByRole() {
    Set<String> roles = authenticatedUser.getRoles();

    if (roles.contains(UserRole.CONSULTANT.getValue())) {
      return consultantDataProvider.retrieveData(userAccountProvider.retrieveValidatedConsultant());
    } else if (roles.contains(UserRole.USER.getValue())) {
      return askerDataProvider.retrieveData(userAccountProvider.retrieveValidatedUser());
    } else {
      throw new InternalServerErrorException(
          String.format("User with id %s has neither Consultant-Role, nor User-Role .",
              authenticatedUser.getUserId()));
    }
  }

}
