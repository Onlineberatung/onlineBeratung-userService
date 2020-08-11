package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT;
import static de.caritas.cob.userservice.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID;
import static de.caritas.cob.userservice.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_BAD_REQUEST;
import static de.caritas.cob.userservice.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_OK;
import static de.caritas.cob.userservice.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_OK_NO_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_SUCHT_WITHOUT_EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_NO_DATA;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.AgencyHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.UserService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserFacadeTest {

  @InjectMocks
  CreateUserFacade createUserFacade;
  @Mock
  LogService logService;
  @Mock
  KeycloakAdminClientHelper keycloakAdminClientHelper;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  AgencyServiceHelper agencyServiceHelper;
  @Mock
  UserRepository userRepository;
  @Mock
  UserService userService;
  @Mock
  UserHelper userHelper;
  @Mock
  SessionDataService sessionDataService;
  @Mock
  SessionService sessionService;
  @Mock
  RocketChatService rocketChatService;
  @Mock
  UserAgencyService userAgencyService;
  @Mock
  AgencyHelper agencyHelper;

  @Test
  public void createUserAndInitializeAccount_Should_ReturnConflict_When_UsernameIsAlreadyExisting() {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(false);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.CONFLICT));
  }

  @Test
  public void createUserAndInitializeAccount_Should_ReturnBadRequest_When_ProvidedConsultingTypeDoesNotMatchAgency() {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(false);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.BAD_REQUEST));
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_KeycloakHelperCreateUserThrowsException()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenThrow(new KeycloakException(ERROR));

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void createUserAndInitializeAccount_Should_ReturnConflict_When_KeycloakHelperCreateUserReturnsConflict()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.CONFLICT));
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_CreateKeycloakUserReturnsNoUserId()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_KeycloakHelperUpdateUserRoleReturnsException()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doThrow(new KeycloakException(ERROR)).when(keycloakAdminClientHelper).updateUserRole(USER_ID);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_UpdateKeycloakPasswordFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doThrow(new KeycloakException(ERROR)).when(keycloakAdminClientHelper)
        .updatePassword(Mockito.anyString(), Mockito.anyString());

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_UpdateKeycloakDummyEmailFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    doThrow(new KeycloakException(ERROR)).when(keycloakAdminClientHelper)
        .updateDummyEmail(Mockito.anyString(), Mockito.any());

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT_WITHOUT_EMAIL);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_UpdateDummyEmail_When_NoEmailProvided()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT_WITHOUT_EMAIL);

    verify(keycloakAdminClientHelper, times(1)).updateDummyEmail(Mockito.anyString(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(0)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_CreateAccountInMariaDBFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenThrow(new ServiceException(ERROR));

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_SaveSession_When_ConsultingTypeIsNotWithChat()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);

    verify(sessionDataService, times(1)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(0)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_CreateUserSessionFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(sessionDataService.saveSessionDataFromRegistration(Mockito.any(), Mockito.any()))
        .thenThrow(new ServiceException(ERROR));

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsWithChatAndRocketChatLoginFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(rocketChatService.loginUserFirstTime(Mockito.any(), Mockito.any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_BAD_REQUEST);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(sessionDataService, times(0)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsWithChatAndRocketChatLoginReturnsNoToken()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(rocketChatService.loginUserFirstTime(Mockito.any(), Mockito.any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_OK_NO_TOKEN);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(sessionDataService, times(0)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_LogOutFromRocketChat_When_ConsultingTypeIsKreuzbundAndRocketChatLoginSucceeded()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(rocketChatService.loginUserFirstTime(Mockito.any(), Mockito.any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_OK);
    when(userService.saveUser(Mockito.any())).thenReturn(USER);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);

    verify(rocketChatService, times(1)).logoutUser(Mockito.any());
    verify(sessionDataService, times(0)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(0)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsWithChatAndRcUserIdWasNotUpdatedInDb()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(rocketChatService.loginUserFirstTime(Mockito.any(), Mockito.any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_OK);
    when(userService.saveUser(Mockito.any())).thenReturn(USER_NO_DATA);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(sessionDataService, times(0)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsWithChatAndSavingUserAgencyRelationFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(rocketChatService.loginUserFirstTime(Mockito.any(), Mockito.any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_OK);
    when(userService.saveUser(Mockito.any())).thenReturn(USER);
    when(userAgencyService.saveUserAgency(Mockito.any())).thenThrow(new ServiceException(ERROR));

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(sessionDataService, times(0)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(keycloakAdminClientHelper, times(1)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_SaveUserAgencyRelationForChat_When_ConsultingTypeIsWithChat()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyBoolean())).thenReturn(USER);
    when(rocketChatService.loginUserFirstTime(Mockito.any(), Mockito.any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_OK);
    when(userService.saveUser(Mockito.any())).thenReturn(USER);
    when(userAgencyService.saveUserAgency(Mockito.any())).thenReturn(USER_AGENCY);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);

    verify(sessionDataService, times(0)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
    verify(userAgencyService, times(1)).saveUserAgency(Mockito.any());
    verify(keycloakAdminClientHelper, times(0)).rollBackUser(Mockito.anyString());
  }
}
