package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static java.util.Collections.emptyList;
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

import de.caritas.cob.userservice.api.deleteworkflow.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgencyRepository;
import de.caritas.cob.userservice.api.service.LogService;
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
public class DeleteDatabaseAskerAgencyActionTest {

  @InjectMocks
  private DeleteDatabaseAskerAgencyAction deleteDatabaseAskerAgencyAction;

  @Mock
  private UserAgencyRepository userAgencyRepository;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
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
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

}
