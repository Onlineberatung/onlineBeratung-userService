package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;
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

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDatabaseConsultantActionTest {

  @InjectMocks private DeleteDatabaseConsultantAction deleteDatabaseConsultantAction;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private Logger logger;

  @Before
  public void setup() {
    setInternalState(DeleteDatabaseConsultantAction.class, "log", logger);
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformDeletion_When_consultantCanBeDeleted() {
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), emptyList());

    this.deleteDatabaseConsultantAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_deletionFails() {
    doThrow(new RuntimeException()).when(this.consultantRepository).delete(any());
    Consultant consultant = new Consultant();
    consultant.setId("consultantId");
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(consultant, new ArrayList<>());

    this.deleteDatabaseConsultantAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is("consultantId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete consultant in database"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }
}
