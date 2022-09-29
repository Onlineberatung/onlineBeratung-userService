package de.caritas.cob.userservice.api.service.liveevents;

import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.ANONYMOUSCONVERSATIONFINISHED;
import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.ANONYMOUSENQUIRYACCEPTED;
import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.DIRECTMESSAGE;
import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.NEWANONYMOUSENQUIRY;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.service.mobilepushmessage.MobilePushNotificationService;
import de.caritas.cob.userservice.liveservice.generated.ApiException;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.userservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.userservice.liveservice.generated.web.model.StatusSource;
import de.caritas.cob.userservice.liveservice.generated.web.model.StatusSource.FinishConversationPhaseEnum;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service class to provide live event triggers to the live service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveEventNotificationService {

  private final @NonNull LiveControllerApi liveControllerApi;
  private final @NonNull UserIdsProviderFactory userIdsProviderFactory;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull MobilePushNotificationService mobilePushNotificationService;

  private static final String RC_GROUP_ID_MESSAGE_TEMPLATE = "Rocket.Chat group ID: %s";
  private static final String NEW_ANONYMOUS_ENQUIRY_MESSAGE_TEMPLATE = "Anonymous Enquiry ID: %s";

  /**
   * Sends a anonymous enquiry accepted event to the live service,
   *
   * @param userId the id of the user who should receive the event
   */
  public void sendAcceptAnonymousEnquiryEventToUser(String userId) {
    if (isNotBlank(userId)) {
      var liveEventMessage =
          new LiveEventMessage().eventType(ANONYMOUSENQUIRYACCEPTED).userIds(singletonList(userId));
      sendLiveEventMessage(liveEventMessage);
    }
  }

  private void sendLiveEventMessage(LiveEventMessage liveEventMessage) {
    sendLiveEventMessage(
        liveEventMessage,
        () -> String.format("Unable to trigger live event message %s", liveEventMessage));
  }

  private void sendLiveEventMessage(
      LiveEventMessage liveEventMessage, Supplier<String> errorMessageSupplier) {
    try {
      this.liveControllerApi.sendLiveEvent(liveEventMessage);
    } catch (ApiException e) {
      log.error("Internal Server Error: {}", errorMessageSupplier.get(), e);
    }
  }

  /**
   * Collects all relevant user or consultant ids of chats and sessions and sends a new direct
   * message to the live service.
   *
   * @param rcGroupId the rocket chat group id used to observe relevant users
   */
  public void sendLiveDirectMessageEventToUsers(String rcGroupId) {
    if (isNotBlank(rcGroupId)) {
      var userIds =
          this.userIdsProviderFactory
              .byRocketChatGroup(rcGroupId)
              .collectUserIds(rcGroupId)
              .stream()
              .filter(this::notInitiatingUser)
              .collect(Collectors.toList());

      triggerDirectMessageLiveEvent(userIds, rcGroupId);
      this.mobilePushNotificationService.triggerMobilePushNotification(userIds);
    }
  }

  private boolean notInitiatingUser(String userId) {
    return !userId.equals(this.authenticatedUser.getUserId());
  }

  private void triggerDirectMessageLiveEvent(List<String> userIds, String rcGroupId) {
    if (isNotEmpty(userIds)) {
      var liveEventMessage = new LiveEventMessage().eventType(DIRECTMESSAGE).userIds(userIds);

      sendLiveEventMessage(
          liveEventMessage,
          () -> {
            var rcMessage = String.format(RC_GROUP_ID_MESSAGE_TEMPLATE, rcGroupId);
            return makeUserIdsEventTypeMessage(liveEventMessage, rcMessage);
          });
    }
  }

  private String makeUserIdsEventTypeMessage(
      LiveEventMessage triggeredLiveEventMessage, String withMessage) {
    return String.format(
        "Unable to trigger %s live event message %s",
        triggeredLiveEventMessage.getEventType(), withMessage);
  }

  /**
   * Sends a new anonymous enquiry live event to the provided user IDs.
   *
   * @param userIds list of consultant user IDs
   * @param sessionId anonymous enquiry ID
   */
  public void sendLiveNewAnonymousEnquiryEventToUsers(List<String> userIds, Long sessionId) {
    if (isNotEmpty(userIds)) {
      var liveEventMessage = new LiveEventMessage().eventType(NEWANONYMOUSENQUIRY).userIds(userIds);

      sendLiveEventMessage(
          liveEventMessage,
          () -> {
            var anonymousEnquiryMessage =
                String.format(NEW_ANONYMOUS_ENQUIRY_MESSAGE_TEMPLATE, sessionId);
            return makeUserIdsEventTypeMessage(liveEventMessage, anonymousEnquiryMessage);
          });
    }
  }

  /**
   * Sends a anonymous conversation finished live event to the provided user IDs.
   *
   * @param userIds list of consultant user IDs
   */
  public void sendLiveFinishedAnonymousConversationToUsers(
      List<String> userIds, FinishConversationPhaseEnum finishConversationPhase) {
    if (isNotEmpty(userIds)) {
      var liveEventMessage =
          new LiveEventMessage()
              .eventType(ANONYMOUSCONVERSATIONFINISHED)
              .eventContent(new StatusSource().finishConversationPhase(finishConversationPhase))
              .userIds(userIds);

      sendLiveEventMessage(liveEventMessage);
    }
  }
}
