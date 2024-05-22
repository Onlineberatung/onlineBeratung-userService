package de.caritas.cob.userservice.api.admin.service.user.anonymous;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.internal.WhiteboxImpl.getInternalState;
import static org.powermock.reflect.internal.WhiteboxImpl.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUsernameRegistry;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AnonymousUsernameRegistryTest {

  @InjectMocks private AnonymousUsernameRegistry anonymousUsernameRegistry;
  @Mock private UserService userService;
  @Mock private ConsultantService consultantService;
  @Mock private UsernameTranscoder usernameTranscoder;

  @BeforeEach
  void setUp() {
    setField(anonymousUsernameRegistry, "usernameTranscoder", usernameTranscoder);
    setField(anonymousUsernameRegistry, "usernamePrefix", "Ratsuchende_r ");
  }

  @Test
  void
      generateUniqueUsername_Should_GenerateUsernameWithMissingIdOfList_When_MissingUserIdNotExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 3"));
  }

  private void setIdRegistryField(LinkedList<Integer> idRegistryListWithoutThree) {
    setInternalState(AnonymousUsernameRegistry.class, "ID_REGISTRY", idRegistryListWithoutThree);
  }

  @Test
  void
      generateUniqueUsername_Should_GenerateUsernameWithSecondMissingIdOfList_When_FirstMissingUserIdIsExistingInDb() {
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
  void
      generateUniqueUsername_Should_GenerateUsernameWithIdOne_When_ListIsEmptyAndUserNotExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>();
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 1"));
  }

  @Test
  void
      generateUniqueUsername_Should_GenerateUsernameWithIdGreaterThanBiggestListId_When_ListContainsContiguousIdsAndUserNotExistingInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 3, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 6"));
  }

  @Test
  void
      generateUniqueUsername_Should_GenerateUsernameWithIdGreaterThanBiggestListId_When_MissingIdsOfListHaveExistingUsersInDb() {
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

  @Test
  void
      generateUniqueUsername_Should_GenerateUsernameWithIdGreaterThanBiggestListId_When_MissingIdsOfListHaveExistingConsultantsInDb() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 6));
    setIdRegistryField(idRegistryListWithoutThree);
    when(consultantService.getConsultantByUsername("Ratsuchende_r 3"))
        .thenReturn(Optional.of(CONSULTANT));
    when(consultantService.getConsultantByUsername("Ratsuchende_r 5"))
        .thenReturn(Optional.of(CONSULTANT));
    when(consultantService.getConsultantByUsername("Ratsuchende_r 7")).thenReturn(Optional.empty());

    anonymousUsernameRegistry.generateUniqueUsername();

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(usernameTranscoder, times(1)).encodeUsername(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue(), is("Ratsuchende_r 7"));
  }

  @Test
  void
      removeRegistryIdByUsername_Should_RemoveIdFromRegistry_When_UsernameMatchesUsernamePattern() {
    String usernameToDelete = "Ratsuchende_r 2";

    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());
    when(usernameTranscoder.decodeUsername(anyString())).thenReturn(usernameToDelete);

    anonymousUsernameRegistry.removeRegistryIdByUsername(usernameToDelete);

    List<Integer> resultingRegistry = getIdRegistryField();
    assertThat(resultingRegistry, is(List.of(1, 4, 5)));
    verify(usernameTranscoder, times(1)).decodeUsername(usernameToDelete);
  }

  private List<Integer> getIdRegistryField() {
    return getInternalState(AnonymousUsernameRegistry.class, "ID_REGISTRY");
  }

  @Test
  void
      removeRegistryIdByUsername_Should_NotModifyTheRegistry_When_UsernameDoesNotMatchUsernamePattern() {
    String usernameNotMatchingPattern = "something else";

    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());
    when(usernameTranscoder.decodeUsername(anyString())).thenReturn(usernameNotMatchingPattern);

    anonymousUsernameRegistry.removeRegistryIdByUsername(usernameNotMatchingPattern);

    List<Integer> resultingRegistry = getIdRegistryField();
    assertThat(resultingRegistry, is(idRegistryListWithoutThree));
    verify(usernameTranscoder, times(1)).decodeUsername(usernameNotMatchingPattern);
  }

  @Test
  void
      removeRegistryIdByUsername_Should_NotModifyTheRegistry_When_UsernameDoesMatchUsernamePatternButHasNotExistingNumber() {
    String usernameWithNotExistingId = "Ratsuchende_r 3";

    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());
    when(usernameTranscoder.decodeUsername(anyString())).thenReturn(usernameWithNotExistingId);

    anonymousUsernameRegistry.removeRegistryIdByUsername(usernameWithNotExistingId);

    List<Integer> resultingRegistry = getIdRegistryField();
    assertThat(resultingRegistry, is(idRegistryListWithoutThree));
    verify(usernameTranscoder, times(1)).decodeUsername(usernameWithNotExistingId);
  }

  @Test
  void removeRegistryIdByUsername_Should_NotModifyTheRegistry_When_EmptyValuePassed() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    List<String> emptyStrings = List.of("", "   ");
    emptyStrings.forEach(
        emptyValue -> {
          when(usernameTranscoder.decodeUsername(anyString())).thenReturn(emptyValue);
          anonymousUsernameRegistry.removeRegistryIdByUsername(emptyValue);
          List<Integer> resultingRegistry = getIdRegistryField();
          assertThat(resultingRegistry, is(idRegistryListWithoutThree));
        });
  }

  @Test
  void removeRegistryIdByUsername_Should_NotModifyTheRegistry_When_NullPassed() {
    LinkedList<Integer> idRegistryListWithoutThree = new LinkedList<>(List.of(1, 2, 4, 5));
    setIdRegistryField(idRegistryListWithoutThree);
    when(userService.findUserByUsername(any())).thenReturn(Optional.empty());

    anonymousUsernameRegistry.removeRegistryIdByUsername(null);
    List<Integer> resultingRegistry = getIdRegistryField();
    assertThat(resultingRegistry, is(idRegistryListWithoutThree));
  }
}
