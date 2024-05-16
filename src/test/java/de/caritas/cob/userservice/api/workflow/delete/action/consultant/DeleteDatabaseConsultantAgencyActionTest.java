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
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
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
public class DeleteDatabaseConsultantAgencyActionTest {

  @InjectMocks private DeleteDatabaseConsultantAgencyAction deleteDatabaseConsultantAgencyAction;

  @Mock private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(DeleteDatabaseConsultantAgencyAction.class, "log", logger);
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformDeletion_When_consultantAgencyCanBeDeleted() {
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), emptyList());

    this.deleteDatabaseConsultantAgencyAction.execute(workflowDTO);

    assertThat(workflowDTO.getDeletionWorkflowErrors(), hasSize(0));
    verifyNoMoreInteractions(this.logger);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_deletionFails() {
    Consultant consultant = new Consultant();
    consultant.setId("consultantId");
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(consultant, new ArrayList<>());
    doThrow(new RuntimeException()).when(this.consultantAgencyRepository).deleteAll(any());

    this.deleteDatabaseConsultantAgencyAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is("consultantId"));
    assertThat(
        workflowErrors.get(0).getReason(), is("Could not delete consultant agency relations"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }
}
