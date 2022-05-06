package de.caritas.cob.userservice.api.facade.userdata;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Provider for consultant information.
 */
@Component
@RequiredArgsConstructor
public class KeycloakUserDataProvider {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull IdentityClient identityClient;

  public UserDataResponseDTO retrieveAuthenticatedUserData() {
    assertCalledInAuthenticatedUserContext();
    var user = identityClient.findByUsername(
        authenticatedUser.getUsername());
    assertUniqueUserWasFound(user);
    return userDataResponseDtoOf(user.get(0));
  }

  private void assertCalledInAuthenticatedUserContext() {
    Assert.isTrue(!authenticatedUser.isAnonymous(), "Cannot retrieve keycloak data for anonymous users");
  }

  private void assertUniqueUserWasFound(List<UserRepresentation> user) {
    Assert.notEmpty(user, "user representation should be non empty");
    Assert.isTrue(!user.isEmpty(), "no user representation found");
    Assert.isTrue(user.size() == 1, "user representation should be unique");
  }

  private UserDataResponseDTO userDataResponseDtoOf(UserRepresentation keycloakUser) {

    return UserDataResponseDTO.builder()
        .userId(keycloakUser.getId())
        .userName(keycloakUser.getUsername())
        .firstName(keycloakUser.getFirstName())
        .lastName(keycloakUser.getLastName())
        .email(keycloakUser.getEmail())
        .encourage2fa(false)
        .absenceMessage("")
        .isInTeamAgency(false)
        .agencies(Lists.newArrayList())
        .userRoles(authenticatedUser.getRoles())
        .grantedAuthorities(authenticatedUser.getGrantedAuthorities())
        .hasAnonymousConversations(false)
        .hasArchive(false)
        .build();
  }
}