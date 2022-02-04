package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.List;
import javax.ws.rs.core.Response;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class KeycloakAdminClientServiceTest {

  @InjectMocks
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private UserHelper userHelper;

  @Mock
  private UsernameTranscoder usernameTranscoder;

  @Mock
  private KeycloakAdminClientAccessor keycloakAdminClientAccessor;

  @Mock
  private Logger logger;

  EasyRandom easyRandom = new EasyRandom();

  @Before
  public void setup() {
    setField(keycloakAdminClientService, "usernameTranscoder", usernameTranscoder);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void createKeycloakUser_Should_createExpectedUser_When_keycloakReturnsCreated() {
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(HttpStatus.CREATED.value());
    when(usersResource.create(any())).thenReturn(response);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    KeycloakCreateUserResponseDTO keycloakUser = this.keycloakAdminClientService
        .createKeycloakUser(userDTO);

    assertThat(keycloakUser, notNullValue());
    assertThat(keycloakUser.getStatus(), is(HttpStatus.CREATED));
  }

  @Test
  public void createKeycloakUser_Should_createExpectedTenantAwareUser_When_keycloakReturnsCreated() {
    TenantContext.setCurrentTenant(1L);
    setField(keycloakAdminClientService, "multiTenancyEnabled", true);

    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    userDTO.setTenantId(1L);
    UsersResource usersResource = mock(UsersResource.class);
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(HttpStatus.CREATED.value());
    when(usersResource.create(any())).thenReturn(response);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    KeycloakCreateUserResponseDTO keycloakUser = this.keycloakAdminClientService
        .createKeycloakUser(userDTO);

    assertThat(keycloakUser, notNullValue());
    assertThat(keycloakUser.getStatus(), is(HttpStatus.CREATED));

    ArgumentCaptor<UserRepresentation> argumentCaptor = ArgumentCaptor
        .forClass(UserRepresentation.class);
    verify(usersResource, times(1)).create(argumentCaptor.capture());

    Assertions.assertEquals(argumentCaptor.getValue().getAttributes().get("tenantId").get(0),
        TenantContext.getCurrentTenant().toString());

    TenantContext.clear();
  }

  @Test
  public void createKeycloakUser_Should_throwExpectedStatusException_When_keycloakResponseHasEmailErrorMessage() {
    String emailError = "emailError";
    ReflectionTestUtils.setField(keycloakAdminClientService, "keycloakErrorEmail", emailError);
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn(emailError);
    Response response = mock(Response.class);
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(usersResource.create(any())).thenReturn(response);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    try {
      this.keycloakAdminClientService.createKeycloakUser(userDTO);
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0), is(EMAIL_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void createKeycloakUser_Should_throwExpectedStatusException_When_keycloakResponseHasUsernameErrorMessage() {
    String keycloakErrorUsername = "keycloakErrorUsername";
    ReflectionTestUtils
        .setField(keycloakAdminClientService, "keycloakErrorUsername", keycloakErrorUsername);
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn(keycloakErrorUsername);
    Response response = mock(Response.class);
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(usersResource.create(any())).thenReturn(response);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    try {
      this.keycloakAdminClientService.createKeycloakUser(userDTO);
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0), is(USERNAME_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void createKeycloakUser_Should_throwExpectedResponseException_When_keycloakMailUpdateFails() {
    String keycloakErrorUsername = "keycloakErrorUsername";
    ReflectionTestUtils
        .setField(keycloakAdminClientService, "keycloakErrorUsername", keycloakErrorUsername);
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn(keycloakErrorUsername);
    Response response = mock(Response.class);
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(usersResource.create(any())).thenReturn(response);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    try {
      this.keycloakAdminClientService.createKeycloakUser(userDTO);
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0), is(USERNAME_NOT_AVAILABLE.name()));
    }
  }

  @Test(expected = InternalServerErrorException.class)
  public void createKeycloakUser_Should_ThrowInternalServerException_When_errorIsUnknown() {
    UsersResource usersResource = mock(UsersResource.class);
    Response response = mock(Response.class);
    when(usersResource.create(any())).thenReturn(response);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn("error");
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);

    this.keycloakAdminClientService.createKeycloakUser(userDTO);
  }

  @Test
  public void isUsernameAvailable_Should_returnTrue_When_usernameIsAvailable() {
    UserRepresentation userMock = mock(UserRepresentation.class);
    when(userMock.getUsername()).thenReturn("Unique");
    List<UserRepresentation> userRepresentations =
        singletonList(userMock);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.search(any())).thenReturn(userRepresentations);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    boolean isAvailable = this.keycloakAdminClientService.isUsernameAvailable("username");

    assertThat(isAvailable, is(true));
  }

  @Test
  public void isUsernameAvailable_Should_returnFalse_When_DecodedUsernameIsNotAvailable() {
    String notUnique = "NotUnique";
    UserRepresentation userMock = easyRandom.nextObject(UserRepresentation.class);
    userMock.setUsername(notUnique);
    List<UserRepresentation> decodedUserRepresentations =
        singletonList(userMock);
    List<UserRepresentation> encodedUserRepresentations =
        singletonList(easyRandom.nextObject(UserRepresentation.class));
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.search(any()))
        .thenReturn(decodedUserRepresentations)
        .thenReturn(encodedUserRepresentations);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    when(usernameTranscoder.decodeUsername(any())).thenReturn(notUnique);

    boolean isAvailable = this.keycloakAdminClientService.isUsernameAvailable(notUnique);

    assertThat(isAvailable, is(false));
  }

  @Test
  public void isUsernameAvailable_Should_returnFalse_When_EncodedUsernameIsNotAvailable() {
    String notUnique = "enc.KVXGS4LVMU......";
    UserRepresentation userMock = easyRandom.nextObject(UserRepresentation.class);
    userMock.setUsername(notUnique);
    List<UserRepresentation> decodedUserRepresentations =
        singletonList(easyRandom.nextObject(UserRepresentation.class));
    List<UserRepresentation> encodedUserRepresentations =
        singletonList(userMock);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.search(any()))
        .thenReturn(decodedUserRepresentations)
        .thenReturn(encodedUserRepresentations);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    when(usernameTranscoder.encodeUsername(any())).thenReturn(notUnique);

    boolean isAvailable = this.keycloakAdminClientService.isUsernameAvailable(notUnique);

    assertThat(isAvailable, is(false));
  }

  @Test(expected = KeycloakException.class)
  public void updateRole_Should_throwKeycloakException_When_roleCouldNotBeUpdated() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(anyString())).thenReturn(userResource);
    RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);

    RoleRepresentation roleRepresentation = new EasyRandom().nextObject(RoleRepresentation.class);
    RoleResource roleResource = mock(RoleResource.class);
    when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
    RolesResource rolesResource = mock(RolesResource.class);
    when(rolesResource.get(any())).thenReturn(roleResource);

    RealmResource realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(this.keycloakAdminClientAccessor.getRealmResource()).thenReturn(realmResource);

    this.keycloakAdminClientService.updateRole("user", "role");
  }

  @Test
  public void updateRole_Should_updateRole_When_roleUpdateIsValid() {
    String validRole = "role";

    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(anyString())).thenReturn(userResource);
    RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
    RoleRepresentation keycloakRoleMock = mock(RoleRepresentation.class);
    when(keycloakRoleMock.toString()).thenReturn(validRole);
    when(roleScopeResource.listAll()).thenReturn(singletonList(keycloakRoleMock));
    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);

    RoleRepresentation roleRepresentation = new EasyRandom().nextObject(RoleRepresentation.class);
    RoleResource roleResource = mock(RoleResource.class);
    when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
    RolesResource rolesResource = mock(RolesResource.class);
    when(rolesResource.get(any())).thenReturn(roleResource);

    RealmResource realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(this.keycloakAdminClientAccessor.getRealmResource()).thenReturn(realmResource);

    this.keycloakAdminClientService.updateRole("user", validRole);

    verify(roleScopeResource, times(1)).add(any());
  }

  @Test
  public void updateRole_Should_updateUserWithProvidedRole() {
    UserRole validRole = UserRole.USER;

    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(anyString())).thenReturn(userResource);
    RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
    RoleRepresentation keycloakRoleMock = mock(RoleRepresentation.class);
    when(keycloakRoleMock.toString()).thenReturn(validRole.getValue());
    when(roleScopeResource.listAll()).thenReturn(singletonList(keycloakRoleMock));
    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);

    RoleRepresentation roleRepresentation = new EasyRandom().nextObject(RoleRepresentation.class);
    RoleResource roleResource = mock(RoleResource.class);
    when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
    RolesResource rolesResource = mock(RolesResource.class);
    when(rolesResource.get(any())).thenReturn(roleResource);

    RealmResource realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(this.keycloakAdminClientAccessor.getRealmResource()).thenReturn(realmResource);

    this.keycloakAdminClientService.updateRole("user", validRole);

    verify(roleScopeResource, times(1)).add(any());
    verify(rolesResource, times(1)).get(validRole.getValue());
  }

  @Test
  public void updatePassword_Should_callServicesCorrectly() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    this.keycloakAdminClientService.updatePassword("userId", "password");

    verify(userResource, times(1)).resetPassword(any());
  }

  @Test
  public void updateDummyMail_Should_callServicesCorrectly() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    when(this.userHelper.getDummyEmail(anyString())).thenReturn("dummy");

    String dummyMail = this.keycloakAdminClientService.updateDummyEmail("userId", new UserDTO());

    verify(userResource, times(1)).update(any());
    assertThat(dummyMail, is("dummy"));
  }

  @Test
  public void updateUserData_Should_callServicesCorrectly_When_emailIsChangedAndAvailable() {
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userRepresentation.getEmail()).thenReturn("email");
    UserResource userResource = mock(UserResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail("anotherEmail");

    this.keycloakAdminClientService.updateUserData("userId", userDTO, "firstName", "lastName");

    verify(userResource, times(1)).update(any());
  }

  @Test
  public void updateUserData_Should_callServicesCorrectly_When_emailIsUnchanged() {
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userRepresentation.getEmail()).thenReturn("email");
    UserResource userResource = mock(UserResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail("email");

    this.keycloakAdminClientService.updateUserData("userId", userDTO, "firstName", "lastName");

    verify(userResource, times(1)).update(any());
  }

  @Test
  public void updateUserData_Should_throwCustomException_When_emailIsChangedButNotAvailable() {
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userRepresentation.getEmail()).thenReturn("email");
    UserRepresentation otherUserRepresentation = mock(UserRepresentation.class);
    when(otherUserRepresentation.getEmail()).thenReturn("newemail");
    UserResource userResource = mock(UserResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(usersResource.search(any(), any(), any()))
        .thenReturn(singletonList(otherUserRepresentation));
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail("newemail");

    try {
      this.keycloakAdminClientService
          .updateUserData("userId", userDTO, "firstName", "lastName");
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0), is(EMAIL_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void rollbackUser_Should_callServicesCorrectly() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    this.keycloakAdminClientService.rollBackUser("userId");

    verify(userResource, times(1)).remove();
  }

  @Test
  public void rollbackUser_Should_logError_When_rollbackFails() {
    UserResource userResource = mock(UserResource.class);
    doThrow(new RuntimeException()).when(userResource).remove();
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    this.keycloakAdminClientService.rollBackUser("userId");

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void userHasAuthority_Should_returnTrue_When_userHasAuthority() {
    RoleRepresentation roleRepresentation = mock(RoleRepresentation.class);
    when(roleRepresentation.getName()).thenReturn("user");
    RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
    when(roleScopeResource.listAll()).thenReturn(singletonList(roleRepresentation));
    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    UserResource userResource = mock(UserResource.class);
    when(userResource.roles()).thenReturn(roleMappingResource);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    boolean hasAuthority = this.keycloakAdminClientService
        .userHasAuthority("user", AuthorityValue.USER_DEFAULT);

    assertThat(hasAuthority, is(true));
  }

  @Test(expected = KeycloakException.class)
  public void userHasAuthority_Should_returnThrowKeycloakException_When_userHasNoRoles() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    this.keycloakAdminClientService.userHasAuthority("user", "authority");
  }

  @Test
  public void userHasAuthority_Should_returnFalse_When_userHasNotAuthority() {
    RoleRepresentation roleRepresentation = mock(RoleRepresentation.class);
    when(roleRepresentation.getName()).thenReturn("user");
    RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
    when(roleScopeResource.listAll()).thenReturn(singletonList(roleRepresentation));
    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    UserResource userResource = mock(UserResource.class);
    when(userResource.roles()).thenReturn(roleMappingResource);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    boolean hasAuthority = this.keycloakAdminClientService
        .userHasAuthority("user", AuthorityValue.USER_ADMIN);

    assertThat(hasAuthority, is(false));
  }

  @Test
  public void closeSession_Should_deleteSession() {
    RealmResource realmResource = mock(RealmResource.class);
    when(this.keycloakAdminClientAccessor.getRealmResource()).thenReturn(realmResource);

    this.keycloakAdminClientService.closeSession("sessionId");

    verify(realmResource, times(1)).deleteSession(anyString());
  }

  @Test
  public void deactivateUser_Should_deactivateUser() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    this.keycloakAdminClientService.deactivateUser("userId");

    verify(userRepresentation, times(1)).setEnabled(false);
    verify(userResource, times(1)).update(userRepresentation);
  }

  @Test
  public void changeEmailAddress_Should_callServicesCorrectly_When_emailIsChangedAndAvailable() {
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userRepresentation.getEmail()).thenReturn("email");
    UserResource userResource = mock(UserResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    when(this.keycloakAdminClientAccessor.getUsersResource()).thenReturn(usersResource);

    this.keycloakAdminClientService.updateEmail("userId", "anotherEmail");

    verify(userRepresentation, times(1)).setEmail("anotherEmail");
    verify(userResource, times(1)).update(any());
  }

}
