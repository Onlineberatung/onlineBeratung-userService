package de.caritas.cob.userservice.api.facade.rollback;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUserAccountService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RollbackFacadeTest {

  @InjectMocks private RollbackFacade rollbackFacade;
  @Mock private KeycloakService keycloakService;
  @Mock private UserAgencyService userAgencyService;
  @Mock private SessionService sessionService;
  @Mock private UserService userService;

  @Mock DeleteUserAccountService deleteUserAccountService;

  @Test
  public void rollbackConsultantAccount_Should_Call_DeleteUserAccountService() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    // when
    rollbackFacade.rollbackConsultantAccount(consultant);
    // then
    verify(deleteUserAccountService, times(1)).performConsultantDeletion(consultant);
  }

  @Test
  public void rollBackUserAccount_Should_DeleteSessionAndMonitoring_When_SessionIsGiven() {
    EasyRandom easyRandom = new EasyRandom();
    Session session = easyRandom.nextObject(Session.class);
    RollbackUserAccountInformation rbUserInfo =
        RollbackUserAccountInformation.builder().session(session).build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(sessionService, times(1)).deleteSession(session);
  }

  @Test
  public void rollBackUserAccount_Should_DeleteUserAgency_When_UserAgencyIsGiven() {
    EasyRandom easyRandom = new EasyRandom();
    UserAgency userAgency = easyRandom.nextObject(UserAgency.class);
    RollbackUserAccountInformation rbUserInfo =
        RollbackUserAccountInformation.builder().userAgency(userAgency).build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(userAgencyService, times(1)).deleteUserAgency(userAgency);
  }

  @Test
  public void
      rollBackUserAccount_Should_DeleteKeycloakAccount_When_UserIdIsGivenAndRollbackUserAccountFlagIsTrue() {
    RollbackUserAccountInformation rbUserInfo =
        RollbackUserAccountInformation.builder().userId(USER_ID).rollBackUserAccount(true).build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(keycloakService, times(1)).rollBackUser(USER_ID);
  }

  @Test
  public void
      rollBackUserAccount_Should_DeleteMariaDbAccount_When_UserIsGivenAndRollbackUserAccountFlagIsTrue() {
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    user.setUserId(USER_ID);
    RollbackUserAccountInformation rbUserInfo =
        RollbackUserAccountInformation.builder().user(user).rollBackUserAccount(true).build();

    rollbackFacade.rollBackUserAccount(rbUserInfo);

    verify(userService, times(1)).deleteUser(user);
  }
}
