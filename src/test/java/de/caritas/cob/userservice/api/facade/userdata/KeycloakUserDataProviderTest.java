package de.caritas.cob.userservice.api.facade.userdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeycloakUserDataProviderTest {

  @Mock AuthenticatedUser authenticatedUser;

  @Mock KeycloakService keycloakService;

  @InjectMocks KeycloakUserDataProvider keycloakUserDataProvider;

  @Test
  void retrieveData_Should_ThrowExceptionIfCalledInAnonymousUserContext() {
    // given
    Mockito.when(authenticatedUser.isAnonymous()).thenReturn(true);
    // when, then
    assertThrows(
        IllegalArgumentException.class,
        () -> keycloakUserDataProvider.retrieveAuthenticatedUserData());
  }

  @Test
  void retrieveData_Should_CallKeycloakAndFindExactlyOneUser() {
    // given
    Mockito.when(authenticatedUser.isAnonymous()).thenReturn(false);
    Mockito.when(authenticatedUser.getUserId()).thenReturn("userId");
    UserRepresentation userRepresentation = giveUserRepresentation();
    Mockito.when(keycloakService.getById("userId")).thenReturn(userRepresentation);
    // when
    UserDataResponseDTO userDataResponseDTO =
        keycloakUserDataProvider.retrieveAuthenticatedUserData();
    // then
    assertKeycloakUserRepresentationAttributesConverted(userRepresentation, userDataResponseDTO);
    assertRolesTakenFromAuthenticatedUserBean(userDataResponseDTO);
    assertOtherDtoAttributesSetToDefaults(userDataResponseDTO);
  }

  private void assertRolesTakenFromAuthenticatedUserBean(UserDataResponseDTO userDataResponseDTO) {
    assertThat(userDataResponseDTO.getUserRoles()).isEqualTo(authenticatedUser.getRoles());
    assertThat(userDataResponseDTO.getGrantedAuthorities())
        .isEqualTo(authenticatedUser.getGrantedAuthorities());
  }

  private void assertOtherDtoAttributesSetToDefaults(UserDataResponseDTO userDataResponseDTO) {
    assertThat(userDataResponseDTO.getEncourage2fa()).isFalse();
    assertThat(userDataResponseDTO.getAbsenceMessage()).isEmpty();
    assertThat(userDataResponseDTO.isInTeamAgency()).isFalse();
    assertThat(userDataResponseDTO.isHasAnonymousConversations()).isFalse();
    assertThat(userDataResponseDTO.isHasArchive()).isFalse();
    assertThat(userDataResponseDTO.getAgencies()).isEmpty();
  }

  private void assertKeycloakUserRepresentationAttributesConverted(
      UserRepresentation userRepresentation, UserDataResponseDTO userDataResponseDTO) {
    assertThat(userDataResponseDTO.getUserId()).isEqualTo(userRepresentation.getId());
    assertThat(userDataResponseDTO.getUserName()).isEqualTo(userRepresentation.getUsername());
    assertThat(userDataResponseDTO.getEmail()).isEqualTo(userRepresentation.getEmail());
    assertThat(userDataResponseDTO.getFirstName()).isEqualTo(userRepresentation.getFirstName());
    assertThat(userDataResponseDTO.getLastName()).isEqualTo(userRepresentation.getLastName());
  }

  private UserRepresentation giveUserRepresentation() {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername("username");
    userRepresentation.setFirstName("firstname");
    userRepresentation.setLastName("lastname");
    userRepresentation.setId("id");
    userRepresentation.setEmail("email");
    return userRepresentation;
  }
}
