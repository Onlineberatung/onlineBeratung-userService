package de.caritas.cob.userservice.api.testConfig;

import com.google.common.collect.Maps;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakClient;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakMapper;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@Slf4j
public class KeycloakTestConfig {

  @Bean
  public KeycloakService keycloakService(
      RestTemplate restTemplate,
      AuthenticatedUser authenticatedUser,
      UserAccountInputValidator userAccountInputValidator,
      IdentityClientConfig identityClientConfig,
      KeycloakClient keycloakClient,
      KeycloakMapper keycloakMapper,
      UserHelper userHelper) {

    return new KeycloakService(
        restTemplate,
        authenticatedUser,
        userAccountInputValidator,
        identityClientConfig,
        keycloakClient,
        keycloakMapper,
        userHelper) {
      @Override
      public boolean changePassword(String userId, String password) {
        return super.changePassword(userId, password);
      }

      @Override
      public void changeLanguage(String userId, String locale) {
        UserResource userResource = keycloakClient.getUsersResource().get(userId);
        UserRepresentation user = getUserRepresentationAndCreateNewUserIfNotExist(userResource);
        super.changeLanguageForTheUser(locale, userResource, user);
      }

      private UserRepresentation getUserRepresentationAndCreateNewUserIfNotExist(
          UserResource userResource) {
        var user = userResource.toRepresentation();
        if (user == null) {
          user = new UserRepresentation();
          user.setAttributes(Maps.newHashMap());
        }
        return user;
      }

      @Override
      public KeycloakLoginResponseDTO loginUser(String userName, String password) {
        return new KeycloakLoginResponseDTO("", 0, 0, "", "", "", "");
      }

      @Override
      public boolean logoutUser(String refreshToken) {
        return true;
      }

      @Override
      public void changeEmailAddress(String emailAddress) {
        log.debug("KeycloakService.changeEmailAddress called");
      }

      @Override
      public KeycloakCreateUserResponseDTO createKeycloakUser(UserDTO user) {

        KeycloakCreateUserResponseDTO keycloakUserDTO = new KeycloakCreateUserResponseDTO();
        keycloakUserDTO.setUserId("keycloak-user-id " + RandomStringUtils.randomNumeric(5));
        keycloakUserDTO.setStatus(HttpStatus.OK);
        /*if (shouldGenerateNewUsername(user)) {
          keycloakUserDTO.setUserId("keycloak-user-id" + RandomStringUtils.randomNumeric(5));
        }*/
        return keycloakUserDTO;
      }

      private boolean shouldGenerateNewUsername(UserDTO user) {
        return user.getUserGender() != null;
      }

      @Override
      public String updateDummyEmail(String userId, UserDTO user) {
        var dummyMail = userId + "@dummy.du";
        user.setEmail(dummyMail);
        return dummyMail;
      }

      @Override
      public void updateUserRole(String userId) {}

      @Override
      public void updateRole(String userId, UserRole role) {}

      @Override
      public void updateRole(String userId, String roleName) {}

      @Override
      public void removeRoleIfPresent(String userId, String roleName) {}

      @Override
      public void updatePassword(String userId, String password) {}

      @Override
      public void updateUserData(
          String userId, UserDTO userDTO, String firstName, String lastName) {}

      @Override
      public void updateEmail(String userId, String emailAddress) {}

      @Override
      public void rollBackUser(String userId) {}

      @Override
      public void deleteUser(String userId) {}

      @Override
      public void deactivateUser(String userId) {}

      @Override
      public boolean userHasRole(String userId, String userRole) {
        return true;
      }
    };
  }
}
