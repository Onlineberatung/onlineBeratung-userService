package de.caritas.cob.userservice.api.actions.session;

import static de.caritas.cob.userservice.liveservice.generated.web.model.StatusSource.FinishConversationPhaseEnum.IN_PROGRESS;
import static de.caritas.cob.userservice.liveservice.generated.web.model.StatusSource.FinishConversationPhaseEnum.NEW;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.liveservice.generated.web.model.StatusSource.FinishConversationPhaseEnum;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Action to send a live finished anonymous conversation event. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SendFinishedAnonymousConversationEventActionCommand implements ActionCommand<Session> {

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
      try {
        this.liveEventNotificationService.sendLiveFinishedAnonymousConversationToUsers(
            userIdsToSendLiveEvent, forSession(session));
      } catch (Exception e) {
        log.error("Unable to send anonymous conversation finished live event");
        log.error(getStackTrace(e));
      }
    }
  }

  private List<String> collectNotInitiatingUser(Session session) {
    if (hasSessionOnlyUser(session)) {
      return singletonList(session.getUser().getUserId());
    }
    if (doesSessionHaveConsultantAndUser(session)) {
      return obtainNotInitiatingUsers(session);
    }
    return emptyList();
  }

  private boolean hasSessionOnlyUser(Session session) {
    return isNull(session.getConsultant()) && nonNull(session.getUser());
  }

  private boolean doesSessionHaveConsultantAndUser(Session session) {
    return nonNull(session.getConsultant()) && nonNull(session.getUser());
  }

  private List<String> obtainNotInitiatingUsers(Session session) {
    return Stream.of(session.getConsultant().getId(), session.getUser().getUserId())
        .filter(this::notInitiatingUser)
        .collect(Collectors.toList());
  }

  private boolean notInitiatingUser(String userId) {
    try {
      return !userId.equals(this.authenticatedUser.getUserId());
    } catch (Exception e) {
      return true;
    }
  }

  private FinishConversationPhaseEnum forSession(Session session) {
    return isNull(session.getConsultant()) ? NEW : IN_PROGRESS;
  }
}
