package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ROCKET_CHAT;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.model.User;
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
public class DeleteRocketChatAskerActionTest {

  @InjectMocks private DeleteRocketChatAskerAction deleteRocketChatAskerAction;

  @Mock private RocketChatService rocketChatService;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(DeleteRocketChatAskerAction.class, "log", logger);
  }

  @Test
  public void execute_Should_deleteRocketChatUserAndReturnEmptyList_When_userDeletionIsSuccessful()
      throws RocketChatDeleteUserException {
    User user = new User();
    user.setRcUserId("rcId");
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(user, emptyList());

    this.deleteRocketChatAskerAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verify(this.rocketChatService, times(1)).deleteUser(any());
  }

  @Test
  public void execute_Should_notDeleteRocketChatUserAndReturnEmptyList_When_userHasNoRcId() {
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(new User(), emptyList());

    this.deleteRocketChatAskerAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verifyNoInteractions(this.rocketChatService);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_userDeletionFailes()
      throws RocketChatDeleteUserException {
    User user = new User();
    user.setRcUserId("userId");
    doThrow(new RuntimeException()).when(this.rocketChatService).deleteUser(any());
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(user, new ArrayList<>());

    this.deleteRocketChatAskerAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(ROCKET_CHAT));
    assertThat(workflowErrors.get(0).getIdentifier(), is("userId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete Rocket.Chat user account"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }
}
