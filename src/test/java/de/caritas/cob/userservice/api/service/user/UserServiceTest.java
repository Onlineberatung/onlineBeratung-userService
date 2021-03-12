package de.caritas.cob.userservice.api.service.user;

import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_LANGUAGE_FORMAL;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_NO_RC_USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserServiceTest {

  @InjectMocks
  private UserService userService;
  @Mock
  private UserRepository userRepository;

  @Test
  public void createUser_Should_ReturnUser_When_RepositoryCallIsSuccessful() {

    when(userRepository.save(Mockito.any())).thenReturn(USER);

    User result = userService.createUser(USER_ID, USERNAME, EMAIL, IS_LANGUAGE_FORMAL);

    assertNotNull(result);
    assertEquals(USER, result);
  }

  @Test
  public void getUser_Should_ReturnUser_WhenRepositoryCallIsSuccessful() {

    when(userRepository.findByUserIdAndDeleteDateIsNull(Mockito.anyString()))
        .thenReturn(Optional.of(USER));

    Optional<User> result = userService.getUser(USER_ID);

    assertTrue(result.isPresent());
    assertEquals(USER, result.get());
  }

  /**
   * Method: getUserViaAuthenticatedUser
   */

  @Test
  public void getUserViaAuthenticatedUser_Should_InternalServerErrorException_When_UserWasNotFound() {

    try {
      userService.getUserViaAuthenticatedUser(AUTHENTICATED_USER);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException intServerErrExc) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getUserViaAuthenticatedUser_Should_UserOptionalObject() {

    when(userRepository.findByUserIdAndDeleteDateIsNull(Mockito.any()))
        .thenReturn(Optional.of(USER));

    Optional<User> result = userService.getUserViaAuthenticatedUser(AUTHENTICATED_USER);

    assertTrue(result.isPresent());
    assertEquals(USER, result.get());
  }

  @Test
  public void saveUser_Should_UserObject() {

    when(userRepository.save(Mockito.any())).thenReturn(USER);

    User result = userService.saveUser(USER);

    assertNotNull(result);
    assertEquals(USER, result);
  }

  @Test
  public void deleteUser_Should_CallDeleteUserRepository() {

    userService.deleteUser(USER);

    verify(userRepository, times(1)).delete(Mockito.any());
  }

  @Test
  public void findUserByRcUserId_Should_ReturnUser_WhenRepositoryCallIsSuccessful() {

    when(userRepository.findByRcUserIdAndDeleteDateIsNull(Mockito.anyString()))
        .thenReturn(Optional.of(USER));

    Optional<User> result = userService.findUserByRcUserId(USER_ID);

    assertTrue(result.isPresent());
    assertEquals(USER, result.get());
  }

  @Test
  public void updateRocketChatIdInDatabase_Should_UpdateUserObjectAndSaveToDb() {
    userService.updateRocketChatIdInDatabase(USER_NO_RC_USER_ID, RC_USER_ID);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(captor.capture());
    assertEquals(RC_USER_ID, captor.getValue().getRcUserId());
  }

  @Test
  public void updateRocketChatIdInDatabase_ShouldNot_UpdateUserObject_When_UserNotGiven() {
    userService.updateRocketChatIdInDatabase(null, RC_USER_ID);

    verifyNoInteractions(userRepository);
  }

  @Test
  public void updateRocketChatIdInDatabase_ShouldNot_UpdateUserObject_When_UserIdNotGiven() {
    userService.updateRocketChatIdInDatabase(USER_NO_RC_USER_ID, "");

    verifyNoInteractions(userRepository);
  }
}
