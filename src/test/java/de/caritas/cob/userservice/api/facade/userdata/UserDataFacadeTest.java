package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.TwoFactorAuthValidator;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserDataFacadeTest {

  @Mock
  AuthenticatedUser authenticatedUser;
  @Mock
  AskerDataProvider askerDataProvider;
  @Mock
  ConsultantDataProvider consultantDataProvider;
  @Mock
  ValidatedUserAccountProvider userAccountProvider;
  @Mock
  TwoFactorAuthValidator twoFactorAuthValidator;
  @InjectMocks
  private UserDataFacade userDataFacade;


  @Test
  public void buildUserDataByRole_Should_ReturnUserDataResponseDTO_When_ProvidedWithValidUser() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userAccountProvider.retrieveValidatedUser()).thenReturn(USER);
    when(askerDataProvider.retrieveData(USER)).thenReturn(new UserDataResponseDTO());

    assertThat(userDataFacade.buildUserDataByRole(),
        instanceOf(UserDataResponseDTO.class));
  }

  @Test
  public void buildUserDataByRole_Should_BuildConsultantData_WhenRoleConsultant() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(userAccountProvider.retrieveValidatedConsultant()).thenReturn(CONSULTANT);
    when(consultantDataProvider.retrieveData(any())).thenReturn(new UserDataResponseDTO());

    userDataFacade.buildUserDataByRole();

    verify(consultantDataProvider, times(1)).retrieveData(CONSULTANT);
    verify(twoFactorAuthValidator, times(1)).createAndValidateTwoFactorAuthDTO(any());
  }

  @Test
  public void buildUserDataByRole_Should_BuildUserData_WhenRoleUser() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userAccountProvider.retrieveValidatedUser()).thenReturn(USER);
    when(askerDataProvider.retrieveData(any())).thenReturn(new UserDataResponseDTO());

    userDataFacade.buildUserDataByRole();

    verify(askerDataProvider, times(1)).retrieveData(USER);
    verify(twoFactorAuthValidator, times(1)).createAndValidateTwoFactorAuthDTO(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void buldUserDataByRole_Should_ThrowInternalServerErrorException_WhenRoleNotConsultantNeitherUser() {

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.TECHNICAL.getValue()));

    userDataFacade.buildUserDataByRole();
  }

  @Test
  public void buildUserDataByRole_Should_BuildUserData_WhenRoleAnonymous() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.ANONYMOUS.getValue()));
    when(userAccountProvider.retrieveValidatedUser()).thenReturn(USER);
    when(askerDataProvider.retrieveData(any())).thenReturn(new UserDataResponseDTO());

    userDataFacade.buildUserDataByRole();

    verify(askerDataProvider, times(1)).retrieveData(USER);
    verify(twoFactorAuthValidator, times(1)).createAndValidateTwoFactorAuthDTO(any());
  }

}
