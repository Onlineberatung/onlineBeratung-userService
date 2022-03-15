package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUserAnonymousService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserAnonymousSchedulerTest {

  @InjectMocks
  private DeleteUserAnonymousScheduler deleteUserAnonymousScheduler;

  @Mock
  private DeleteUserAnonymousService deleteUserAnonymousService;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteInactiveAnonymousUsers() {
    this.deleteUserAnonymousScheduler.performDeletionWorkflow();

    verify(this.deleteUserAnonymousService, times(1)).deleteInactiveAnonymousUsers();
  }
}
