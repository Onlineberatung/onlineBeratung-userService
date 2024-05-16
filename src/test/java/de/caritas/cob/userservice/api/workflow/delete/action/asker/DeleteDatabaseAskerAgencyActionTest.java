package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.DATABASE;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DeleteDatabaseAskerAgencyActionTest {

  @InjectMocks private DeleteDatabaseAskerAgencyAction deleteDatabaseAskerAgencyAction;

  @Mock private UserAgencyRepository userAgencyRepository;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(DeleteDatabaseAskerAgencyAction.class, "log", logger);
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformDeletion_When_userHasNoAgencyAssigned() {
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(new User(), emptyList());

    this.deleteDatabaseAskerAgencyAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_deletionFails() {
    doThrow(new RuntimeException()).when(this.userAgencyRepository).deleteAll(any());
    User user = new User();
    user.setUserId("userId");
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(user, new ArrayList<>());

    this.deleteDatabaseAskerAgencyAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is("userId"));
    assertThat(workflowErrors.get(0).getReason(), is("Could not delete user agency relations"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }
}
