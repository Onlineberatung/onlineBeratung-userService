package de.caritas.cob.UserService.api.service.helper.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;

/**
 * Aspect for adding a log out of the Admin CLI after the annotated method was executed.
 *
 */

@Aspect
@Component
public class KeycloakAdminClientLogoutAspect {

  private KeycloakAdminClientHelper keycloakHelper;

  @Autowired
  public KeycloakAdminClientLogoutAspect(KeycloakAdminClientHelper keycloakHelper) {
    this.keycloakHelper = keycloakHelper;
  }

  @After("@annotation(KeycloakAdminClientLogout)")
  public void keycloakAdminClientLogout() {
    keycloakHelper.closeInstance();
  }
}
