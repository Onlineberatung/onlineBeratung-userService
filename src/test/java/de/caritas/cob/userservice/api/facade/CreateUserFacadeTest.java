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

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
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

  @Test(expected = BadRequestException.class)
  public void createUserAndInitializeAccount_Should_ThrowBadRequest_When_ProvidedConsultingTypeDoesNotMatchAgency() {

    when(userHelper.isUsernameAvailable(Mockito.anyString())).thenReturn(true);
    when(agencyHelper.doesConsultingTypeMatchToAgency(USER_DTO_SUCHT.getAgencyId(),
        CONSULTING_TYPE_SUCHT)).thenReturn(false);

    createUserFacade.createUserAndInitializeAccount(USER_DTO_SUCHT);
  }

}
