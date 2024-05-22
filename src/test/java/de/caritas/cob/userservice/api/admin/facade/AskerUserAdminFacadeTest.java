package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AskerUserAdminFacadeTest {

  @InjectMocks private AskerUserAdminFacade askerUserAdminFacade;

  @Mock private KeycloakService keycloakService;

  @Mock private UserService userService;

  @Mock private UsernameTranscoder usernameTranscoder;

  @Test
  public void markAskerForDeletion_Should_throwNotFoundException_When_askerDoesNotExist() {
    assertThrows(
        NotFoundException.class,
        () -> {
          when(this.userService.getUser(any())).thenReturn(Optional.empty());

          this.askerUserAdminFacade.markAskerForDeletion("user id");
        });
  }

  @Test
  public void
      markAskerForDeletion_Should_throwConflictException_When_askerIsAlreadyMarkedForDeletion() {
    assertThrows(
        ConflictException.class,
        () -> {
          User user = new User();
          user.setDeleteDate(nowInUtc());
          when(this.userService.getUser(any())).thenReturn(Optional.of(user));

          this.askerUserAdminFacade.markAskerForDeletion("user id");
        });
  }

  @Test
  public void
      markAskerForDeletion_Should_markUserForDeletion_When_askerExistsAndIsNotMarkedForDeletion() {
    User user = new User();
    when(this.userService.getUser(any())).thenReturn(Optional.of(user));

    this.askerUserAdminFacade.markAskerForDeletion("user id");

    verify(this.keycloakService, times(1)).deactivateUser("user id");
    ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getDeleteDate(), notNullValue());
  }
}
