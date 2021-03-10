package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_OK;
import static de.caritas.cob.userservice.testHelper.TestConstants.LOGIN_RESPONSE_ENTITY_OK_NO_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_WITH_RC_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.UserService;
import java.util.Collections;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserChatRelationFacadeTest {

  @InjectMocks
  private CreateUserChatRelationFacade createUserChatRelationFacade;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private UserHelper userHelper;
  @Mock
  private UserService userService;
  @Mock
  private UserAgencyService userAgencyService;
  @Mock
  private RollbackFacade rollbackFacade;

  @Test
  public void initializeUserChatAgencyRelation_Should_CreateUserChatAgencyRelation_ForNewUserAccountCreation()
      throws RocketChatLoginException, SaveUserException {
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    user.setRcUserId(null);
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);

    when(rocketChatService.loginUserFirstTime(any(), any())).thenReturn(LOGIN_RESPONSE_ENTITY_OK);
    when(userService.saveUser(any())).thenReturn(USER_WITH_RC_ID);

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, null);

    verify(userAgencyService, times(1)).saveUserAgency(any());
  }

  @Test
  public void initializeUserChatAgencyRelation_Should_CreateUserChatAgencyRelation_ForNewConsultingTypeRegistrations() {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    User user = easyRandom.nextObject(User.class);
    RocketChatCredentials rocketChatCredentials = easyRandom
        .nextObject(RocketChatCredentials.class);

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, rocketChatCredentials);

    verify(userAgencyService, times(1)).saveUserAgency(any());
  }

  @Test
  public void initializeUserChatAgencyRelation_Should_LoginUserInRocketChatAndUpdateRocketChatUserIdInDatabase_IfNotAlreadySet_ForNewUserAccountCreation()
      throws RocketChatLoginException, SaveUserException {
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    user.setRcUserId(null);
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);

    when(rocketChatService.loginUserFirstTime(any(), any())).thenReturn(LOGIN_RESPONSE_ENTITY_OK);
    when(userService.saveUser(any())).thenReturn(USER_WITH_RC_ID);
    when(userHelper.encodeUsername(userDTO.getUsername())).thenReturn(userDTO.getUsername());

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, null);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userService).saveUser(captor.capture());
    verify(rocketChatService, times(1))
        .loginUserFirstTime(userDTO.getUsername(), userDTO.getPassword());
    assertEquals(LOGIN_RESPONSE_ENTITY_OK.getBody().getData().getUserId(),
        captor.getValue().getRcUserId());
  }

  @Test
  public void initializeUserChatAgencyRelation_Should_UpdateRocketChatUserIdInDatabase_IfNotAlreadySet_ForNewConsultingTypeRegistrations()
      throws SaveUserException {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    User user = easyRandom.nextObject(User.class);
    user.setRcUserId(null);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    RocketChatCredentials rocketChatCredentials = easyRandom
        .nextObject(RocketChatCredentials.class);

    when(userService.saveUser(any())).thenReturn(USER_WITH_RC_ID);

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, rocketChatCredentials);

    verify(userService).saveUser(captor.capture());
    assertEquals(rocketChatCredentials.getRocketChatUserId(),
        captor.getValue().getRcUserId());
  }

  @Test(expected = InternalServerErrorException.class)
  public void initializeUserChatAgencyRelation_Should_ThrowInternalServerErrorExceptionAndRollBackUserAccount_IfLoginToRocketChatFails_OnUpdatingRocketChatCredentialsInDb()
      throws RocketChatLoginException {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    User user = easyRandom.nextObject(User.class);
    user.setRcUserId(null);
    ArgumentCaptor<RollbackUserAccountInformation> captor = ArgumentCaptor
        .forClass(RollbackUserAccountInformation.class);

    when(rocketChatService.loginUserFirstTime(any(), any()))
        .thenThrow(new RocketChatLoginException(MESSAGE));

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, null);

    verify(rollbackFacade).rollBackUserAccount(captor.capture());
    assertEquals(user, captor.getValue().getUser());
    assertEquals(true, captor.getValue().isRollBackUserAccount());
  }

  @Test(expected = InternalServerErrorException.class)
  public void initializeUserChatAgencyRelation_Should_ThrowInternalServerErrorExceptionAndRollBackUserAccount_IfLoginToRocketChatReturnsInvalidResponse_OnUpdatingRocketChatCredentialsInDb()
      throws RocketChatLoginException {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    User user = easyRandom.nextObject(User.class);
    user.setRcUserId(null);
    ArgumentCaptor<RollbackUserAccountInformation> captor = ArgumentCaptor
        .forClass(RollbackUserAccountInformation.class);

    when(rocketChatService.loginUserFirstTime(any(), any()))
        .thenReturn(LOGIN_RESPONSE_ENTITY_OK_NO_TOKEN);

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, null);

    verify(rollbackFacade).rollBackUserAccount(captor.capture());
    assertEquals(user, captor.getValue().getUser());
    assertEquals(true, captor.getValue().isRollBackUserAccount());
  }

  @Test(expected = InternalServerErrorException.class)
  public void initializeUserChatAgencyRelation_Should_ThrowInternalServerErrorExceptionAndRollBackUserAccount_IfUpdatingRocketChatUserIdInDbFails()
      throws SaveUserException {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    User user = easyRandom.nextObject(User.class);
    user.setRcUserId(null);
    RocketChatCredentials rocketChatCredentials = easyRandom
        .nextObject(RocketChatCredentials.class);
    ArgumentCaptor<RollbackUserAccountInformation> captor = ArgumentCaptor
        .forClass(RollbackUserAccountInformation.class);

    when(userService.saveUser(any())).thenThrow(new SaveUserException(MESSAGE));

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, rocketChatCredentials);

    verify(rollbackFacade).rollBackUserAccount(captor.capture());
    assertEquals(user, captor.getValue().getUser());
    assertEquals(true, captor.getValue().isRollBackUserAccount());
  }

  @Test(expected = InternalServerErrorException.class)
  public void initializeUserChatAgencyRelation_Should_ThrowInternalServerErrorExceptionAndRollBackUserAccount_IfCreateUserChatAgencyRelationFails() {
    ArgumentCaptor<RollbackUserAccountInformation> captor = ArgumentCaptor
        .forClass(RollbackUserAccountInformation.class);
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    User user = easyRandom.nextObject(User.class);
    RocketChatCredentials rocketChatCredentials = easyRandom
        .nextObject(RocketChatCredentials.class);

    when(userAgencyService.saveUserAgency(any()))
        .thenThrow(new InternalServerErrorException(MESSAGE));

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, rocketChatCredentials);

    verify(rollbackFacade).rollBackUserAccount(captor.capture());
    assertEquals(user, captor.getValue().getUser());
    assertEquals(true, captor.getValue().isRollBackUserAccount());
  }

  @Test(expected = BadRequestException.class)
  public void initializeUserChatAgencyRelation_Should_ThrowBadRequestException_WhenAlreadyRegisteredToConsultingType() {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setAgencyId(AGENCY_ID);
    User user = easyRandom.nextObject(User.class);
    RocketChatCredentials rocketChatCredentials = easyRandom
        .nextObject(RocketChatCredentials.class);
    UserAgency userAgency = new UserAgency(user, AGENCY_ID);
    List<UserAgency> userAgencies = Collections.singletonList(userAgency);

    when(userAgencyService.getUserAgenciesByUser(any())).thenReturn(userAgencies);

    createUserChatRelationFacade
        .initializeUserChatAgencyRelation(userDTO, user, rocketChatCredentials);

    verify(userAgencyService, times(1)).saveUserAgency(any());
  }
}
