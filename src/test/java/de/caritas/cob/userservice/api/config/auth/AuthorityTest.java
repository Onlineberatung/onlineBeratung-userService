package de.caritas.cob.userservice.api.config.auth;

import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CONSULTANT_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.USER_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_AGENCY_CONSULTANTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityTest {

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleConsultant() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.CONSULTANT);

    assertNotNull(result);
    assertThat(
        result,
        containsInAnyOrder(
            CONSULTANT_DEFAULT, ASSIGN_CONSULTANT_TO_SESSION, VIEW_AGENCY_CONSULTANTS));
    assertEquals(3, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleUser() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.USER);

    assertNotNull(result);
    assertThat(result, contains(USER_DEFAULT, ASSIGN_CONSULTANT_TO_SESSION));
    assertEquals(2, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleAnonymous() {
    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.ANONYMOUS);

    assertNotNull(result);
    assertTrue(result.contains(AuthorityValue.ANONYMOUS_DEFAULT));
    assertEquals(1, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRolePeerConsultant() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.PEER_CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(AuthorityValue.USE_FEEDBACK));
    assertEquals(1, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleMainConsultant() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.MAIN_CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS));
    assertTrue(result.contains(AuthorityValue.VIEW_ALL_PEER_SESSIONS));
    assertTrue(result.contains(AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY));
    assertTrue(result.contains(AuthorityValue.ASSIGN_CONSULTANT_TO_PEER_SESSION));
    assertEquals(4, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleTechnical() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.TECHNICAL);

    assertNotNull(result);
    assertTrue(result.contains(AuthorityValue.TECHNICAL_DEFAULT));
    assertEquals(1, result.size());
  }

  @Test
  public void
      getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleKreuzbundConsultant() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.GROUP_CHAT_CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(CONSULTANT_DEFAULT));
    assertTrue(result.contains(AuthorityValue.CREATE_NEW_CHAT));
    assertTrue(result.contains(AuthorityValue.START_CHAT));
    assertTrue(result.contains(AuthorityValue.STOP_CHAT));
    assertTrue(result.contains(AuthorityValue.UPDATE_CHAT));
    assertEquals(5, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_When_keycloakRoleIsUserAdmin() {

    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.USER_ADMIN);

    assertNotNull(result);
    assertTrue(result.contains(AuthorityValue.USER_ADMIN));
    assertTrue(result.contains(AuthorityValue.CONSULTANT_CREATE));
    assertTrue(result.contains(AuthorityValue.CONSULTANT_UPDATE));
    assertEquals(3, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_When_KeycloakRoleIsAnonymous() {
    List<String> result = Authority.getAuthoritiesByUserRole(UserRole.ANONYMOUS);

    assertNotNull(result);
    assertTrue(result.contains(AuthorityValue.ANONYMOUS_DEFAULT));
    assertEquals(1, result.size());
  }
}
