package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_LANGUAGE_FORMAL;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import javax.ws.rs.InternalServerErrorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit4.SpringRunner;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;

@RunWith(SpringRunner.class)
public class UserServiceTest {

  @InjectMocks
  private UserService userService;
  @Mock
  private LogService logService;
  @Mock
  private UserRepository userRepository;

  /**
   * Method: createUser
   * 
   */

  @Test
  public void createUser_Should_ReturnServiceException_When_RepositoryFails() throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(userRepository.save(Mockito.any())).thenThrow(ex);

    try {
      userService.createUser(USER_ID, USERNAME, EMAIL, IS_LANGUAGE_FORMAL);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceEx) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void createUser_Should_ReturnUser_When_RepositoryCallIsSuccessfull() {

    when(userRepository.save(Mockito.any())).thenReturn(USER);

    User result = userService.createUser(USER_ID, USERNAME, EMAIL, IS_LANGUAGE_FORMAL);

    assertNotNull(result);
    assertEquals(USER, result);
  }

  /**
   * Method: getUser(String userId)
   * 
   */

  @Test
  public void getUser_Should_ReturnServiceException_When_RepositoryFails() throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(userRepository.findByUserId(Mockito.anyString())).thenThrow(ex);

    try {
      userService.getUser(USER_ID);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceEx) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void getUser_Should_ReturnUser_WhenRepositoryCallIsSuccessfull() {

    when(userRepository.findByUserId(Mockito.anyString())).thenReturn(Optional.of(USER));

    Optional<User> result = userService.getUser(USER_ID);

    assertTrue(result.isPresent());
    assertEquals(USER, result.get());
  }

  /**
   * Method: getUserViaAuthenticatedUser
   * 
   */

  @Test
  public void getUserViaAuthenticatedUser_Should_InternalServerErrorException_When_UserWasNotFound()
      throws Exception {

    try {
      userService.getUserViaAuthenticatedUser(AUTHENTICATED_USER);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException intServerErrExc) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getUserViaAuthenticatedUser_Should_UserOptionalObject() {

    when(userRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(USER));

    Optional<User> result = userService.getUserViaAuthenticatedUser(AUTHENTICATED_USER);

    assertTrue(result.isPresent());
    assertEquals(USER, result.get());
  }

  /**
   * Method: saveUser
   * 
   */

  @Test
  public void saveUser_Should_ThrowSaveUserException_When_DatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(userRepository.save(Mockito.any())).thenThrow(ex);

    try {
      userService.saveUser(USER);
      fail("Expected exception: SaveUserException");
    } catch (SaveUserException saveUserException) {
      assertTrue("Excepted SaveUserException thrown", true);
    }

  }

  @Test
  public void saveUser_Should_UserObject() {

    when(userRepository.save(Mockito.any())).thenReturn(USER);

    User result = userService.saveUser(USER);

    assertNotNull(result);
    assertEquals(USER, result);
  }

  /**
   * Method: deleteUser
   * 
   */

  @Test
  public void deleteUser_Should_ThrowSaveUserException_When_DatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    doThrow(ex).when(userRepository).delete(Mockito.any());

    try {
      userService.deleteUser(USER);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

  }

  @Test
  public void deleteUser_Should_CallDeleteUserRepository() {

    userService.deleteUser(USER);

    verify(userRepository, times(1)).delete(Mockito.any());
  }

}
