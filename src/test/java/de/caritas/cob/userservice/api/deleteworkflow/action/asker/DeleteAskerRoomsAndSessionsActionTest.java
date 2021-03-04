package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.SECOND;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
import static java.util.Collections.singletonList;
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
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeleteAskerRoomsAndSessionsActionTest {

  @InjectMocks
  private DeleteAskerRoomsAndSessionsAction deleteAskerRoomsAndSessionsAction;

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private SessionDataRepository sessionDataRepository;

  @Mock
  private MonitoringRepository monitoringRepository;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void getOrder_Should_returnSecond() {
    assertThat(this.deleteAskerRoomsAndSessionsAction.getOrder(), is(SECOND.getOrder()));
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformNoDeletions_When_userHasNoSession() {
    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.sessionDataRepository, this.monitoringRepository,
        this.rocketChatService, this.logger);
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformAllDeletions_When_userSessionIsDeletedSuccessful()
      throws Exception {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionRepository.findByUser(any())).thenReturn(singletonList(session));

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
    verify(this.rocketChatService, times(2)).deleteGroupAsTechnicalUser(any());
    verify(this.monitoringRepository, times(1)).findBySessionId(session.getId());
    verify(this.monitoringRepository, times(1)).deleteAll(any());
    verify(this.sessionDataRepository, times(1)).findBySessionId(session.getId());
    verify(this.sessionDataRepository, times(1)).deleteAll(any());
    verify(this.sessionRepository, times(1)).delete(session);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrors_When_noUserSessionDeletedStepIsSuccessful()
      throws Exception {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionRepository.findByUser(any())).thenReturn(singletonList(session));
    doThrow(new RocketChatDeleteGroupException(new RuntimeException())).when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());
    doThrow(new RuntimeException()).when(this.monitoringRepository).deleteAll(any());
    doThrow(new RuntimeException()).when(this.sessionDataRepository).deleteAll(any());
    doThrow(new RuntimeException()).when(this.sessionRepository).delete(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(5));
    verify(this.logger, times(5)).error(anyString(), anyString());
  }

  @Test
  public void execute_Should_returnExpectedAmountOfWorkflowErrors_When_manySessionDeletionsFailed()
      throws Exception {
    List<Session> sessions = new EasyRandom().objects(Session.class, 3)
        .collect(Collectors.toList());
    when(this.sessionRepository.findByUser(any())).thenReturn(sessions);
    doThrow(new RocketChatDeleteGroupException(new RuntimeException())).when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());
    doThrow(new RuntimeException()).when(this.monitoringRepository).deleteAll(any());
    doThrow(new RuntimeException()).when(this.sessionDataRepository).deleteAll(any());
    doThrow(new RuntimeException()).when(this.sessionRepository).delete(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(15));
    verify(this.logger, times(15)).error(anyString(), anyString());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrors_When_rocketChatDeletionFails()
      throws Exception {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionRepository.findByUser(any())).thenReturn(singletonList(session));
    doThrow(new RocketChatDeleteGroupException(new RuntimeException())).when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(2));
    verify(this.logger, times(2)).error(anyString(), anyString());
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
  public void execute_Should_returnExpectedWorkflowError_When_monitoringDeletionFails() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionRepository.findByUser(any())).thenReturn(singletonList(session));
    doThrow(new RuntimeException()).when(this.monitoringRepository).deleteAll(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(1));
    verify(this.logger, times(1)).error(anyString(), anyString());
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is(session.getId().toString()));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete monitorings from session"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowError_When_sessionDataDeletionFails() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionRepository.findByUser(any())).thenReturn(singletonList(session));
    doThrow(new RuntimeException()).when(this.sessionDataRepository).deleteAll(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(1));
    verify(this.logger, times(1)).error(anyString(), anyString());
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is(session.getId().toString()));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete session data from session"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
  }

  @Test
  public void execute_Should_returnExpectedWorkflowError_When_sessionDeletionFails() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionRepository.findByUser(any())).thenReturn(singletonList(session));
    doThrow(new RuntimeException()).when(this.sessionRepository).delete(any());

    List<DeletionWorkflowError> workflowErrors = this.deleteAskerRoomsAndSessionsAction
        .execute(new User());

    assertThat(workflowErrors, hasSize(1));
    verify(this.logger, times(1)).error(anyString(), anyString());
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is(session.getId().toString()));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete session"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
  }

}
