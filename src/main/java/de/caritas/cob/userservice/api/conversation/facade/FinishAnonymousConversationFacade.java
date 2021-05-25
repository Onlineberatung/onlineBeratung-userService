package de.caritas.cob.userservice.api.conversation.facade;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinishAnonymousConversationFacade {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;
  private final @NonNull SessionService sessionService;

  /**
   * Finishes the anonymous session with given id.
   *
   * @param sessionId the session id
   */
  public void finishConversation(Long sessionId) {
    var session = this.sessionService.getSession(sessionId)
        .orElseThrow(() -> new NotFoundException(
            String.format("Session with id %s does not exist", sessionId)));

    sendFinishedEventToUser(session);
  }

  private void sendFinishedEventToUser(Session session) {
    List<String> userIdsToSendLiveEvent = collectNotInitiatingUser(session);

    this.liveEventNotificationService
        .sendLiveFinishedAnonymousConversationToUsers(userIdsToSendLiveEvent);
  }

  private List<String> collectNotInitiatingUser(Session session) {
    return Stream.of(session.getConsultant().getId(), session.getUser().getUserId())
        .filter(userId -> !this.authenticatedUser.getUserId().equals(userId))
        .collect(Collectors.toList());
  }

}
