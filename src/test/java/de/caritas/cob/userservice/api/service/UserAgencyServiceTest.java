package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_AGENCY_LIST;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class UserAgencyServiceTest {

  @InjectMocks private UserAgencyService userAgencyService;
  @Mock private LogService logService;
  @Mock private UserAgencyRepository userAgencyRepository;

  /** Method: saveUserAgency */
  @Test
  public void saveUserAgency_Should_ThrowInternalServerErrorException_When_DatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(userAgencyRepository.save(Mockito.any())).thenThrow(ex);

    try {
      userAgencyService.saveUserAgency(USER_AGENCY);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  public void saveUser_Should_UserObject() {

    when(userAgencyRepository.save(Mockito.any())).thenReturn(USER_AGENCY);

    UserAgency result = userAgencyService.saveUserAgency(USER_AGENCY);

    assertNotNull(result);
    assertEquals(USER_AGENCY, result);
  }

  /** Method: getUserAgenciesByUser */
  @Test
  public void getUserAgenciesByUser_Should_ReturnInternalServerErrorException_When_RepositoryFails()
      throws Exception {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(userAgencyRepository.findByUser(Mockito.any())).thenThrow(ex);

    try {
      userAgencyService.getUserAgenciesByUser(USER);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceEx) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  public void
      getUserAgenciesByUser_Should_ReturnListOfUserAgencyObjects_When_RepositoryCallIsSuccessfull() {

    when(userAgencyRepository.findByUser(Mockito.any())).thenReturn(USER_AGENCY_LIST);

    List<UserAgency> result = userAgencyService.getUserAgenciesByUser(USER);

    assertThat(result, everyItem(instanceOf(UserAgency.class)));
  }

  /** Method: deleteUser */
  @Test
  public void deleteUserAgency_Should_ThrowInternalServerErrorException_When_DatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    doThrow(ex).when(userAgencyRepository).delete(USER_AGENCY);

    try {
      userAgencyService.deleteUserAgency(USER_AGENCY);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceEx) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  public void deleteUserAgency_Should_ThrowIllegalArgumentException_When_UserAgencyIsNull() {

    IllegalArgumentException ex = new IllegalArgumentException();
    doThrow(ex).when(userAgencyRepository).delete(null);

    try {
      userAgencyService.deleteUserAgency(null);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceEx) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  public void deleteUserAgency_Should_CallDeleteUserAgencyRepository() {

    userAgencyService.deleteUserAgency(USER_AGENCY);

    verify(userAgencyRepository, times(1)).delete(USER_AGENCY);
  }
}
