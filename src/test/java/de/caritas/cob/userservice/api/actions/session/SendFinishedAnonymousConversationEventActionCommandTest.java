package de.caritas.cob.userservice.api.actions.session;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.liveservice.generated.web.model.StatusSource.FinishConversationPhaseEnum;
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

  @InjectMocks private SendFinishedAnonymousConversationEventActionCommand actionCommand;

  @Mock private AuthenticatedUser authenticatedUser;

  @Mock private LiveEventNotificationService liveEventNotificationService;

  @ParameterizedTest
  @MethodSource("sessionsWithOnlyConsultantAndWithoutAnyUser")
  void execute_Should_useNoOtherServices_When_sessionHasNoUserOrOnlyConsultant(Session session) {
    this.actionCommand.execute(session);

    verifyNoMoreInteractions(this.authenticatedUser, this.liveEventNotificationService);
  }

  private static List<Session> sessionsWithOnlyConsultantAndWithoutAnyUser() {
    Session emptySession = new Session();
    Session onlyConsultantSession = new Session();
    onlyConsultantSession.setConsultant(new Consultant());

    return asList(emptySession, onlyConsultantSession);
  }

  @Test
  void execute_Should_triggerLiveEventWithStatusInProgressToUser_When_consultantWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.authenticatedUser.getUserId()).thenReturn(session.getConsultant().getId());

    this.actionCommand.execute(session);

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            singletonList(session.getUser().getUserId()), FinishConversationPhaseEnum.IN_PROGRESS);
  }

  @Test
  void execute_Should_triggerLiveEventWithStatusInProgressToConsultant_When_userWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.authenticatedUser.getUserId()).thenReturn(session.getUser().getUserId());

    this.actionCommand.execute(session);

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            singletonList(session.getConsultant().getId()),
            FinishConversationPhaseEnum.IN_PROGRESS);
  }

  @Test
  void execute_Should_triggerLiveEventWithStatusNewToUser_When_sessionHasOnlyUser() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);

    this.actionCommand.execute(session);

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            singletonList(session.getUser().getUserId()), FinishConversationPhaseEnum.NEW);
  }

  @Test
  void
      execute_Should_triggerLiveEventWithStatusInProgressToUserAndConsultant_When_systemWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.authenticatedUser.getUserId()).thenThrow(new RuntimeException(""));

    this.actionCommand.execute(session);

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            List.of(session.getConsultant().getId(), session.getUser().getUserId()),
            FinishConversationPhaseEnum.IN_PROGRESS);
  }
}
