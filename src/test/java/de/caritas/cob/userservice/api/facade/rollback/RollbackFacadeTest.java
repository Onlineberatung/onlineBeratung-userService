package de.caritas.cob.userservice.api.facade.rollback;


import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.UserService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RollbackFacadeTest {

  @InjectMocks
  private RollbackFacade rollbackFacade;
  @Mock
  private KeycloakAdminClientHelper keycloakAdminClientHelper;
  @Mock
  private UserAgencyService userAgencyService;
  @Mock
  private SessionService sessionService;
  @Mock
  private UserService userService;
  @Mock
  private MonitoringService monitoringService;

  @Test
  public void rollBackUserAccount_Should_DeleteSessionAndMonitoring_When_SessionIsGiven() {
    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    RollbackUserAccountInformation rbUserInfo = RollbackUserAccountInformation
        .builder()
        .session(session)
        .build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(sessionService, times(1)).deleteSession(session);
    verify(monitoringService, times(1)).rollbackInitializeMonitoring(session);
  }

  @Test
  public void rollBackUserAccount_Should_DeleteUserAgency_When_UserAgencyIsGiven() {
    EasyRandom easyRandom = new EasyRandom();
    UserAgency userAgency = easyRandom.nextObject(UserAgency.class);
    RollbackUserAccountInformation rbUserInfo = RollbackUserAccountInformation
        .builder()
        .userAgency(userAgency)
        .build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(userAgencyService, times(1)).deleteUserAgency(userAgency);
  }

  @Test
  public void rollBackUserAccount_Should_DeleteKeycloakAccount_When_UserIdIsGivenAndRollbackUserAccountFlagIsTrue() {
    RollbackUserAccountInformation rbUserInfo = RollbackUserAccountInformation
        .builder()
        .userId(USER_ID)
        .rollBackUserAccount(true)
        .build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(keycloakAdminClientHelper, times(1)).rollBackUser(USER_ID);
  }

  @Test
  public void rollBackUserAccount_Should_DeleteMariaDbAccount_When_UserIsGivenAndRollbackUserAccountFlagIsTrue() {
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    user.setUserId(USER_ID);
    RollbackUserAccountInformation rbUserInfo = RollbackUserAccountInformation
        .builder()
        .user(user)
        .rollBackUserAccount(true)
        .build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(userService, times(1)).deleteUser(user);
  }
}
