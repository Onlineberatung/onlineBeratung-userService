package de.caritas.cob.userservice.api.conversation.facade;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinishAnonymousConversationFacadeTest {

  @InjectMocks
  private FinishAnonymousConversationFacade finishAnonymousConversationFacade;

  @Mock
  private LiveEventNotificationService liveEventNotificationService;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Mock
  private SessionService sessionService;

  @Test
  void finishConversation_Should_throwNotFoundException_When_sessionDoesNotExist() {
    when(this.sessionService.getSession(any())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> this.finishAnonymousConversationFacade.finishConversation(1L));
  }

  @Test
  void finishConversation_Should_sendLiveEventToConsultant_When_userWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionService.getSession(any())).thenReturn(Optional.of(session));
    when(this.authenticatedUser.getUserId()).thenReturn(session.getUser().getUserId());

    this.finishAnonymousConversationFacade.finishConversation(session.getId());

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            singletonList(session.getConsultant().getId()));
  }

  @Test
  void finishConversation_Should_sendLiveEventToUser_When_consultantWasInitiator() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionService.getSession(any())).thenReturn(Optional.of(session));
    when(this.authenticatedUser.getUserId()).thenReturn(session.getConsultant().getId());

    this.finishAnonymousConversationFacade.finishConversation(session.getId());

    verify(this.liveEventNotificationService, times(1))
        .sendLiveFinishedAnonymousConversationToUsers(
            singletonList(session.getUser().getUserId()));
  }

}
