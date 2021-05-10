package de.caritas.cob.userservice.api.service.liveevents;

import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.DIRECTMESSAGE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.PushMessageService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
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

  /**
   * Collects all relevant user or consultant ids of chats and sessions and sends a new
   * directmessage to the live service.
   *
   * @param rcGroupId the rocket chat group id used to observe relevant users
   */
  public void sendLiveDirectMessageEventToUsers(String rcGroupId) {
    if (isNotBlank(rcGroupId)) {
      List<String> userIds = this.userIdsProviderFactory.byRocketChatGroup(rcGroupId)
          .collectUserIds(rcGroupId).stream()
          .filter(this::notInitiatingUser)
          .collect(Collectors.toList());
      triggerLiveEvent(rcGroupId, userIds);
      triggerMobilePushNotification(userIds);
    }
  }

  private void triggerLiveEvent(String rcGroupId, List<String> userIds) {
    if (isNotEmpty(userIds)) {
      try {
        LiveEventMessage liveEventMessage = new LiveEventMessage()
            .eventType(DIRECTMESSAGE);
        this.liveControllerApi.sendLiveEvent(userIds, liveEventMessage);
      } catch (RestClientException e) {
        LogService.logInternalServerError(
            String.format("Unable to trigger live event for rocket chat group id %s",
                rcGroupId), e);
      }
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
