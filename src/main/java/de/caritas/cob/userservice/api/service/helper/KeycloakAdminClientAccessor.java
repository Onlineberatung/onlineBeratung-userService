package de.caritas.cob.userservice.api.service.helper;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Accessor class to provide the keycloak connection.
 */
@Service
public class KeycloakAdminClientAccessor {

  @Value("${keycloak.auth-server-url}")
  private String keycloakServerUrl;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  @Value("${keycloakService.admin.username}")
  private String keycloakUsername;

  @Value("${keycloakService.admin.password}")
  private String keycloakPassword;

  @Value("${keycloakService.admin.clientId}")
  private String keycloakClientId;

  /**
   * Returnes the {@link UsersResource} of current realm.
   *
   * @return the {@link UsersResource}
   */
  public UsersResource getUsersResource() {
    return getRealmResource().users();
  }

  /**
   * Returnes the {@link RealmResource} of current realm.
   *
   * @return the {@link RealmResource}
   */
  public RealmResource getRealmResource() {
    return getInstance().realm(this.keycloakRealm);
  }

  public String getBearerToken() {
    return getInstance().tokenManager().getAccessTokenString();
  }

  private Keycloak getInstance() {
    return Keycloak
        .getInstance(this.keycloakServerUrl, this.keycloakRealm, this.keycloakUsername,
            this.keycloakPassword, this.keycloakClientId);
  }

}
