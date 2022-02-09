package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.OPTIONAL_OTP_INFO_DTO;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.model.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class TwoFactorAuthValidatorTest {

  @InjectMocks
  private TwoFactorAuthValidator twoFactorAuthValidator;

  @Mock
  private IdentityClient identityClient;

  @Mock
  private IdentityClientConfig identityClientConfig;

  @Test
  public void createAndValidateTwoFactorAuthDTO_Should_Return_Valid_Object_When_Request_Was_Successful() {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(UserRole.USER.getValue()));
    authenticatedUser.setUsername("test");
    when(identityClientConfig.getOtpAllowedForUsers()).thenReturn(true);
    when(identityClient.getOtpCredential("test"))
        .thenReturn(OPTIONAL_OTP_INFO_DTO);

    var twoFactorAuthDTO = new TwoFactorAuthDTO()
        .isEnabled(true).isActive(false).secret("secret").qrCode("QrCode");

    Assertions
        .assertEquals(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser),
            twoFactorAuthDTO);
  }

  @Test
  public void createAndValidateTwoFactorAuthDTO_Should_Return_Null_When_Request_Was_Not_Successful() {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(UserRole.USER.getValue()));
    authenticatedUser.setUsername("test");
    when(identityClientConfig.getOtpAllowedForUsers()).thenReturn(true);
    when(identityClient.getOtpCredential("test"))
        .thenReturn(Optional.empty());

    var twoFactorAuthDTO = new TwoFactorAuthDTO().isEnabled(false);

    Assertions
        .assertEquals(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser),
            twoFactorAuthDTO);
  }

  @Test
  public void createAndValidateTwoFactorAuthDTO_Should_Return_Valid_Object_When_Two_Factor_Auth_For_Role_Is_Disabled() {
    var authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setRoles(Set.of(UserRole.USER.getValue()));
    authenticatedUser.setUsername("test");
    when(identityClientConfig.getOtpAllowedForUsers()).thenReturn(false);

    var twoFactorAuthDTO = new TwoFactorAuthDTO().isEnabled(false);

    Assertions
        .assertEquals(twoFactorAuthValidator.createAndValidateTwoFactorAuthDTO(authenticatedUser),
            twoFactorAuthDTO);
    verify(identityClient, times(0))
        .getOtpCredential(Mockito.any());
  }

}
