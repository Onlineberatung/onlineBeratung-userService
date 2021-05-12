package de.caritas.cob.userservice.api.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityTest {

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleConsultant() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(Authority.CONSULTANT_DEFAULT));
    assertEquals(1, result.size());

  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleUser() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.USER);

    assertNotNull(result);
    assertTrue(result.contains(Authority.USER_DEFAULT));
    assertEquals(1, result.size());

  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleAnonymous() {
    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.ANONYMOUS);

    assertNotNull(result);
    assertTrue(result.contains(Authority.ANONYMOUS_DEFAULT));
    assertEquals(1, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleU25Consultant() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.U25_CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(Authority.USE_FEEDBACK));
    assertEquals(1, result.size());

  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleU25MainConsultant() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.U25_MAIN_CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(Authority.VIEW_ALL_FEEDBACK_SESSIONS));
    assertTrue(result.contains(Authority.VIEW_ALL_PEER_SESSIONS));
    assertTrue(result.contains(Authority.ASSIGN_CONSULTANT_TO_SESSION));
    assertTrue(result.contains(Authority.ASSIGN_CONSULTANT_TO_ENQUIRY));
    assertTrue(result.contains(Authority.VIEW_AGENCY_CONSULTANTS));
    assertEquals(5, result.size());

  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleTechnical() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.TECHNICAL);

    assertNotNull(result);
    assertTrue(result.contains(Authority.TECHNICAL_DEFAULT));
    assertEquals(1, result.size());

  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_ForKeycloakRoleKreuzbundConsultant() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.GROUP_CHAT_CONSULTANT);

    assertNotNull(result);
    assertTrue(result.contains(Authority.CONSULTANT_DEFAULT));
    assertTrue(result.contains(Authority.CREATE_NEW_CHAT));
    assertTrue(result.contains(Authority.START_CHAT));
    assertTrue(result.contains(Authority.STOP_CHAT));
    assertTrue(result.contains(Authority.UPDATE_CHAT));
    assertEquals(5, result.size());

  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_When_keycloakRoleIsUserAdmin() {

    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.USER_ADMIN);

    assertNotNull(result);
    assertTrue(result.contains(Authority.USER_ADMIN));
    assertEquals(1, result.size());
  }

  @Test
  public void getAuthoritiesByRoleName_Should_ReturnCorrectRoles_When_KeycloakRoleIsAnonymous() {
    List<String> result = Authorities.getAuthoritiesByUserRole(UserRole.ANONYMOUS);

    assertNotNull(result);
    assertTrue(result.contains(Authority.ANONYMOUS_DEFAULT));
    assertEquals(1, result.size());
  }
}
