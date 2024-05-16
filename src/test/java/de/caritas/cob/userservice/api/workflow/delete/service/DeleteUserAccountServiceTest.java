package de.caritas.cob.userservice.api.workflow.delete.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.ActionCommandMockProvider;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteDatabaseAskerAction;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteRocketChatAskerAction;
import de.caritas.cob.userservice.api.workflow.delete.action.consultant.DeleteDatabaseConsultantAction;
import de.caritas.cob.userservice.api.workflow.delete.action.consultant.DeleteRocketChatConsultantAction;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteUserAccountServiceTest {

  @InjectMocks private DeleteUserAccountService deleteUserAccountService;

  @Mock private UserRepository userRepository;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private ActionsRegistry actionsRegistry;

  @Mock private WorkflowErrorMailService workflowErrorMailService;

  private final ActionCommandMockProvider commandMockProvider = new ActionCommandMockProvider();

  @Test
  public void deleteUserAccounts_Should_notPerformAnyDeletion_When_noUserAccountIsMarkedDeleted() {
    this.deleteUserAccountService.deleteUserAccounts();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verifyNoMoreInteractions(this.actionsRegistry);
  }

  @Test
  public void deleteUserAccounts_Should_performAskerDeletion_When_userIsMarkedAsDeleted() {
    User user = new User();
    when(this.userRepository.findAllByDeleteDateNotNull()).thenReturn(singletonList(user));
    when(this.actionsRegistry.buildContainerForType(AskerDeletionWorkflowDTO.class))
        .thenReturn(this.commandMockProvider.getActionContainer(AskerDeletionWorkflowDTO.class));

    this.deleteUserAccountService.deleteUserAccounts();

    verify(this.actionsRegistry, times(1)).buildContainerForType(AskerDeletionWorkflowDTO.class);
    verify(this.commandMockProvider.getActionMock(DeleteDatabaseAskerAction.class), times(1))
        .execute(new AskerDeletionWorkflowDTO(user, emptyList()));
    verifyNoMoreInteractions(this.workflowErrorMailService);
  }

  @Test
  public void
      deleteUserAccounts_Should_performConsultantDeletion_When_consultantIsMarkedAsDeleted() {
    Consultant consultant = new Consultant();
    when(this.consultantRepository.findAllByDeleteDateNotNull())
        .thenReturn(singletonList(consultant));
    when(this.actionsRegistry.buildContainerForType(ConsultantDeletionWorkflowDTO.class))
        .thenReturn(
            this.commandMockProvider.getActionContainer(ConsultantDeletionWorkflowDTO.class));

    this.deleteUserAccountService.deleteUserAccounts();

    verify(this.actionsRegistry, times(1))
        .buildContainerForType(ConsultantDeletionWorkflowDTO.class);
    verify(this.commandMockProvider.getActionMock(DeleteDatabaseConsultantAction.class), times(1))
        .execute(new ConsultantDeletionWorkflowDTO(consultant, emptyList()));
    verifyNoMoreInteractions(this.workflowErrorMailService);
  }

  @Test
  public void deleteUserAccounts_Should_sendErrorMails_When_someActionsFail()
      throws RocketChatDeleteUserException {
    Consultant consultant = new Consultant();
    consultant.setRocketChatId("rc consultant id");
    when(this.consultantRepository.findAllByDeleteDateNotNull())
        .thenReturn(singletonList(consultant));
    User user = new User();
    user.setRcUserId("rc user id");
    when(this.userRepository.findAllByDeleteDateNotNull()).thenReturn(singletonList(user));
    RocketChatService rocketChatService = mock(RocketChatService.class);
    DeleteRocketChatAskerAction deleteRocketChatAskerAction =
        new DeleteRocketChatAskerAction(rocketChatService);
    this.commandMockProvider.setCustomClassForAction(
        DeleteRocketChatAskerAction.class, deleteRocketChatAskerAction);
    DeleteRocketChatConsultantAction deleteRocketChatConsultantAction =
        new DeleteRocketChatConsultantAction(rocketChatService);
    this.commandMockProvider.setCustomClassForAction(
        DeleteRocketChatConsultantAction.class, deleteRocketChatConsultantAction);
    when(this.actionsRegistry.buildContainerForType(ConsultantDeletionWorkflowDTO.class))
        .thenReturn(
            this.commandMockProvider.getActionContainer(ConsultantDeletionWorkflowDTO.class));
    when(this.actionsRegistry.buildContainerForType(AskerDeletionWorkflowDTO.class))
        .thenReturn(this.commandMockProvider.getActionContainer(AskerDeletionWorkflowDTO.class));
    doThrow(new RuntimeException()).when(rocketChatService).deleteUser(any());

    this.deleteUserAccountService.deleteUserAccounts();

    verify(this.workflowErrorMailService, times(1)).buildAndSendErrorMail(anyList());
  }
}
