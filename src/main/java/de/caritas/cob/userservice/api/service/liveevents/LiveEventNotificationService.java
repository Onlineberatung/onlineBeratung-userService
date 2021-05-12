package de.caritas.cob.userservice.api.service.liveevents;

import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.ANONYMOUSENQUIRYACCEPTED;
import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.DIRECTMESSAGE;
import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.NEWANONYMOUSENQUIRY;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.PushMessageService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.userservice.liveservice.generated.web.model.EventType;
import de.caritas.cob.userservice.liveservice.generated.web.model.LiveEventMessage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Service class to provide live event triggers to the live service.
 */
@Service
@RequiredArgsConstructor
public class LiveEventNotificationService {

  private final @NonNull LiveControllerApi liveControllerApi;
  private final @NonNull UserIdsProviderFactory userIdsProviderFactory;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull PushMessageService pushMessageService;
  private final @NonNull UserService userService;

  private static final String RC_GROUP_ID_IDENTIFIER = "Rocket.Chat group ID: ";
  private static final String NEW_ANONYMOUS_ENQUIRY_IDENTIFIER = "Anonymous Enquiry ID: ";

  /**
   * Sends a anonymous enquiry accepted event to the live service,
   *
   * @param userId the id of the user who should receive the event
   */
  public void sendAcceptAnonymousEnquiryEventToUser(String userId) {
    if (isNotBlank(userId)) {
      var liveMessage = new LiveEventMessage()
          .eventType(ANONYMOUSENQUIRYACCEPTED);
      sendLiveEventMessage(singletonList(userId), liveMessage);
    }
  }

  private void sendLiveEventMessage(List<String> userIds, LiveEventMessage liveEventMessage) {
    try {
      this.liveControllerApi.sendLiveEvent(userIds, liveEventMessage);
    } catch (RestClientException e) {
      LogService.logInternalServerError(
          String.format("Unable to trigger live event to users %s with message %s",
              userIds, liveEventMessage), e);
    }
  }

  /**
   * Collects all relevant user or consultant ids of chats and sessions and sends a new
   * direct message to the live service.
   *
   * @param rcGroupId the rocket chat group id used to observe relevant users
   */
  public void sendLiveDirectMessageEventToUsers(String rcGroupId) {
    if (isNotBlank(rcGroupId)) {
      List<String> userIds = this.userIdsProviderFactory.byRocketChatGroup(rcGroupId)
          .collectUserIds(rcGroupId).stream()
          .filter(this::notInitiatingUser)
          .collect(Collectors.toList());
      triggerLiveEvent(userIds, DIRECTMESSAGE, RC_GROUP_ID_IDENTIFIER + rcGroupId);
      triggerDirectMessageLiveEvent(userIds);
      triggerMobilePushNotification(userIds);
    }
  }

  /**
   * Sends a new anonymous enquiry live event to the provided user IDs.
   *
   * @param userIds list of consultant user IDs
   * @param sessionId anonymous enquiry ID
   */
  public void sendLiveNewAnonymousEnquiryEventToUsers(List<String> userIds, Long sessionId) {
    triggerLiveEvent(userIds, NEWANONYMOUSENQUIRY, NEW_ANONYMOUS_ENQUIRY_IDENTIFIER + sessionId);
  }

  private void triggerLiveEvent(List<String> userIds, EventType eventType, String identifier) {
    if (isNotEmpty(userIds)) {
      try {
        var liveEventMessage = new LiveEventMessage()
            .eventType(eventType);
        this.liveControllerApi.sendLiveEvent(userIds, liveEventMessage);
      } catch (RestClientException e) {
        LogService.logInternalServerError(
            String.format("Unable to trigger %s live event for %s", eventType, identifier), e);
      }
    }
  }

  private void triggerDirectMessageLiveEvent(List<String> userIds) {
    if (isNotEmpty(userIds)) {
      var liveEventMessage = new LiveEventMessage()
          .eventType(DIRECTMESSAGE);
      sendLiveEventMessage(userIds, liveEventMessage);
    }
  }
  private boolean notInitiatingUser(String userId) {
    return !userId.equals(this.authenticatedUser.getUserId());
  }

  private void triggerMobilePushNotification(List<String> userIds) {
    userIds.forEach(this::sendPushNotificationForUser);
  }

  private void sendPushNotificationForUser(String userId) {
    this.userService.getUser(userId)
        .ifPresent(this::sendPushNotificationIfUserHasMobileToken);
  }

  private void sendPushNotificationIfUserHasMobileToken(User user) {
    if (isNotBlank(user.getMobileToken())) {
      this.pushMessageService.pushNewMessageEvent(user.getMobileToken());
    }
  }

}
