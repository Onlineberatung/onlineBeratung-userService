package de.caritas.cob.userservice.api.adapters.keycloak;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.OTP_INFO_DTO;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class KeycloakServiceTest {

  private final String USER_ID = "asdh89sdfsjodifjsdf";
  private final String OLD_PW = "oldP@66w0rd!";
  private final String NEW_PW = "newP@66w0rd!";
  private final String REFRESH_TOKEN = "s09djf0w9ejf09wsejf09wjef";
  private static final String BEARER_TOKEN = "token";
  private static final String USERNAME = "testuser";

  @InjectMocks private KeycloakService keycloakService;

  @Mock private RestTemplate restTemplate;
  @Mock private Logger logger;
  @Mock private AuthenticatedUser authenticatedUser;
  @Mock private UserAccountInputValidator userAccountInputValidator;
  @Mock private IdentityClientConfig identityClientConfig;
  @Mock private KeycloakClient keycloakClient;

  @Mock
  @SuppressWarnings("unused")
  private KeycloakMapper keycloakMapper;

  @Mock private UsernameTranscoder usernameTranscoder;
  @Mock private UserHelper userHelper;

  @Mock UsersResource usersResource;

  EasyRandom easyRandom = new EasyRandom();

  @BeforeEach
  public void setup() throws NoSuchFieldException, SecurityException {
    givenAKeycloakLoginUrl();
    givenAKeycloakLogoutUrl();
    setField(keycloakService, "keycloakClientId", "app");
    setField(keycloakService, "usernameTranscoder", usernameTranscoder);
    setField(keycloakService, "multiTenancyEnabled", false);
    setInternalState(KeycloakService.class, "log", logger);
  }

  @Test
  public void changePassword_Should_ReturnTrue_When_KeycloakPasswordChangeWasSuccessful() {
    var usersResource = mock(UsersResource.class);
    var userResource = mock(UserResource.class);
    when(usersResource.get(USER_ID)).thenReturn(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    assertTrue(keycloakService.changePassword(USER_ID, NEW_PW));
  }

  @Test
  public void
      changePassword_Should_ReturnFalseAndLogError_When_KeycloakPasswordChangeFailsWithException() {
    assertFalse(keycloakService.changePassword(USER_ID, NEW_PW));
    verify(logger, atLeastOnce()).info(anyString(), any(Object.class));
  }

  @Test
  public void loginUser_Should_ReturnKeycloakLoginResponseDTO_When_KeycloakLoginWasSuccessful() {
    KeycloakLoginResponseDTO loginResponseDTO =
        new EasyRandom().nextObject(KeycloakLoginResponseDTO.class);
    when(restTemplate.postForEntity(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<KeycloakLoginResponseDTO>>any()))
        .thenReturn(new ResponseEntity<>(loginResponseDTO, HttpStatus.OK));

    KeycloakLoginResponseDTO response = keycloakService.loginUser(USER_ID, OLD_PW);

    assertThat(response, instanceOf(KeycloakLoginResponseDTO.class));
  }

  @Test
  public void loginUser_Should_ReturnBadRequest_When_KeycloakLoginFails() {
    var exception =
        new RestClientResponseException("some exception", 500, "text", null, null, null);
    when(restTemplate.postForEntity(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<KeycloakLoginResponseDTO>>any()))
        .thenThrow(exception);

    try {
      keycloakService.loginUser(USER_ID, OLD_PW);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue(true, "Excepted BadRequestException thrown");
    }
  }

  @Test
  public void logoutUser_Should_ReturnTrue_When_KeycloakLoginWasSuccessful() {
    when(restTemplate.postForEntity(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<Void>>any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

    assertTrue(keycloakService.logoutUser(REFRESH_TOKEN));
  }

  @Test
  public void logoutUser_Should_ReturnFalseAndLogError_WhenKeycloakLogoutFailsWithException() {
    RestClientException exception = new RestClientException("error");
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(), any()))
        .thenThrow(exception);

    boolean response = keycloakService.logoutUser(REFRESH_TOKEN);

    assertFalse(response);
    verify(logger, atLeastOnce()).error(anyString(), anyString(), any(Exception.class));
  }

  @Test
  public void logoutUser_Should_ReturnFalseAndLogError_When_KeycloakLogoutFails() {
    when(restTemplate.postForEntity(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<Void>>any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    boolean response = keycloakService.logoutUser(REFRESH_TOKEN);

    assertFalse(response);
    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void changeEmailAddress_Should_useServicesCorrectly() {
    when(this.authenticatedUser.getUserId()).thenReturn("userId");
    UserRepresentation userRepresentation =
        givenUserRepresentationWithFilledEmail(RandomStringUtils.randomAlphanumeric(8));
    UserResource userResource = givenUserResource(userRepresentation);
    UsersResource usersResource = givenUsersResource(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    var email = RandomStringUtils.randomAlphabetic(8);

    this.keycloakService.changeEmailAddress(email);

    verify(this.userAccountInputValidator, times(1)).validateEmailAddress(email);
    verify(this.authenticatedUser, times(1)).getUserId();
  }

  private UserRepresentation givenUserRepresentationWithFilledEmail(String email) {
    var userRepresentation = mock(UserRepresentation.class);
    when(userRepresentation.getEmail()).thenReturn(email);
    return userRepresentation;
  }

  @Test
  public void changeEmailAddress_Should_NotThrowNPEIfUserDoesNotHaveEmailDefinedInKeycloak() {
    when(this.authenticatedUser.getUserId()).thenReturn("userId");
    UserRepresentation userRepresentation = givenUserRepresentationWithNullEmail();
    UserResource userResource = givenUserResource(userRepresentation);
    UsersResource usersResource = givenUsersResource(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    var email = RandomStringUtils.randomAlphabetic(8);

    this.keycloakService.changeEmailAddress(email);

    verify(this.userAccountInputValidator, times(1)).validateEmailAddress(email);
    verify(this.authenticatedUser, times(1)).getUserId();
  }

  private UserRepresentation givenUserRepresentationWithNullEmail() {
    UserRepresentation userRepresentation = givenUserRepresentationWithFilledEmail(null);
    return userRepresentation;
  }

  private UsersResource givenUsersResource(UserResource userResource) {
    var usersResource = mock(UsersResource.class);
    when(usersResource.get("userId")).thenReturn(userResource);
    when(usersResource.search(anyString(), eq(0), eq(Integer.MAX_VALUE))).thenReturn(List.of());
    return usersResource;
  }

  private UserResource givenUserResource(UserRepresentation userRepresentation) {
    var userResource = mock(UserResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    return userResource;
  }

  @Test
  public void deleteEmailAddress_Should_useServicesCorrectly() {
    var userId = random(16);
    when(authenticatedUser.getUserId()).thenReturn(userId);
    when(userHelper.getDummyEmail(userId)).thenReturn("dummy");
    var usersResource = mock(UsersResource.class);
    var userResource = mock(UserResource.class);
    UserRepresentation userRepresentation =
        givenUserRepresentationWithFilledEmail(RandomStringUtils.randomAlphanumeric(8));
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    when(usersResource.get(userId)).thenReturn(userResource);
    when(usersResource.search(anyString(), eq(0), eq(Integer.MAX_VALUE))).thenReturn(List.of());
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    keycloakService.deleteEmailAddress();

    Mockito.verify(userResource, times(1)).update(any());
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void getOtpCredential_Should_Return_Response_When_RequestWasSuccessful() {
    when(keycloakClient.getBearerToken()).thenReturn(BEARER_TOKEN);
    var entity = new ResponseEntity(OTP_INFO_DTO, HttpStatus.OK);
    when(this.keycloakClient.get(anyString(), any(), any())).thenReturn(entity);

    assertEquals(OTP_INFO_DTO, keycloakService.getOtpCredential(USERNAME));
  }

  @Test
  public void getOtpCredential_Should_Throw_When_RequestHasAnError() {
    assertThrows(
        RestClientException.class,
        () -> {
          when(keycloakClient.getBearerToken()).thenReturn(BEARER_TOKEN);
          when(this.keycloakClient.get(any(), any(), any()))
              .thenThrow(new RestClientException("Fail test case"));

          keycloakService.getOtpCredential(USERNAME);
        });
  }

  @Test
  public void
      setUpOtpCredential_ShouldNot_ThrowInternalServerErrorException_When_RequestWasSuccessfully() {
    when(keycloakClient.getBearerToken()).thenReturn(BEARER_TOKEN);

    assertDoesNotThrow(
        () ->
            keycloakService.setUpOtpCredential(USERNAME, randomAlphabetic(8), randomAlphabetic(8)));
  }

  @Test
  public void
      deleteOtpCredential_Should_Not_ThrowBadRequestException_When_RequestWasSuccessfully() {
    when(keycloakClient.getBearerToken()).thenReturn(BEARER_TOKEN);

    assertDoesNotThrow(() -> keycloakService.deleteOtpCredential(USERNAME));
  }

  private void givenAKeycloakLoginUrl() {
    when(identityClientConfig.getOpenIdConnectUrl(anyString()))
        .thenReturn(
            "https://caritas.local/auth/realms/online-beratung/protocol/openid-connect/token");
  }

  private void givenAKeycloakLogoutUrl() {
    when(identityClientConfig.getOpenIdConnectUrl(anyString()))
        .thenReturn(
            "https://caritas.local/auth/realms/online-beratung/protocol/openid-connect/logout");
  }

  @Test
  public void createKeycloakUser_Should_createExpectedUser_When_keycloakReturnsCreated() {
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(HttpStatus.CREATED.value());
    when(usersResource.create(any())).thenReturn(response);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    KeycloakCreateUserResponseDTO keycloakUser = this.keycloakService.createKeycloakUser(userDTO);

    assertThat(keycloakUser, notNullValue());
    assertThat(keycloakUser.getStatus(), is(HttpStatus.CREATED));
  }

  @Test
  public void
      createKeycloakUser_Should_createExpectedTenantAwareUser_When_keycloakReturnsCreated() {
    TenantContext.setCurrentTenant(1L);
    setField(keycloakService, "multiTenancyEnabled", true);

    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    userDTO.setTenantId(1L);
    UsersResource usersResource = mock(UsersResource.class);
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(HttpStatus.CREATED.value());
    when(usersResource.create(any())).thenReturn(response);
    when(this.keycloakClient.getUsersResource()).thenReturn(usersResource);

    KeycloakCreateUserResponseDTO keycloakUser = this.keycloakService.createKeycloakUser(userDTO);

    assertThat(keycloakUser, notNullValue());
    assertThat(keycloakUser.getStatus(), is(HttpStatus.CREATED));

    ArgumentCaptor<UserRepresentation> argumentCaptor =
        ArgumentCaptor.forClass(UserRepresentation.class);
    verify(usersResource, times(1)).create(argumentCaptor.capture());

    Assertions.assertEquals(
        argumentCaptor.getValue().getAttributes().get("tenantId").get(0),
        TenantContext.getCurrentTenant().toString());

    TenantContext.clear();
  }

  @Test
  public void createKeycloakUser_Should_createUserWithDefaultLocale() {
    var userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setPreferredLanguage(null);
    var usersResource = mock(UsersResource.class);
    var response = mock(Response.class);
    when(response.getStatus()).thenReturn(HttpStatus.CREATED.value());
    when(usersResource.create(any())).thenReturn(response);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    var keycloakUser = keycloakService.createKeycloakUser(userDTO);

    assertThat(keycloakUser.getStatus(), is(HttpStatus.CREATED));

    var argumentCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
    verify(usersResource).create(argumentCaptor.capture());

    var locales = argumentCaptor.getValue().getAttributes().get("locale");
    assertEquals("de", locales.get(0));
  }

  @Test
  public void
      createKeycloakUser_Should_throwExpectedStatusException_When_keycloakResponseHasEmailErrorMessage() {
    var emailError = givenADuplicatedEmailErrorMessage();
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn(emailError);
    Response response = mock(Response.class);
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(usersResource.create(any())).thenReturn(response);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    try {
      this.keycloakService.createKeycloakUser(userDTO);
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders(), notNullValue());
      assertThat(e.getCustomHttpHeaders().get("X-Reason").get(0), is(EMAIL_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void
      createKeycloakUser_Should_throwExpectedStatusException_When_keycloakResponseHasUsernameErrorMessage() {
    var keycloakErrorUsername = givenADuplicatedUserErrorMessage();
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn(keycloakErrorUsername);
    Response response = mock(Response.class);
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(usersResource.create(any())).thenReturn(response);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    try {
      this.keycloakService.createKeycloakUser(userDTO);
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders(), notNullValue());
      assertThat(
          e.getCustomHttpHeaders().get("X-Reason").get(0), is(USERNAME_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void
      createKeycloakUser_Should_throwExpectedResponseException_When_keycloakMailUpdateFails() {
    var keycloakErrorUsername = givenADuplicatedUserErrorMessage();
    UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);
    UsersResource usersResource = mock(UsersResource.class);
    ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
    when(errorRepresentation.getErrorMessage()).thenReturn(keycloakErrorUsername);
    Response response = mock(Response.class);
    when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
    when(usersResource.create(any())).thenReturn(response);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    try {
      this.keycloakService.createKeycloakUser(userDTO);
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders(), notNullValue());
      assertThat(
          e.getCustomHttpHeaders().get("X-Reason").get(0), is(USERNAME_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void createKeycloakUser_Should_ThrowInternalServerException_When_errorIsUnknown() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          UsersResource usersResource = mock(UsersResource.class);
          Response response = mock(Response.class);
          when(usersResource.create(any())).thenReturn(response);
          ErrorRepresentation errorRepresentation = mock(ErrorRepresentation.class);
          when(errorRepresentation.getErrorMessage()).thenReturn("error");
          when(response.readEntity(ErrorRepresentation.class)).thenReturn(errorRepresentation);
          when(keycloakClient.getUsersResource()).thenReturn(usersResource);
          UserDTO userDTO = new EasyRandom().nextObject(UserDTO.class);

          this.keycloakService.createKeycloakUser(userDTO);
        });
  }

  @Test
  public void isUsernameAvailable_Should_returnTrue_When_usernameIsAvailable() {
    UserRepresentation userMock = mock(UserRepresentation.class);
    when(userMock.getUsername()).thenReturn("Unique");
    List<UserRepresentation> userRepresentations = singletonList(userMock);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.search(any())).thenReturn(userRepresentations);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    boolean isAvailable = this.keycloakService.isUsernameAvailable("username");

    assertThat(isAvailable, is(true));
  }

  @Test
  public void isUsernameAvailable_Should_returnFalse_When_DecodedUsernameIsNotAvailable() {
    String notUnique = "NotUnique";
    UserRepresentation userMock = easyRandom.nextObject(UserRepresentation.class);
    userMock.setUsername(notUnique);
    List<UserRepresentation> decodedUserRepresentations = singletonList(userMock);
    List<UserRepresentation> encodedUserRepresentations =
        singletonList(easyRandom.nextObject(UserRepresentation.class));
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.search(any()))
        .thenReturn(decodedUserRepresentations)
        .thenReturn(encodedUserRepresentations);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    when(usernameTranscoder.decodeUsername(any())).thenReturn(notUnique);

    boolean isAvailable = this.keycloakService.isUsernameAvailable(notUnique);

    assertThat(isAvailable, is(false));
  }

  @Test
  public void isUsernameAvailable_Should_returnFalse_When_EncodedUsernameIsNotAvailable() {
    String notUnique = "enc.KVXGS4LVMU......";
    UserRepresentation userMock = easyRandom.nextObject(UserRepresentation.class);
    userMock.setUsername(notUnique);
    List<UserRepresentation> decodedUserRepresentations =
        singletonList(easyRandom.nextObject(UserRepresentation.class));
    List<UserRepresentation> encodedUserRepresentations = singletonList(userMock);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.search(any()))
        .thenReturn(decodedUserRepresentations)
        .thenReturn(encodedUserRepresentations);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    when(usernameTranscoder.encodeUsername(any())).thenReturn(notUnique);

    boolean isAvailable = this.keycloakService.isUsernameAvailable(notUnique);

    assertThat(isAvailable, is(false));
  }

  @Test
  public void updateRole_Should_throwKeycloakException_When_roleCouldNotBeUpdated() {
    assertThrows(
        KeycloakException.class,
        () -> {
          UserResource userResource = mock(UserResource.class);
          UsersResource usersResource = mock(UsersResource.class);
          when(usersResource.get(anyString())).thenReturn(userResource);
          RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
          RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
          when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
          when(userResource.roles()).thenReturn(roleMappingResource);

          RoleRepresentation roleRepresentation =
              new EasyRandom().nextObject(RoleRepresentation.class);
          RoleResource roleResource = mock(RoleResource.class);
          when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
          RolesResource rolesResource = mock(RolesResource.class);
          when(rolesResource.get(any())).thenReturn(roleResource);

          RealmResource realmResource = mock(RealmResource.class);
          when(realmResource.users()).thenReturn(usersResource);
          when(realmResource.roles()).thenReturn(rolesResource);
          when(keycloakClient.getRealmResource()).thenReturn(realmResource);

          this.keycloakService.updateRole("user", "role");
        });
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
    when(keycloakClient.getRealmResource()).thenReturn(realmResource);

    this.keycloakService.updateRole("user", validRole);

    verify(roleScopeResource, times(1)).add(any());
  }

  @Test
  public void removeRole_Should_removeRole_When_rolePresent() {
    String validRole = "role";

    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(anyString())).thenReturn(userResource);
    RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
    RoleRepresentation keycloakRoleMock = mock(RoleRepresentation.class);
    when(keycloakRoleMock.getName()).thenReturn(validRole);
    when(roleScopeResource.listAll()).thenReturn(singletonList(keycloakRoleMock));
    when(roleScopeResource.listAll()).thenReturn(singletonList(keycloakRoleMock));
    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);

    RoleRepresentation roleRepresentation = new EasyRandom().nextObject(RoleRepresentation.class);
    roleRepresentation.setName("role");
    RoleResource roleResource = mock(RoleResource.class);
    when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
    RolesResource rolesResource = mock(RolesResource.class);
    when(rolesResource.get(any())).thenReturn(roleResource);

    RealmResource realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(keycloakClient.getRealmResource()).thenReturn(realmResource);

    this.keycloakService.removeRoleIfPresent("user", validRole);

    verify(roleScopeResource, times(1)).remove(any());
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
    when(keycloakClient.getRealmResource()).thenReturn(realmResource);

    this.keycloakService.updateRole("user", validRole);

    verify(roleScopeResource, times(1)).add(any());
    verify(rolesResource, times(1)).get(validRole.getValue());
  }

  @Test
  public void updatePassword_Should_callServicesCorrectly() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    this.keycloakService.updatePassword("userId", "password");

    verify(userResource, times(1)).resetPassword(any());
  }

  @Test
  public void updateDummyMail_id_dto_Should_callServicesCorrectly() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    when(this.userHelper.getDummyEmail(anyString())).thenReturn("dummy");

    String dummyMail = this.keycloakService.updateDummyEmail("userId", new UserDTO());

    verify(userResource, times(1)).update(any());
    assertThat(dummyMail, is("dummy"));
  }

  @Test
  public void updateDummyMail_id_Should_callServicesCorrectly() {
    var userRepresentation = mock(UserRepresentation.class);
    var userResource = mock(UserResource.class);
    var usersResource = mock(UsersResource.class);

    when(userRepresentation.getEmail()).thenReturn("email");
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    when(usersResource.get("userId")).thenReturn(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    when(userHelper.getDummyEmail(anyString())).thenReturn("dummy");

    keycloakService.updateDummyEmail("userId");

    verify(userResource).update(any());
  }

  @Test
  public void updateUserData_Should_callServicesCorrectly_When_emailIsChangedAndAvailable() {
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail("anotherEmail");

    this.keycloakService.updateUserData("userId", userDTO, "firstName", "lastName");

    verify(userResource, times(1)).update(any());
  }

  @Test
  public void updateUserData_Should_callServicesCorrectly_When_emailIsUnchanged() {
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail("email");

    this.keycloakService.updateUserData("userId", userDTO, "firstName", "lastName");

    verify(userResource, times(1)).update(any());
  }

  @Test
  public void updateUserData_Should_throwCustomException_When_emailIsChangedButNotAvailable() {
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserRepresentation otherUserRepresentation = givenUserRepresentation("newemail");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(usersResource.search(any(), any(), any()))
        .thenReturn(singletonList(otherUserRepresentation));
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail("newemail");

    try {
      this.keycloakService.updateUserData("userId", userDTO, "firstName", "lastName");
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders().get("X-Reason").get(0), is(EMAIL_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void rollbackUser_Should_callServicesCorrectly() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    this.keycloakService.rollBackUser("userId");

    verify(userResource, times(1)).remove();
  }

  @Test
  public void rollbackUser_Should_logError_When_rollbackFails() {
    UserResource userResource = mock(UserResource.class);
    doThrow(new RuntimeException()).when(userResource).remove();
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    this.keycloakService.rollBackUser("userId");

    verify(logger).error(anyString(), anyString());
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
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    boolean hasAuthority =
        this.keycloakService.userHasAuthority("user", AuthorityValue.USER_DEFAULT);

    assertThat(hasAuthority, is(true));
  }

  @Test
  public void userHasAuthority_Should_returnThrowKeycloakException_When_userHasNoRoles() {
    assertThrows(
        KeycloakException.class,
        () -> {
          UserResource userResource = mock(UserResource.class);
          UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
          when(keycloakClient.getUsersResource()).thenReturn(usersResource);

          this.keycloakService.userHasAuthority("user", "authority");
        });
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
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    boolean hasAuthority = this.keycloakService.userHasAuthority("user", AuthorityValue.USER_ADMIN);

    assertThat(hasAuthority, is(false));
  }

  @Test
  public void closeSession_Should_deleteSession() {
    RealmResource realmResource = mock(RealmResource.class);
    when(keycloakClient.getRealmResource()).thenReturn(realmResource);

    this.keycloakService.closeSession("sessionId");

    verify(realmResource, times(1)).deleteSession(anyString());
  }

  @Test
  public void deactivateUser_Should_deactivateUser() {
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    when(usersResource.get(any())).thenReturn(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    this.keycloakService.deactivateUser("userId");

    verify(userRepresentation, times(1)).setEnabled(false);
    verify(userResource, times(1)).update(userRepresentation);
  }

  @Test
  public void changeEmailAddress_Should_callServicesCorrectly_When_emailIsChangedAndAvailable() {
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);

    this.keycloakService.updateEmail("userId", "anotherEmail");

    verify(userRepresentation, times(1)).setEmail("anotherEmail");
    verify(userResource, times(1)).update(any());
  }

  @Test
  public void changeLanguage_ShouldNotChangeLanguageIfLanguageExistInKeycloak() {
    // given
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    HashMap<String, List<String>> attributeMap = Maps.newHashMap();
    attributeMap.put("locale", Lists.newArrayList("de"));
    when(userRepresentation.getAttributes()).thenReturn(attributeMap);

    // when
    this.keycloakService.changeLanguage("userId", "de");

    // then
    verify(userResource, Mockito.never()).update(userRepresentation);
  }

  private UsersResource givenUsersResourceWithAnyUserId(UserResource userResource) {
    UsersResource usersResource = mock(UsersResource.class);
    when(usersResource.get(any())).thenReturn(userResource);
    return usersResource;
  }

  @Test
  public void changeLanguage_ShouldChangeLanguageIfLanguageDoesNotExistInKeycloak() {
    // given
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    HashMap<String, List<String>> attributeMap = Maps.newHashMap();
    attributeMap.put("locale", Lists.newArrayList("en"));
    when(userRepresentation.getAttributes()).thenReturn(attributeMap);

    // when
    this.keycloakService.changeLanguage("userId", "de");

    // then
    verify(userResource).update(userRepresentation);
  }

  private UserResource givenUserResourceWithRepresentation(UserRepresentation userRepresentation) {
    UserResource userResource = mock(UserResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    return userResource;
  }

  private UserRepresentation givenUserRepresentation(String email) {
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    when(userRepresentation.getEmail()).thenReturn(email);
    return userRepresentation;
  }

  @Test
  public void changeLanguage_ShouldChangeLanguageIfLocaleAttributeDoesNotExistInKeycloak() {
    // given
    UserRepresentation userRepresentation = givenUserRepresentation("email");
    UserResource userResource = givenUserResourceWithRepresentation(userRepresentation);
    UsersResource usersResource = givenUsersResourceWithAnyUserId(userResource);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    HashMap<String, List<String>> attributeMap = Maps.newHashMap();
    when(userRepresentation.getAttributes()).thenReturn(attributeMap);

    // when
    this.keycloakService.changeLanguage("userId", "de");

    // then
    verify(userResource).update(userRepresentation);
  }

  @Test
  public void getById_Should_getUserById() {

    // given
    UserRepresentation userRepresentation = mock(UserRepresentation.class);
    UserResource userResource = mock(UserResource.class);
    UsersResource usersResource = mock(UsersResource.class);
    when(userResource.toRepresentation()).thenReturn(userRepresentation);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    when(usersResource.get("userId")).thenReturn(userResource);

    // when
    UserRepresentation userId = this.keycloakService.getById("userId");

    // then
    verify(keycloakClient, times(1)).getUsersResource();
    assertThat(userId, equalTo(userRepresentation));
  }

  @Test
  public void getById_Should_ThrowKeycloakExceptionIfUserNotFound() {

    // given
    UsersResource usersResource = mock(UsersResource.class);
    when(keycloakClient.getUsersResource()).thenReturn(usersResource);
    when(usersResource.get("userId")).thenReturn(null);

    // when, then
    assertThrows(KeycloakException.class, () -> this.keycloakService.getById("userId"));
  }

  private String givenADuplicatedEmailErrorMessage() {
    var emailError = RandomStringUtils.random(32);
    when(identityClientConfig.getErrorMessageDuplicatedEmail()).thenReturn(emailError);

    return emailError;
  }

  private String givenADuplicatedUserErrorMessage() {
    var userError = RandomStringUtils.random(32);
    when(identityClientConfig.getErrorMessageDuplicatedUsername()).thenReturn(userError);

    return userError;
  }
}
