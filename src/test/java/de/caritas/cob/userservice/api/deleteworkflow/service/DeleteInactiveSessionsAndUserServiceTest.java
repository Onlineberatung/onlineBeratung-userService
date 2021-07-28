package de.caritas.cob.userservice.api.deleteworkflow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.deleteworkflow.service.provider.InactivePrivateGroupsProvider;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteInactiveSessionsAndUserServiceTest {

  @InjectMocks
  private DeleteInactiveSessionsAndUserService deleteInactiveSessionsAndUserService;

  @Mock
  private WorkflowErrorMailService workflowErrorMailService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private DeleteUserAccountService deleteUserAccountService;
  @Mock
  private DeleteSessionService deleteSessionService;
  @Mock
  private InactivePrivateGroupsProvider inactivePrivateGroupsProvider;

  @Test
  public void deleteInactiveSessionsAndUsers_Should_SendWorkflowErrorsMail() {

    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>() {{
        put(user.getUserId(), Collections.singletonList(session.getGroupId()));
      }};
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Collections.singletonList(session));
    DeletionWorkflowError deletionWorkflowError = Mockito.mock(DeletionWorkflowError.class);
    when(deleteUserAccountService.performUserDeletion(user))
        .thenReturn(Collections.singletonList(deletionWorkflowError));

    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    verify(workflowErrorMailService, Mockito.times(1)).buildAndSendErrorMail(any());
  }

  @Test
  public void deleteInactiveSessionsAndUsers_Should_DeleteEntireUserAccount_WhenUserHasOnlyInactiveSessions() {

    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>() {{
        put(user.getUserId(), Arrays.asList(session1.getGroupId(), session2.getGroupId()));
      }};
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session1, session2));

    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    verify(deleteUserAccountService, Mockito.times(1)).performUserDeletion(user);
  }

  @Test
  public void deleteInactiveSessionsAndUsers_Should_DeleteSingleSession_WhenUserHasActiveAndInactiveSessions() {

    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>() {{
        put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
      }};
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session1, session2));

    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    verify(deleteSessionService, Mockito.times(1)).performSessionDeletion(session1);
  }

  @Test
  public void deleteInactiveSessionsAndUsers_Should_SendWorkflowErrorMail_WhenUserHasActiveAndInactiveSessionsAndHasErrors() {

    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>() {{
        put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
      }};
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session1, session2));
    DeletionWorkflowError deletionWorkflowError = Mockito.mock(DeletionWorkflowError.class);
    when(deleteSessionService.performSessionDeletion(session1))
        .thenReturn(Collections.singletonList(deletionWorkflowError));

    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    verify(workflowErrorMailService, Mockito.times(1)).buildAndSendErrorMail(any());
  }

  @Test
  public void deleteInactiveSessionsAndUsers_Should_SendWorkflowErrorMail_WhenSessionCouldNotBeFound() {

    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Session session3 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>() {{
        put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
      }};
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session2, session3));

    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    verify(workflowErrorMailService, Mockito.times(1)).buildAndSendErrorMail(any());
  }

  @Test
  public void deleteInactiveSessionsAndUsers_Should_SendWorkflowErrorMail_WhenUserCouldNotBeFound() {

    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>() {{
        put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
      }};
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.empty());

    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    verify(workflowErrorMailService, Mockito.times(1)).buildAndSendErrorMail(any());
  }
}
