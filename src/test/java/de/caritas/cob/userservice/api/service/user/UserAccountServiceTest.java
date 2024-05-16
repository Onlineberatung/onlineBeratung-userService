package de.caritas.cob.userservice.api.service.user;

import static de.caritas.cob.userservice.api.helper.EmailNotificationUtils.deserializeNotificationSettingsDTOOrDefaultIfNull;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.userdata.EmailNotificationMapper;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.DeleteAccountStatisticsEvent;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserAccountServiceTest {

  private static final EasyRandom EASY_RANDOM = new EasyRandom();

  @InjectMocks private UserAccountService accountProvider;
  @Mock private UserService userService;
  @Mock private ConsultantService consultantService;
  @Mock private AuthenticatedUser authenticatedUser;
  @Mock private KeycloakService keycloakService;
  @Mock private RocketChatService rocketChatService;
  @Mock private UserHelper userHelper;
  @Mock private AppointmentService appointmentService;

  @Mock private IdentityClientConfig identityClientConfig;

  @Mock private StatisticsService statisticsService;

  @Test
  public void findUserByEmail_Should_CallUserService() {
    // given
    String email = "mail@mail.de";

    // when
    accountProvider.findUserByEmail(email);

    // then
    Mockito.verify(userService).findUserByEmail(email);
  }

  @Test
  public void findConsultantByEmail_Should_CallConsultantService() {
    // given
    String email = "mail@mail.de";

    // when
    accountProvider.findConsultantByEmail(email);

    // then
    Mockito.verify(consultantService).findConsultantByEmail(email);
  }

  @Test
  public void updateUserEmail_setInitialEmailNotifications() {
    // when
    User user = new User();
    accountProvider.updateUserEmail(user, "some@email.com");

    assertThat(user.getEmail()).isNotNull();
    var notificationsSettingsDTO = deserializeNotificationSettingsDTOOrDefaultIfNull(user);
    assertThat(user.getNotificationsSettings()).isNotNull();
    assertThat(notificationsSettingsDTO.getNewChatMessageNotificationEnabled()).isTrue();
    assertThat(notificationsSettingsDTO.getAppointmentNotificationEnabled()).isTrue();
    assertThat(notificationsSettingsDTO.getReassignmentNotificationEnabled()).isTrue();
  }

  @Test
  public void retrieveValidatedUser_Should_ReturnUser_When_UserIsPresent() {
    User userMock = mock(User.class);
    when(userService.getUser(any())).thenReturn(Optional.of(userMock));

    User resultUser = this.accountProvider.retrieveValidatedUser();

    assertThat(resultUser).isEqualTo(userMock);
  }

  @Test
  public void
      retrieveValidatedUser_Should_Throw_InternalServerErrorException_When_UserIsNotPresent() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(userService.getUser(any())).thenReturn(Optional.empty());

          this.accountProvider.retrieveValidatedUser();
        });
  }

  @Test
  public void retrieveValidatedConsultant_Should_ReturnConsultant_When_ConsultantIsPresent() {
    Consultant consultantMock = mock(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

    Consultant resultUser = this.accountProvider.retrieveValidatedConsultant();

    assertThat(resultUser).isEqualTo(consultantMock);
  }

  @Test
  public void
      retrieveValidatedConsultant_Should_Throw_InternalServerErrorException_When_ConsultantIsNotPresent() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultantService.getConsultant(any())).thenReturn(Optional.empty());

          this.accountProvider.retrieveValidatedConsultant();
        });
  }

  @Test
  public void
      retrieveValidatedTeamConsultant_Should_ReturnTeamConsultant_When_TeamConsultantIsPresent() {
    Consultant teamConsultantMock = mock(Consultant.class);
    when(teamConsultantMock.isTeamConsultant()).thenReturn(true);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(teamConsultantMock));

    Consultant resultUser = this.accountProvider.retrieveValidatedTeamConsultant();

    assertThat(resultUser).isEqualTo(teamConsultantMock);
  }

  @Test
  public void
      retrieveValidatedTeamConsultant_Should_Throw_ForbiddenException_When_ConsultantIsNotATeamConsultant() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Consultant consultantMock = mock(Consultant.class);
          when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

          this.accountProvider.retrieveValidatedTeamConsultant();
        });
  }

  @Test
  public void
      changeUserAccountEmailAddress_Should_changeNonEmptyAddressInKeycloakRocketChatAndConsultantRepository_When_authenticatedUserIsConsultant() {
    Consultant consultant = EASY_RANDOM.nextObject(Consultant.class);
    when(this.authenticatedUser.getUserId()).thenReturn("consultant");
    when(this.consultantService.getConsultant("consultant")).thenReturn(Optional.of(consultant));

    this.accountProvider.changeUserAccountEmailAddress(Optional.of("newMail"));

    verify(keycloakService).changeEmailAddress("newMail");
    verify(this.rocketChatService, times(1))
        .updateUser(
            new UserUpdateRequestDTO(
                consultant.getRocketChatId(), new UserUpdateDataDTO("newMail", true)));
    consultant.setEmail("newMail");
    verify(this.consultantService, times(1)).saveConsultant(consultant);
    verifyNoMoreInteractions(this.rocketChatService);
    verify(this.userService, times(1)).getUser(any());
    verifyNoMoreInteractions(this.userService);
    verifyNoInteractions(userHelper);
  }

  @Test
  public void
      changeUserAccountEmailAddress_Should_changeNonEmptyAddressInKeycloakRocketChatAndUserRepository_When_authenticatedUserIsUser() {
    User user = EASY_RANDOM.nextObject(User.class);
    when(this.authenticatedUser.getUserId()).thenReturn("user");
    when(this.userService.getUser("user")).thenReturn(Optional.of(user));

    final String newMail = "newMail";
    this.accountProvider.changeUserAccountEmailAddress(Optional.of(newMail));

    verify(keycloakService).changeEmailAddress(newMail);
    verify(this.rocketChatService, times(1))
        .updateUser(
            new UserUpdateRequestDTO(user.getRcUserId(), new UserUpdateDataDTO(newMail, true)));
    verify(this.appointmentService, times(1)).updateAskerEmail(user.getUserId(), newMail);
    user.setEmail(newMail);
    verify(this.userService, times(1)).saveUser(user);
    verifyNoMoreInteractions(this.rocketChatService);
    verify(this.consultantService, times(1)).getConsultant(any());
    verifyNoMoreInteractions(this.consultantService);
    verifyNoInteractions(userHelper);
  }

  @Test
  public void
      changeUserAccountEmailAddress_Should_changeNonEmptyAddressInKeycloakUserRepositoryButNotInRocketChat_When_authenticatedUserIsUserWithoutRocketChatUserId() {
    User user = EASY_RANDOM.nextObject(User.class);
    user.setRcUserId(null);
    when(this.authenticatedUser.getUserId()).thenReturn("user");
    when(this.userService.getUser("user")).thenReturn(Optional.of(user));

    final String newMail = "newMail";
    this.accountProvider.changeUserAccountEmailAddress(Optional.of(newMail));

    verify(keycloakService).changeEmailAddress(newMail);
    verify(this.rocketChatService, never())
        .updateUser(
            new UserUpdateRequestDTO(user.getRcUserId(), new UserUpdateDataDTO(newMail, true)));
    verify(this.appointmentService, times(1)).updateAskerEmail(user.getUserId(), newMail);
    user.setEmail(newMail);
    verify(this.userService, times(1)).saveUser(user);
    verifyNoMoreInteractions(this.rocketChatService);
    verify(this.consultantService, times(1)).getConsultant(any());
    verifyNoMoreInteractions(this.consultantService);
    verifyNoInteractions(userHelper);
  }

  @Test
  public void
      changeUserAccountEmailAddress_Should_changeEmptyAddressInKeycloakRocketChatAndConsultantRepository_When_authenticatedUserIsConsultant() {
    var consultant = EASY_RANDOM.nextObject(Consultant.class);
    var consultantId = RandomStringUtils.randomAlphabetic(16);
    var dummyEmail = RandomStringUtils.randomAlphabetic(16);
    when(authenticatedUser.getUserId()).thenReturn(consultantId);
    when(consultantService.getConsultant(consultantId)).thenReturn(Optional.of(consultant));
    when(userHelper.getDummyEmail(consultantId)).thenReturn(dummyEmail);

    accountProvider.changeUserAccountEmailAddress(Optional.empty());

    verify(keycloakService).deleteEmailAddress();
    verify(keycloakService, never()).changeEmailAddress(anyString());
    verify(rocketChatService)
        .updateUser(
            new UserUpdateRequestDTO(
                consultant.getRocketChatId(), new UserUpdateDataDTO(dummyEmail, true)));
    consultant.setEmail(dummyEmail);
    verify(consultantService).saveConsultant(consultant);
    verifyNoMoreInteractions(rocketChatService);
    verify(userService).getUser(any());
    verifyNoMoreInteractions(userService);
  }

  @Test
  public void
      changeUserAccountEmailAddress_Should_changeEmptyAddressInKeycloakRocketChatAndUserRepository_When_authenticatedUserIsUser() {
    var user = EASY_RANDOM.nextObject(User.class);
    var userId = RandomStringUtils.randomAlphabetic(16);
    var dummyEmail = RandomStringUtils.randomAlphabetic(16);
    when(authenticatedUser.getUserId()).thenReturn(userId);
    user.setEmail(dummyEmail);
    when(userService.getUser(userId)).thenReturn(Optional.of(user));
    when(userHelper.getDummyEmail(userId)).thenReturn(dummyEmail);
    when(identityClientConfig.getEmailDummySuffix()).thenReturn(dummyEmail);

    accountProvider.changeUserAccountEmailAddress(Optional.empty());

    verify(keycloakService).deleteEmailAddress();
    verify(keycloakService, never()).changeEmailAddress(anyString());
    verify(rocketChatService)
        .updateUser(
            new UserUpdateRequestDTO(user.getRcUserId(), new UserUpdateDataDTO(dummyEmail, true)));
    verify(this.appointmentService, times(1)).updateAskerEmail(user.getUserId(), dummyEmail);
    user.setEmail(dummyEmail);
    verify(userService).saveUser(user);
    assertAllAdviceSeekerNotificationsAreEnabled(user);
    verifyNoMoreInteractions(rocketChatService);
    verify(consultantService).getConsultant(any());
    verifyNoMoreInteractions(consultantService);
  }

  private void assertAllAdviceSeekerNotificationsAreEnabled(User user) {
    assertThat(user.isNotificationsEnabled()).isTrue();
    NotificationsSettingsDTO notificationsSettingsDTO =
        EmailNotificationMapper.mapNotificationsFromJson(user.getNotificationsSettings());
    assertThat(notificationsSettingsDTO.getReassignmentNotificationEnabled()).isTrue();
    assertThat(notificationsSettingsDTO.getAppointmentNotificationEnabled()).isTrue();
    assertThat(notificationsSettingsDTO.getNewChatMessageNotificationEnabled()).isTrue();
  }

  @Test
  public void
      deactivateAndFlagUserAccountForDeletion_Should_DeactivateKeycloakAccountAndSetDeleteDate() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.deactivateAndFlagUserAccountForDeletion();

    verify(keycloakService, times(1)).deactivateUser(USER.getUserId());
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userService, times(1)).saveUser(captor.capture());
    assertNotNull(captor.getValue().getDeleteDate());
    verify(statisticsService).fireEvent(any(DeleteAccountStatisticsEvent.class));
  }

  @Test
  public void updateUserMobileToken_Should_updateUsersMobileToken_When_mobileTokenIsValidString() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.updateUserMobileToken("mobileToken");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(captor.capture());
    assertThat(captor.getValue().getMobileToken()).isEqualTo("mobileToken");
  }

  @Test
  public void updateUserMobileToken_Should_updateUsersMobileToken_When_mobileTokenIsNull() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.updateUserMobileToken(null);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(captor.capture());
    assertThat(captor.getValue().getMobileToken()).isNull();
  }

  @Test
  public void updateUserMobileToken_Should_updateUsersMobileToken_When_mobileTokenIsEmptyString() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.updateUserMobileToken("");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(captor.capture());
    assertThat(captor.getValue().getMobileToken()).isEmpty();
  }

  @Test
  public void addMobileAppToken_Should_callUserServiceAndConsultantService() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);

    this.accountProvider.addMobileAppToken("token");

    verify(this.userService, times(1)).addMobileAppToken(USER_ID, "token");
    verify(this.consultantService, times(1)).addMobileAppToken(USER_ID, "token");
  }
}
