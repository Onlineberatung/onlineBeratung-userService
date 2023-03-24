package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.DATABASE;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionDataRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import java.util.ArrayList;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeleteSingleRoomAndSessionActionTest {

  @InjectMocks private DeleteSingleRoomAndSessionAction deleteSingleRoomAndSessionAction;

  @Mock private SessionRepository sessionRepository;

  @Mock private SessionDataRepository sessionDataRepository;

  @Mock private RocketChatService rocketChatService;

  @Mock private Logger logger;

  @Before
  public void setup() {
    setInternalState(DeleteSingleRoomAndSessionAction.class, "log", logger);
  }

  @Test
  public void
      execute_Should_returnEmptyListAndPerformAllDeletions_When_userSessionIsDeletedSuccessful()
          throws Exception {
    Session session = new EasyRandom().nextObject(Session.class);
    SessionDeletionWorkflowDTO workflowDTO = new SessionDeletionWorkflowDTO(session, emptyList());

    this.deleteSingleRoomAndSessionAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
    verify(this.rocketChatService, times(2)).deleteGroupAsTechnicalUser(any());
    verify(this.sessionDataRepository, times(1)).findBySessionId(session.getId());
    verify(this.sessionDataRepository, times(1)).deleteAll(any());
    verify(this.sessionRepository, times(1)).delete(session);
  }

  @Test
  public void
      execute_Should_returnExpectedWorkflowErrors_When_noUserSessionDeletedStepIsSuccessful()
          throws Exception {
    Session session = new EasyRandom().nextObject(Session.class);
    doThrow(new RocketChatDeleteGroupException(new RuntimeException()))
        .when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());
    doThrow(new RuntimeException()).when(this.sessionDataRepository).deleteAll(any());
    doThrow(new RuntimeException()).when(this.sessionRepository).delete(any());
    SessionDeletionWorkflowDTO workflowDTO =
        new SessionDeletionWorkflowDTO(session, new ArrayList<>());

    this.deleteSingleRoomAndSessionAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(4));
    verify(logger, times(4)).error(anyString(), any(Exception.class));
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrors_When_rocketChatDeletionFails()
      throws Exception {
    Session session = new EasyRandom().nextObject(Session.class);
    doThrow(new RocketChatDeleteGroupException(new RuntimeException()))
        .when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());
    SessionDeletionWorkflowDTO workflowDTO =
        new SessionDeletionWorkflowDTO(session, new ArrayList<>());

    this.deleteSingleRoomAndSessionAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(2));
    verify(logger, times(2)).error(anyString(), any(RocketChatDeleteGroupException.class));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(ROCKET_CHAT));
    assertThat(workflowErrors.get(0).getIdentifier(), is(session.getGroupId()));
    assertThat(workflowErrors.get(0).getReason(), is("Deletion of Rocket.Chat group failed"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    assertThat(workflowErrors.get(1).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(1).getDeletionTargetType(), is(ROCKET_CHAT));
    assertThat(workflowErrors.get(1).getIdentifier(), is(session.getFeedbackGroupId()));
    assertThat(workflowErrors.get(1).getReason(), is("Deletion of Rocket.Chat group failed"));
    assertThat(workflowErrors.get(1).getTimestamp(), notNullValue());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowError_When_sessionDataDeletionFails() {
    Session session = new EasyRandom().nextObject(Session.class);
    doThrow(new RuntimeException()).when(this.sessionDataRepository).deleteAll(any());
    SessionDeletionWorkflowDTO workflowDTO =
        new SessionDeletionWorkflowDTO(session, new ArrayList<>());

    this.deleteSingleRoomAndSessionAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    verify(logger).error(anyString(), any(RuntimeException.class));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is(session.getId().toString()));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete session data from session"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowError_When_sessionDeletionFails() {
    Session session = new EasyRandom().nextObject(Session.class);
    doThrow(new RuntimeException()).when(this.sessionRepository).delete(any());
    SessionDeletionWorkflowDTO workflowDTO =
        new SessionDeletionWorkflowDTO(session, new ArrayList<>());

    this.deleteSingleRoomAndSessionAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    verify(logger).error(anyString(), any(RuntimeException.class));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is(session.getId().toString()));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete session"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
  }
}
