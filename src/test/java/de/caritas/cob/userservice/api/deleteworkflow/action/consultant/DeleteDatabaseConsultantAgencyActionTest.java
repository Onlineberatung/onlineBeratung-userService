package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.SECOND;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDatabaseConsultantAgencyActionTest {

  @InjectMocks
  private DeleteDatabaseConsultantAgencyAction deleteDatabaseConsultantAgencyAction;

  @Mock
  private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void getOrder_Should_returnSecond() {
    assertThat(this.deleteDatabaseConsultantAgencyAction.getOrder(), is(SECOND.getOrder()));
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformDeletion_When_consultantAgencyCanBeDeleted() {
    List<DeletionWorkflowError> workflowErrors = this.deleteDatabaseConsultantAgencyAction
        .execute(new Consultant());

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_deletionFails() {
    doThrow(new RuntimeException()).when(this.consultantAgencyRepository).deleteAll(any());

    Consultant consultant = new Consultant();
    consultant.setId("consultantId");
    List<DeletionWorkflowError> workflowErrors = this.deleteDatabaseConsultantAgencyAction
        .execute(consultant);

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is("consultantId"));
    assertThat(workflowErrors.get(0).getReason(),
        is("Could not delete consultant agency relations"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

}
