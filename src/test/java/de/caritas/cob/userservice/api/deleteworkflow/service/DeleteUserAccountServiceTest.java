package de.caritas.cob.userservice.api.deleteworkflow.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.deleteworkflow.action.DeleteRocketChatUserAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteDatabaseAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteDatabaseConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.registry.DeleteActionsRegistry;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserAccountServiceTest {

  @InjectMocks
  private DeleteUserAccountService deleteUserAccountService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private DeleteActionsRegistry deleteActionsRegistry;

  @Mock
  private WorkflowErrorMailService workflowErrorMailService;

  @Test
  public void deleteUserAccounts_Should_notPerformAnyDeletion_When_noUserAccountIsMarkedDeleted() {
    this.deleteUserAccountService.deleteUserAccounts();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verifyNoMoreInteractions(this.deleteActionsRegistry);
  }

  @Test
  public void deleteUserAccounts_Should_performAskerDeletion_When_userIsMarkedAsDeleted() {
    User user = new User();
    when(this.userRepository.findAllByDeleteDateNotNull()).thenReturn(singletonList(user));
    DeleteAskerAction deleteAskerAction = mock(DeleteDatabaseAskerAction.class);
    when(this.deleteActionsRegistry.getAskerDeleteActions())
        .thenReturn(singletonList(deleteAskerAction));

    this.deleteUserAccountService.deleteUserAccounts();

    verify(this.deleteActionsRegistry, times(1)).getAskerDeleteActions();
    verify(deleteAskerAction, times(1)).execute(user);
    verifyNoMoreInteractions(this.workflowErrorMailService);
  }

  @Test
  public void deleteUserAccounts_Should_performConsultantDeletion_When_consultantIsMarkedAsDeleted() {
    Consultant consultant = new Consultant();
    when(this.consultantRepository.findAllByDeleteDateNotNull())
        .thenReturn(singletonList(consultant));
    DeleteConsultantAction deleteConsultantAction = mock(DeleteDatabaseConsultantAction.class);
    when(this.deleteActionsRegistry.getConsultantDeleteActions())
        .thenReturn(singletonList(deleteConsultantAction));

    this.deleteUserAccountService.deleteUserAccounts();

    verify(this.deleteActionsRegistry, times(1)).getConsultantDeleteActions();
    verify(deleteConsultantAction, times(1)).execute(consultant);
    verifyNoMoreInteractions(this.workflowErrorMailService);
  }

  @Test
  public void deleteUserAccounts_Should_sendErrorMails_When_someActionsFail() {
    Consultant consultant = new Consultant();
    when(this.consultantRepository.findAllByDeleteDateNotNull())
        .thenReturn(singletonList(consultant));
    User user = new User();
    when(this.userRepository.findAllByDeleteDateNotNull()).thenReturn(singletonList(user));
    DeleteRocketChatUserAction deleteRocketChatUserAction = mock(DeleteRocketChatUserAction.class);
    when(this.deleteActionsRegistry.getConsultantDeleteActions())
        .thenReturn(singletonList(deleteRocketChatUserAction));
    when(this.deleteActionsRegistry.getAskerDeleteActions())
        .thenReturn(singletonList(deleteRocketChatUserAction));

    DeletionWorkflowError error = DeletionWorkflowError.builder().build();
    when(deleteRocketChatUserAction.execute(any(User.class))).thenReturn(singletonList(error));
    when(deleteRocketChatUserAction.execute(any(Consultant.class)))
        .thenReturn(singletonList(error));

    this.deleteUserAccountService.deleteUserAccounts();

    List<DeletionWorkflowError> expectedErrors = asList(error, error);
    verify(this.workflowErrorMailService, times(1)).buildAndSendErrorMail(expectedErrors);
  }

}
