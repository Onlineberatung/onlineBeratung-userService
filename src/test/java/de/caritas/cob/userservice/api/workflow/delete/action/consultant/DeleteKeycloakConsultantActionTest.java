package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.KEYCLOAK;
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
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.workflow.delete.action.DeleteKeycloakUserAction;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
public class DeleteKeycloakConsultantActionTest {

  @InjectMocks private DeleteKeycloakConsultantAction deleteKeycloakConsultantAction;

  @Mock private KeycloakService keycloakService;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(DeleteKeycloakConsultantAction.class, "log", logger);
    setInternalState(DeleteKeycloakUserAction.class, "log", logger);
  }

  @Test
  public void
      execute_Should_deleteKeycloakUserAndReturnEmptyList_When_consultantDeletionIsSuccessful() {
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), emptyList());

    this.deleteKeycloakConsultantAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verify(this.keycloakService, times(1)).deleteUser(any());
  }

  @Test
  public void
      execute_Should_returnExpectedWorkflowErrorAndLogError_When_consultantDeletionFailes() {
    Consultant consultant = new Consultant();
    consultant.setId("consultantId");
    doThrow(new RuntimeException()).when(this.keycloakService).deleteUser(any());
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(consultant, new ArrayList<>());

    this.deleteKeycloakConsultantAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(KEYCLOAK));
    assertThat(workflowErrors.get(0).getIdentifier(), is("consultantId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete keycloak user account"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }

  @Test
  public void execute_Should_notReturnWorkflowErrorIfUserCouldNotBeFoundInKeycloak() {
    Consultant consultant = new Consultant();
    consultant.setId("consultantId");
    doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
        .when(this.keycloakService)
        .deleteUser(any());
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(consultant, new ArrayList<>());

    this.deleteKeycloakConsultantAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verify(logger)
        .warn(
            "No user with id {} could be found in keycloak, but proceeding with further actions.",
            "consultantId");
  }
}
