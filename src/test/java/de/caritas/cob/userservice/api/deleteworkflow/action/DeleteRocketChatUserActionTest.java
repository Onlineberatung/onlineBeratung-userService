package de.caritas.cob.userservice.api.deleteworkflow.action;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FOURTH;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
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

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeleteRocketChatUserActionTest {

  @InjectMocks
  private DeleteRocketChatUserAction deleteRocketChatUserAction;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void getOrder_Should_returnFourth() {
    assertThat(deleteRocketChatUserAction.getOrder(), is(FOURTH.getOrder()));
  }

  @Test
  public void execute_Should_deleteRocketChatUserAndReturnEmptyList_When_userDeletionIsSuccessful()
      throws RocketChatDeleteUserException {
    User user = new User();
    user.setRcUserId("rcId");
    List<DeletionWorkflowError> workflowErrors = this.deleteRocketChatUserAction.execute(user);

    assertThat(workflowErrors, hasSize(0));
    verify(this.rocketChatService, times(1)).deleteUser(any());
  }

  @Test
  public void execute_Should_notDeleteRocketChatUserAndReturnEmptyList_When_userHasNoRcId() {
    List<DeletionWorkflowError> workflowErrors = this.deleteRocketChatUserAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(0));
    verifyNoInteractions(this.rocketChatService);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_userDeletionFailes()
      throws RocketChatDeleteUserException {
    User user = new User();
    user.setRcUserId("userId");
    doThrow(new RuntimeException()).when(this.rocketChatService).deleteUser(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteRocketChatUserAction.execute(user);

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(ROCKET_CHAT));
    assertThat(workflowErrors.get(0).getIdentifier(), is("userId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete Rocket.Chat user account"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

  @Test
  public void execute_Should_deleteRocketChatUserAndReturnEmptyList_When_consultantDeletionIsSuccessful()
      throws RocketChatDeleteUserException {
    Consultant consultant = new Consultant();
    consultant.setRocketChatId("rcId");
    List<DeletionWorkflowError> workflowErrors = this.deleteRocketChatUserAction
        .execute(consultant);

    assertThat(workflowErrors, hasSize(0));
    verify(this.rocketChatService, times(1)).deleteUser(any());
  }

  @Test
  public void execute_Should_notDeleteRocketChatUserAndReturnEmptyList_When_consultantHasNoRcId() {
    List<DeletionWorkflowError> workflowErrors = this.deleteRocketChatUserAction
        .execute(new Consultant());

    assertThat(workflowErrors, hasSize(0));
    verifyNoInteractions(this.rocketChatService);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_consultantDeletionFailes()
      throws RocketChatDeleteUserException {
    Consultant consultant = new Consultant();
    consultant.setRocketChatId("consultantId");
    doThrow(new RuntimeException()).when(this.rocketChatService).deleteUser(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteRocketChatUserAction
        .execute(consultant);

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(ROCKET_CHAT));
    assertThat(workflowErrors.get(0).getIdentifier(), is("consultantId"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete Rocket.Chat user account"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

}
