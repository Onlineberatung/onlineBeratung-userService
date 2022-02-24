package de.caritas.cob.userservice.api.actions.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateKeycloakUserActionCommandTest {

  @InjectMocks
  private DeactivateKeycloakUserActionCommand deactivateKeycloakUserActionCommand;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Test
  void execute_Should_deactivateUserInKeycloak() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn("user id");

    this.deactivateKeycloakUserActionCommand.execute(user);

    verify(this.keycloakAdminClientService, times(1)).deactivateUser("user id");
  }

}
