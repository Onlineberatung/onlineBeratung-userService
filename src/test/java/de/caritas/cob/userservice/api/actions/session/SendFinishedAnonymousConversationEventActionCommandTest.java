package de.caritas.cob.userservice.api.actions.session;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendFinishedAnonymousConversationEventActionCommandTest {

  @InjectMocks
  private SendFinishedAnonymousConversationEventActionCommand actionCommand;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Mock
  private LiveEventNotificationService liveEventNotificationService;

  @ParameterizedTest
  @MethodSource("sessionsWithOnlyOneAndWithoutUser")
  void execute_Should_useNoOtherServices_When_sessionHasNotUserAndConsultant(Session session) {
    this.actionCommand.execute(session);

    verifyNoMoreInteractions(this.authenticatedUser, this.liveEventNotificationService);
  }

  private static List<Session> sessionsWithOnlyOneAndWithoutUser() {
    Session emptySession = new Session();
    Session onlyUserSession = new Session();
    onlyUserSession.setUser(new User());
    Session onlyConsultantSession = new Session();
    onlyConsultantSession.setConsultant(new Consultant());

    return asList(emptySession, onlyUserSession, onlyConsultantSession);
  }

  @Test
  void execute_Should_triggerLiveEventToUser_When_consultantWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.authenticatedUser.getUserId()).thenReturn(session.getConsultant().getId());

    this.actionCommand.execute(session);

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(singletonList(session.getUser().getUserId()));
  }

  @Test
  void execute_Should_triggerLiveEventToConsultant_When_userWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.authenticatedUser.getUserId()).thenReturn(session.getUser().getUserId());

    this.actionCommand.execute(session);

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            singletonList(session.getConsultant().getId()));
  }

}
