package de.caritas.cob.userservice.api.service.user;

import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.model.PasswordDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.KeycloakService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.user.validation.UserAccountValidator;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidatedUserAccountProviderTest {

  @InjectMocks
  private ValidatedUserAccountProvider accountProvider;
  @Mock
  private UserService userService;
  @Mock
  private ConsultantService consultantService;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private KeycloakService keycloakService;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private UserAccountValidator userAccountValidator;
  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;
  @Mock
  private UserRepository userRepository;

  @Test
  public void retrieveValidatedUser_Should_ReturnUser_When_UserIsPresent() {
    User userMock = mock(User.class);
    when(userService.getUser(any())).thenReturn(Optional.of(userMock));

    User resultUser = this.accountProvider.retrieveValidatedUser();

    assertThat(resultUser, is(userMock));
  }

  @Test(expected = InternalServerErrorException.class)
  public void retrieveValidatedUser_Should_Throw_InternalServerErrorException_When_UserIsNotPresent() {
    when(userService.getUser(any())).thenReturn(Optional.empty());

    this.accountProvider.retrieveValidatedUser();
  }

  @Test
  public void retrieveValidatedConsultant_Should_ReturnConsultant_When_ConsultantIsPresent() {
    Consultant consultantMock = mock(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

    Consultant resultUser = this.accountProvider.retrieveValidatedConsultant();

    assertThat(resultUser, is(consultantMock));
  }

  @Test(expected = InternalServerErrorException.class)
  public void retrieveValidatedConsultant_Should_Throw_InternalServerErrorException_When_ConsultantIsNotPresent() {
    when(consultantService.getConsultant(any())).thenReturn(Optional.empty());

    this.accountProvider.retrieveValidatedConsultant();
  }

  @Test
  public void retrieveValidatedTeamConsultant_Should_ReturnTeamConsultant_When_TeamConsultantIsPresent() {
    Consultant teamConsultantMock = mock(Consultant.class);
    when(teamConsultantMock.isTeamConsultant()).thenReturn(true);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(teamConsultantMock));

    Consultant resultUser = this.accountProvider.retrieveValidatedTeamConsultant();

    assertThat(resultUser, is(teamConsultantMock));
  }

  @Test(expected = ForbiddenException.class)
  public void retrieveValidatedTeamConsultant_Should_Throw_ForbiddenException_When_ConsultantIsNotATeamConsultant() {
    Consultant consultantMock = mock(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

    this.accountProvider.retrieveValidatedTeamConsultant();
  }

  @Test
  public void changeUserAccountEmailAddress_Should_changeAddressInKeycloakRocketChatAndConsultantRepository_When_authenticatedUserIsConsultant() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.authenticatedUser.getUserId()).thenReturn("consultant");
    when(this.consultantService.getConsultant("consultant")).thenReturn(Optional.of(consultant));

    this.accountProvider.changeUserAccountEmailAddress("newMail");

    verify(this.keycloakService, times(1)).changeEmailAddress("newMail");
    verify(this.rocketChatService, times(1))
        .updateUser(
            new UserUpdateRequestDTO(
                consultant.getRocketChatId(),
                new UserUpdateDataDTO("newMail", consultant.getFullName())));
    consultant.setEmail("newMail");
    verify(this.consultantService, times(1)).saveConsultant(consultant);
    verifyNoMoreInteractions(this.rocketChatService);
    verify(this.userService, times(1)).getUser(any());
    verifyNoMoreInteractions(this.userService);
  }

  @Test
  public void changeUserAccountEmailAddress_Should_changeAddressInKeycloakRocketChatAndUserRepository_When_authenticatedUserIsUser() {
    User user = new EasyRandom().nextObject(User.class);
    when(this.authenticatedUser.getUserId()).thenReturn("user");
    when(this.userService.getUser("user")).thenReturn(Optional.of(user));

    this.accountProvider.changeUserAccountEmailAddress("newMail");

    verify(this.keycloakService, times(1)).changeEmailAddress("newMail");
    verify(this.rocketChatService, times(1))
        .updateUser(
            new UserUpdateRequestDTO(
                user.getRcUserId(), new UserUpdateDataDTO("newMail", user.getUsername())));
    user.setEmail("newMail");
    verify(this.userService, times(1)).saveUser(user);
    verifyNoMoreInteractions(this.rocketChatService);
    verify(this.consultantService, times(1)).getConsultant(any());
    verifyNoMoreInteractions(this.consultantService);
  }

  @Test(expected = InternalServerErrorException.class)
  public void changePassword_Should_ThrowInternalServerErrorException_When_KeycloakPwChangeCallFails() {
    PasswordDTO passwordDTO = new EasyRandom().nextObject(PasswordDTO.class);

    this.accountProvider.changePassword(passwordDTO);
  }

  @Test
  public void changePassword_Should_CallKeycloakAdminClientServiceToUpdateThePassword() {
    EasyRandom random = new EasyRandom();
    PasswordDTO passwordDTO = random.nextObject(PasswordDTO.class);
    when(keycloakService.changePassword(any(), any())).thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);

    this.accountProvider.changePassword(passwordDTO);

    verify(keycloakService, times(1)).changePassword(any(), any());
  }

  @Test(expected = BadRequestException.class)
  public void deactivateAndFlagUserAccountForDeletion_ShouldNot_DeactivateKeycloakAccountOrSetDeleteDate_When_PasswordIsIncorrect() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));
    doThrow(new BadRequestException(MESSAGE)).when(userAccountValidator)
        .checkPasswordValidity(anyString(), anyString());

    DeleteUserAccountDTO deleteUserAccountDTO = new EasyRandom()
        .nextObject(DeleteUserAccountDTO.class);
    this.accountProvider.deactivateAndFlagUserAccountForDeletion(deleteUserAccountDTO);

    verifyNoInteractions(keycloakAdminClientService);
    verifyNoInteractions(userRepository);
  }

  @Test
  public void deactivateAndFlagUserAccountForDeletion_Should_DeactivateKeycloakAccountAndSetDeleteDate_When_PasswordIsCorrect() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    DeleteUserAccountDTO deleteUserAccountDTO =
        new EasyRandom().nextObject(DeleteUserAccountDTO.class);
    this.accountProvider.deactivateAndFlagUserAccountForDeletion(deleteUserAccountDTO);

    verify(keycloakAdminClientService, times(1)).deactivateUser(USER.getUserId());
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userService, times(1)).saveUser(captor.capture());
    assertNotNull(captor.getValue().getDeleteDate());
  }

  @Test
  public void updateUserMobileToken_Should_updateUsersMobileToken_When_mobileTokenIsValidString() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.updateUserMobileToken("mobileToken");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(captor.capture());
    assertThat(captor.getValue().getMobileToken(), is("mobileToken"));
  }

  @Test
  public void updateUserMobileToken_Should_updateUsersMobileToken_When_mobileTokenIsNull() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.updateUserMobileToken(null);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(captor.capture());
    assertThat(captor.getValue().getMobileToken(), nullValue());
  }

  @Test
  public void updateUserMobileToken_Should_updateUsersMobileToken_When_mobileTokenIsEmptyString() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(userService.getUser(USER_ID)).thenReturn(Optional.of(USER));

    this.accountProvider.updateUserMobileToken("");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(captor.capture());
    assertThat(captor.getValue().getMobileToken(), is(""));
  }

  @Test
  public void addMobileAppToken_Should_callUserServiceAndConsultantService() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);

    this.accountProvider.addMobileAppToken("token");

    verify(this.userService, times(1)).addMobileAppToken(USER_ID, "token");
    verify(this.consultantService, times(1)).addMobileAppToken(USER_ID, "token");
  }

}
