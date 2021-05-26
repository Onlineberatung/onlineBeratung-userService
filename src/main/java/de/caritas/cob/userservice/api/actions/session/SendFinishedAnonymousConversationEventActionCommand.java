package de.caritas.cob.userservice.api.actions.session;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to send a live finished anonymous conversation event.
 */
@Component
@RequiredArgsConstructor
public class SendFinishedAnonymousConversationEventActionCommand implements
    ActionCommand<Session> {

  private final @NonNull LiveEventNotificationService liveEventNotificationService;
  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Sends a finished anonymous conversation event.
   *
   * @param session the session
   */
  @Override
  public void execute(Session session) {
    List<String> userIdsToSendLiveEvent = collectNotInitiatingUser(session);

    if (isNotEmpty(userIdsToSendLiveEvent)) {
      this.liveEventNotificationService
          .sendLiveFinishedAnonymousConversationToUsers(userIdsToSendLiveEvent);
    }
  }

  private List<String> collectNotInitiatingUser(Session session) {
    if (doesSessionHaveConsultantAndUser(session)) {
      return Stream.of(session.getConsultant().getId(), session.getUser().getUserId())
          .filter(userId -> !this.authenticatedUser.getUserId().equals(userId))
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  private boolean doesSessionHaveConsultantAndUser(Session session) {
    return nonNull(session.getConsultant()) && nonNull(session.getUser());
  }

}
