package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUserAccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserAccountSchedulerTest {

  @InjectMocks
  private DeleteUserAccountScheduler deleteUserAccountScheduler;

  @Mock
  private DeleteUserAccountService deleteUserAccountService;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteUserAccounts() {
    this.deleteUserAccountScheduler.performDeletionWorkflow();

    verify(this.deleteUserAccountService, times(1)).deleteUserAccounts();
  }

}
