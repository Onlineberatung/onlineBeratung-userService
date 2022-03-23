package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUsersRegisteredOnlyServiceTest {

  @InjectMocks
  private DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private DeleteUserAccountService deleteUserAccountService;

  @Mock
  private WorkflowErrorMailService workflowErrorMailService;

  @Mock
  @SuppressWarnings("unused")
  private RocketChatService rocketChatService;

  @Test
  public void deleteUserAccountsTimeSensitive_Should_notPerformAnyDeletion_When_noUserAccountWithoutRunningSessionsIsFound() {
    deleteUsersRegisteredOnlyService.deleteUserAccountsTimeSensitive();

    verifyNoMoreInteractions(deleteUserAccountService);
    verifyNoMoreInteractions(workflowErrorMailService);
  }

  @Test
  public void deleteUserAccountsTimeSensitive_Should_performUserDeletion_When_usersAreFoundWithoutRunningSession() {
    when(userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
        Mockito.any()))
        .thenReturn(Collections.singletonList(USER));

    deleteUsersRegisteredOnlyService.deleteUserAccountsTimeSensitive();

    verify(deleteUserAccountService).performUserDeletion(USER);
  }

  @Test
  public void deleteUserAccountsTimeSensitive_Should_SendWorkflowErrorMail_When_WorkflowsErrorsOccurs() {
    var deletionWorkflowError = mock(DeletionWorkflowError.class);
    var workflowErrors = Collections.singletonList(deletionWorkflowError);
    when(userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
        Mockito.any()))
        .thenReturn(Collections.singletonList(USER));
    when(deleteUserAccountService.performUserDeletion(USER)).thenReturn(workflowErrors);

    deleteUsersRegisteredOnlyService.deleteUserAccountsTimeSensitive();

    verify(workflowErrorMailService).buildAndSendErrorMail(workflowErrors);
  }

  @Test
  public void deleteUserAccountsTimeSensitive_ShouldNot_SendWorkflowErrorMail_When_NoWorkflowsErrorsOccurs() {
    when(userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
        Mockito.any()))
        .thenReturn(Collections.singletonList(USER));
    when(deleteUserAccountService.performUserDeletion(USER)).thenReturn(Collections.emptyList());

    deleteUsersRegisteredOnlyService.deleteUserAccountsTimeSensitive();

    verify(workflowErrorMailService, never()).buildAndSendErrorMail(Mockito.any());
  }

  @Test
  public void deleteUserAccountsTimeSensitive_Should_CheckUsersWithCorrectDate() {
    var thirtyDays = 30;

    setField(deleteUsersRegisteredOnlyService,
        "userRegisteredOnlyDeleteWorkflowCheckDays",
        thirtyDays);
    LocalDateTime dateToCheck = LocalDateTime
        .now()
        .with(LocalTime.MIDNIGHT)
        .minusDays(thirtyDays);

    deleteUsersRegisteredOnlyService.deleteUserAccountsTimeSensitive();

    verify(userRepository).findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
        dateToCheck);
  }

  @Test
  public void deleteUserAccountsTimeInsensitive_Should_CheckUsersIgnoringTheDateSetting() {
    setField(deleteUsersRegisteredOnlyService, "userRegisteredOnlyDeleteWorkflowCheckDays", 30);
    var dateToCheck = LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(1);

    deleteUsersRegisteredOnlyService.deleteUserAccountsTimeInsensitive();

    verify(userRepository).findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
        dateToCheck);
    assertTrue(dateToCheck.isAfter(LocalDateTime.now()));
  }
}
