package de.caritas.cob.userservice.api.deleteworkflow.action.registry;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.LAST;
import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FIRST;
import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.FOURTH;
import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.SECOND;
import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.THIRD;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.deleteworkflow.action.DeleteKeycloakUserAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.DeleteRocketChatUserAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteAskerRoomsAndSessionsAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteDatabaseAskerAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteDatabaseAskerAgencyAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteChatAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteDatabaseConsultantAction;
import de.caritas.cob.userservice.api.deleteworkflow.action.consultant.DeleteDatabaseConsultantAgencyAction;
import de.caritas.cob.userservice.api.repository.chat.ChatRepository;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.repository.useragency.UserAgencyRepository;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class DeleteActionsRegistryTest {

  @InjectMocks
  private DeleteActionsRegistry deleteActionsRegistry;

  @Mock
  private ApplicationContext applicationContext;

  @Test
  public void getAskerDeleteActions_Should_returnEmptyList_When_noAskerDeleteActionIsRegistered() {
    List<DeleteAskerAction> deleteAskerActions = this.deleteActionsRegistry.getAskerDeleteActions();

    assertThat(deleteAskerActions, hasSize(0));
  }

  @Test
  public void getAskerDeleteActions_Should_returnDeleteActionsInExpectedOrder_When_askerDeleteActionAreRegistered() {
    List<DeleteAskerAction> deleteAskerActions = asList(
        new DeleteDatabaseAskerAction(mock(UserRepository.class)),
        new DeleteAskerRoomsAndSessionsAction(mock(SessionRepository.class), mock(
            SessionDataRepository.class), mock(MonitoringRepository.class),
            mock(RocketChatService.class)),
        new DeleteKeycloakUserAction(mock(KeycloakAdminClientService.class)),
        new DeleteRocketChatUserAction(mock(RocketChatService.class)),
        new DeleteDatabaseAskerAgencyAction(mock(UserAgencyRepository.class)));

    Map<String, Object> beans = deleteAskerActions.stream()
        .collect(Collectors.toMap(action -> action.getClass().getName(), action -> action));
    when(this.applicationContext.getBeansOfType(any())).thenReturn(beans);

    List<DeleteAskerAction> resultActions = this.deleteActionsRegistry.getAskerDeleteActions();

    assertThat(resultActions, hasSize(5));
    assertThat(resultActions.get(0).getOrder(), is(FIRST.getOrder()));
    assertThat(resultActions.get(1).getOrder(), is(SECOND.getOrder()));
    assertThat(resultActions.get(2).getOrder(), is(THIRD.getOrder()));
    assertThat(resultActions.get(3).getOrder(), is(FOURTH.getOrder()));
    assertThat(resultActions.get(4).getOrder(), is(LAST.getOrder()));
  }

  @Test
  public void getConsultantDeleteActions_Should_returnDeleteActionsInExpectedOrder_When_consultantDeleteActionAreRegistered() {
    List<DeleteConsultantAction> deleteConsultantActions = asList(
        new DeleteChatAction(mock(ChatRepository.class), mock(RocketChatService.class)),
        new DeleteDatabaseConsultantAction(mock(ConsultantRepository.class)),
        new DeleteDatabaseConsultantAgencyAction(mock(ConsultantAgencyRepository.class)),
        new DeleteRocketChatUserAction(mock(RocketChatService.class)),
        new DeleteKeycloakUserAction(mock(KeycloakAdminClientService.class)));

    Map<String, Object> beans = deleteConsultantActions.stream()
        .collect(Collectors.toMap(action -> action.getClass().getName(), action -> action));
    when(this.applicationContext.getBeansOfType(any())).thenReturn(beans);

    List<DeleteConsultantAction> resultActions = this.deleteActionsRegistry
        .getConsultantDeleteActions();

    assertThat(resultActions, hasSize(5));
    assertThat(resultActions.get(0).getOrder(), is(FIRST.getOrder()));
    assertThat(resultActions.get(1).getOrder(), is(SECOND.getOrder()));
    assertThat(resultActions.get(2).getOrder(), is(THIRD.getOrder()));
    assertThat(resultActions.get(3).getOrder(), is(FOURTH.getOrder()));
    assertThat(resultActions.get(4).getOrder(), is(LAST.getOrder()));
  }

}
