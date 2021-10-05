package de.caritas.cob.userservice.api.deleteworkflow.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
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
  private RocketChatService rocketChatService;

  @Test
  public void deleteUserAccounts_Should_notPerformAnyDeletion_When_noUserAccountWithoutRunningSessionsIsFound() {

    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();

    verifyNoMoreInteractions(deleteUserAccountService);
    verifyNoMoreInteractions(workflowErrorMailService);

  }

  @Test
  public void deleteUserAccounts_Should_performAskerDeletion_When_usersAreFoundWithoutRunningSession() {

    when(this.userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(Mockito.any()))
        .thenReturn(Collections.singletonList(USER));

    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();

    verify(deleteUserAccountService, times(1)).performUserDeletion(USER);

  }

  @Test
  public void deleteUserAccounts_Should_SendWorkflowErrorMail_When_WorkflowsErrorsOccurs() {

    DeletionWorkflowError deletionWorkflowError = mock(DeletionWorkflowError.class);
    var workflowErrors = Collections.singletonList(deletionWorkflowError);
    when(this.userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(Mockito.any()))
        .thenReturn(Collections.singletonList(USER));
    when(deleteUserAccountService.performUserDeletion(USER)).thenReturn(workflowErrors);

    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();

    verify(workflowErrorMailService, times(1)).buildAndSendErrorMail(workflowErrors);
  }

  @Test
  public void deleteUserAccounts_ShouldNot_SendWorkflowErrorMail_When_NoWorkflowsErrorsOccurs() {

    when(this.userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(Mockito.any()))
        .thenReturn(Collections.singletonList(USER));
    when(deleteUserAccountService.performUserDeletion(USER)).thenReturn(Collections.emptyList());

    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();

    verify(workflowErrorMailService, never()).buildAndSendErrorMail(Mockito.any());
  }

  @Test
  public void deleteUserAccounts_Should_CheckUsersWithCorrectDate() {

    String fieldNameUserRegisteredOnlyDeleteWorkflowCheckDays = "userRegisteredOnlyDeleteWorkflowCheckDays";
    int valueUserRegisteredOnlyDeleteWorkflowCheckDays = 30;

    setField(deleteUsersRegisteredOnlyService,
        fieldNameUserRegisteredOnlyDeleteWorkflowCheckDays,
        valueUserRegisteredOnlyDeleteWorkflowCheckDays);
    LocalDateTime dateToCheck = LocalDateTime
        .now()
        .with(LocalTime.MIDNIGHT)
        .minusDays(valueUserRegisteredOnlyDeleteWorkflowCheckDays);

    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();

    verify(userRepository, times(1)).findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateToCheck);

  }
}