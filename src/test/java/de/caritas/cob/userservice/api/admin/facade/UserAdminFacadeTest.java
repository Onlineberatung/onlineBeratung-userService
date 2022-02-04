package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserAdminFacadeTest {

  @InjectMocks
  private UserAdminFacade userAdminFacade;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private UserService userService;

  @Test(expected = NotFoundException.class)
  public void markAskerForDeletion_Should_throwNotFoundException_When_askerDoesNotExist() {
    when(this.userService.getUser(any())).thenReturn(Optional.empty());

    this.userAdminFacade.markAskerForDeletion("user id");
  }

  @Test(expected = ConflictException.class)
  public void markAskerForDeletion_Should_throwConflictException_When_askerIsAlreadyMarkedForDeletion() {
    User user = new User();
    user.setDeleteDate(nowInUtc());
    when(this.userService.getUser(any())).thenReturn(Optional.of(user));

    this.userAdminFacade.markAskerForDeletion("user id");
  }

  @Test
  public void markAskerForDeletion_Should_markUserForDeletion_When_askerExistsAndIsNotMarkedForDeletion() {
    User user = new User();
    when(this.userService.getUser(any())).thenReturn(Optional.of(user));

    this.userAdminFacade.markAskerForDeletion("user id");

    verify(this.keycloakAdminClientService, times(1)).deactivateUser("user id");
    ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
    verify(this.userService, times(1)).saveUser(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getDeleteDate(), notNullValue());
  }

}
