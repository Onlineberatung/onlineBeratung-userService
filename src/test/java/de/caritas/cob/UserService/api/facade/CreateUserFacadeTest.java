package de.caritas.cob.UserService.api.facade;

import static de.caritas.cob.UserService.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT;
import static de.caritas.cob.UserService.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID;
import static de.caritas.cob.UserService.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_DTO_KREUZBUND;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_DTO_U25;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_KREUZBUND;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE;
import static de.caritas.cob.UserService.testHelper.TestConstants.ERROR;
import static de.caritas.cob.UserService.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_BAD_REQUEST;
import static de.caritas.cob.UserService.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_OK;
import static de.caritas.cob.UserService.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_OK_NO_TOKEN;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_AGENCY;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_KREUZBUND;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_SUCHT_WITHOUT_EMAIL;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_NO_DATA;
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
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.UserService.api.exception.keycloak.KeycloakException;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.repository.user.UserRepository;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.SessionDataService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.UserAgencyService;
import de.caritas.cob.UserService.api.service.UserService;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;

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

  @Test
  public void createUserAndInitializeAccount_Should_ReturnConflict_When_UsernameIsAlreadyExistingInPlainText()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(false);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.CONFLICT));
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_AgencyServiceHelperFailsToGetAgency()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenThrow(new AgencyServiceHelperException(new Exception()));

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_AgencyServiceHelperReturnsNull()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong())).thenReturn(null);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowBadRequestException_When_ProvidedConsultingTypeDoesNotMatchAgency()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong())).thenReturn(AGENCY_DTO_U25);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void createUserAndInitializeAccount_Should_ReturnConflict_When_EncodedUsernameIsAlreadyExisting()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.CONFLICT));
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_KeycloakHelperCreateUserReturnsKeycloakException()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_KeycloakHelperCreateUserReturnsException()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any())).thenThrow(new Exception());

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_WhenKeycloakHelperUpdateUserRoleReturnsException()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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
  public void createUserAndInitializeAccount_Should_ThrowServiceException_When_CreateKeycloakAccountReturnsNoUserId()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
    when(keycloakAdminClientHelper.createKeycloakUser(Mockito.any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID);

    try {
      createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
    verify(keycloakAdminClientHelper, times(0)).rollBackUser(Mockito.anyString());
  }

  @Test
  public void createUserAndInitializeAccount_Should_SaveSession_When_ConsultingTypeIsNotKreuzbund()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_SUCHT);
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
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsKreuzbundAndRocketChatLoginFails()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_KREUZBUND);
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
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsKreuzbundAndRocketChatLoginReturnsNoToken()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_KREUZBUND);
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

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_KREUZBUND);
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
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsKreuzbundAndRcUserIdWasNotUpdatedInDb()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_KREUZBUND);
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
  public void createUserAndInitializeAccount_Should_ThrowServiceExceptionAndRollbackUserAccount_When_ConsultingTypeIsKreuzbundAndSavingUserAgencyRelationFails()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_KREUZBUND);
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
  public void createUserAndInitializeAccount_Should_SaveUserAgencyRelationForChat_When_ConsultingTypeIsKreuzbund()
      throws Exception {

    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(Mockito.anyLong()))
        .thenReturn(AGENCY_DTO_KREUZBUND);
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
