package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FIRST;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.KEYCLOAK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeleteKeycloakUserActionTest {

  @InjectMocks
  private DeleteKeycloakUserAction deleteKeycloakUserAction;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void getOrder_Should_returnFirst() {
    assertThat(deleteKeycloakUserAction.getOrder(), is(FIRST.getOrder()));
  }

  @Test
  public void execute_Should_deleteKeycloakUserAndReturnEmptyList_When_userDeletionIsSuccessful() {
    List<DeletionWorkflowError> workflowErrors = this.deleteKeycloakUserAction.execute(new User());

    assertThat(workflowErrors, hasSize(0));
    verify(this.keycloakAdminClientService, times(1)).deleteUser(any());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_userDeletionFailes() {
    User user = new User();
    user.setUserId("userId");
    doThrow(new RuntimeException()).when(this.keycloakAdminClientService).deleteUser(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteKeycloakUserAction.execute(user);

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(KEYCLOAK));
    assertThat(workflowErrors.get(0).getIdentifier(), is("userId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete keycloak user account"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

  @Test
  public void execute_Should_deleteKeycloakUserAndReturnEmptyList_When_consultantDeletionIsSuccessful() {
    List<DeletionWorkflowError> workflowErrors = this.deleteKeycloakUserAction
        .execute(new Consultant());

    assertThat(workflowErrors, hasSize(0));
    verify(this.keycloakAdminClientService, times(1)).deleteUser(any());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_consultantDeletionFailes() {
    Consultant consultant = new Consultant();
    consultant.setId("consultantId");
    doThrow(new RuntimeException()).when(this.keycloakAdminClientService).deleteUser(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteKeycloakUserAction.execute(consultant);

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(KEYCLOAK));
    assertThat(workflowErrors.get(0).getIdentifier(), is("consultantId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete keycloak user account"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

}
