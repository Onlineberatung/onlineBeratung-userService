package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT;
import static de.caritas.cob.userservice.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID;
import static de.caritas.cob.userservice.testHelper.KeycloakConstants.KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_SUCHT_WITHOUT_EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.AgencyHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.service.UserService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserFacadeTest {

  @InjectMocks
  private CreateUserFacade createUserFacade;
  @Mock
  private KeycloakAdminClientHelper keycloakAdminClientHelper;
  @Mock
  private UserService userService;
  @Mock
  private RollbackFacade rollbackFacade;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private UserHelper userHelper;
  @Mock
  private AgencyHelper agencyHelper;
  @Mock
  private CreateNewConsultingTypeFacade createNewConsultingTypeFacade;

  @Test
  public void createUserAndInitializeAccount_Should_ReturnConflict_When_UsernameIsAlreadyExisting() {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(false);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.CONFLICT));
  }

  @Test(expected = BadRequestException.class)
  public void createUserAndInitializeAccount_Should_ThrowBadRequest_When_ProvidedConsultingTypeDoesNotMatchAgency() {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(false);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserAndInitializeAccount_Should_ThrowInternalServerErrorException_When_KeycloakHelperCreateUserThrowsException()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenThrow(new KeycloakException(ERROR));

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
  }

  @Test
  public void createUserAndInitializeAccount_Should_ReturnConflict_When_KeycloakHelperCreateUserReturnsConflict()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT);

    assertThat(createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT).getStatus(),
        is(HttpStatus.CONFLICT));
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserAndInitializeAccount_Should_ThrowInternalServerErrorException_When_CreateKeycloakUserReturnsNoUserId()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserAndInitializeAccount_Should_ThrowInternalServerErrorExceptionAndRollbackUserAccount_When_KeycloakHelperUpdateUserRoleReturnsException()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doThrow(new KeycloakException(ERROR)).when(keycloakAdminClientHelper).updateUserRole(USER_ID);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);

    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserAndInitializeAccount_Should_ThrowInternalServerErrorExceptionAndRollbackUserAccount_When_UpdateKeycloakPasswordFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doThrow(new KeycloakException(ERROR)).when(keycloakAdminClientHelper)
        .updatePassword(Mockito.anyString(), Mockito.anyString());

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);

    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserAndInitializeAccount_Should_ThrowInternalServerErrorExceptionAndRollbackUserAccount_When_UpdateKeycloakDummyEmailFails()
      throws Exception {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setConsultingType(String.valueOf(CONSULTING_TYPE_SUCHT.getValue()));
    userDTO.setEmail(null);

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(any(), any(ConsultingType.class)))
        .thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    doThrow(new KeycloakException(ERROR)).when(keycloakAdminClientHelper)
        .updateDummyEmail(Mockito.anyString(), any());

    createUserFacade.createUserAndInitializeAccount(userDTO);

    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test
  public void createUserAndInitializeAccount_Should_UpdateDummyEmail_When_NoEmailProvided()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT_WITHOUT_EMAIL);

    verify(keycloakAdminClientHelper, times(1)).updateDummyEmail(Mockito.anyString(),
        any());
    verify(rollbackFacade, times(0)).rollBackUserAccount(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserAndInitializeAccount_Should_ThrowInternalServerErrorExceptionAndRollbackUserAccount_When_CreateAccountInMariaDBFails()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(true);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(any(), any(), any(), anyBoolean()))
        .thenThrow(new InternalServerErrorException(ERROR));

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT_WITHOUT_EMAIL);

    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test
  public void createUserAndInitializeAccount_Should_LogOutFromRocketChat_When_ConsultingTypeIsKreuzbundAndRocketChatLoginSucceeded()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(any(), any(), any(), anyBoolean())).thenReturn(USER);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_KREUZBUND);

    verify(rollbackFacade, times(0)).rollBackUserAccount(any());
  }

  @Test
  public void createUserAndInitializeAccount_Should_CallInitializeNewConsultingTypeAndReturnCreated_When_EverythingSucceeded()
      throws Exception {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_KREUZBUND.getAgencyId(),
        CONSULTING_TYPE_KREUZBUND)).thenReturn(true);
    when(consultingTypeManager.getConsultantTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);
    when(keycloakAdminClientHelper.createKeycloakUser(any()))
        .thenReturn(KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID);
    doNothing().when(keycloakAdminClientHelper).updateUserRole(Mockito.anyString());
    doNothing().when(keycloakAdminClientHelper).updatePassword(Mockito.anyString(),
        Mockito.anyString());
    when(userService.createUser(any(), any(), any(), anyBoolean())).thenReturn(USER);

    KeycloakCreateUserResponseDTO responseDTO = createUserFacade
        .createUserAndInitializeAccount(USER_DTO_KREUZBUND);

    verify(createNewConsultingTypeFacade, times(1))
        .initializeNewConsultingType(any(), any(), any(ConsultingTypeSettings.class));
    verify(rollbackFacade, times(0)).rollBackUserAccount(any());
    assertEquals(HttpStatus.CREATED, responseDTO.getStatus());
  }
}