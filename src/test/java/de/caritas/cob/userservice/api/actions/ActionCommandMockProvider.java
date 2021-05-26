package de.caritas.cob.userservice.api.actions;

import static org.mockito.Mockito.mock;

import de.caritas.cob.userservice.api.actions.registry.ActionContainer;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.actions.session.SendFinishedAnonymousConversationEventActionCommand;
import de.caritas.cob.userservice.api.actions.session.SetRocketChatRoomReadOnlyActionCommand;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ActionCommandMockProvider {

  private final Map<Class<? extends ActionCommand<Session>>, ActionCommand<Session>> sessionActionMocks = new HashMap<>();
  private final Map<Class<? extends ActionCommand<User>>, ActionCommand<User>> userActionMocks = new HashMap<>();

  public ActionCommandMockProvider() {
    addSessionMock(DeactivateSessionActionCommand.class);
    addSessionMock(SendFinishedAnonymousConversationEventActionCommand.class);
    addSessionMock(SetRocketChatRoomReadOnlyActionCommand.class);
    addUserMock(DeactivateKeycloakUserActionCommand.class);
  }

  private void addSessionMock(Class<? extends ActionCommand<Session>> classToMock) {
    sessionActionMocks.put(classToMock, mock(classToMock));
  }

  private void addUserMock(Class<? extends ActionCommand<User>> classToMock) {
    userActionMocks.put(classToMock, mock(classToMock));
  }

  public ActionCommand<Session> getSessionActionMock(
      Class<? extends ActionCommand<Session>> actionClass) {
    return this.sessionActionMocks.get(actionClass);
  }

  public ActionContainer<Session> getSessionActionContainer() {
    return new ActionContainer<>(new HashSet<>(this.sessionActionMocks.values()));
  }

  public ActionCommand<User> getUserActionMock(Class<? extends ActionCommand<User>> actionClass) {
    return this.userActionMocks.get(actionClass);
  }

  public ActionContainer<User> getUserActionContainer() {
    return new ActionContainer<>(new HashSet<>(this.userActionMocks.values()));
  }

}
