package de.caritas.cob.userservice.api.admin.service.user.anonymous;

import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.internal.WhiteboxImpl.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUsernameRegistry;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class AnonymousUsernameRegistryTest {

  @InjectMocks
  private AnonymousUsernameRegistry anonymousUsernameRegistry;
  @Mock
  private UserService userService;
  @Mock
  private UsernameTranscoder usernameTranscoder;

  @Before
  public void setUp() {
    setField(anonymousUsernameRegistry, "usernameTranscoder", usernameTranscoder);
    setField(anonymousUsernameRegistry, "usernamePrefix", "Ratsuchende_r ");
  }

  @Test
  public void generateUniqueUsername_Should_GenerateUsernameWithMissingIdOfList_When_MissingUserIdNotExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 3"));
  }

  private void setIdRegistryField(LinkedList<Integer> idRegistryListWithoutThree) {
    setInternalState(AnonymousUsernameRegistry.class, "ID_REGISTRY",
        idRegistryListWithoutThree);
  }

  @Test
  public void generateUniqueUsername_Should_GenerateUsernameWithSecondMissingIdOfList_When_FirstMissingUserIdIsExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 6));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername("Ratsuchende_r 3")).thenReturn(Optional.of(USER));
    when(userService.findUserByUsername("Ratsuchende_r 5")).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 5"));
  }

  @Test
  public void generateUniqueUsername_Should_GenerateUsernameWithIdOne_When_ListIsEmptyAndUserNotExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>();
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 1"));
  }

  @Test
  public void generateUniqueUsername_Should_GenerateUsernameWithIdGreaterThanBiggestListId_When_ListContainsContiguousIdsAndUserNotExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 3, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 6"));
  }

  @Test
  public void generateUniqueUsername_Should_GenerateUsernameWithIdGreaterThanBiggestListId_When_MissingIdsOfListHaveExistingUsersInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 6));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername("Ratsuchende_r 3")).thenReturn(Optional.of(USER));
    when(userService.findUserByUsername("Ratsuchende_r 5")).thenReturn(Optional.of(USER));
    when(userService.findUserByUsername("Ratsuchende_r 7")).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 7"));
  }
}
