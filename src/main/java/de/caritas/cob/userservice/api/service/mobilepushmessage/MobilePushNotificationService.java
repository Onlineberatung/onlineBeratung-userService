package de.caritas.cob.userservice.api.service.mobilepushmessage;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantMobileToken;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserMobileToken;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Collects all relevant mobile tokens and fires push notifications via the {@link
 * FirebasePushMessageService}.
 */
@Service
@RequiredArgsConstructor
public class MobilePushNotificationService {

  private final @NonNull UserService userService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull FirebasePushMessageService firebasePushMessageService;

  /**
   * Triggers mobile push notifications to users who have a mobile device identifier.
   *
   * @param userIds user ids to send push notifications
   */
  public void triggerMobilePushNotification(List<String> userIds) {
    userIds.forEach(this::sendPushNotificationForUser);
  }

  private void sendPushNotificationForUser(String userId) {
    this.userService.getUser(userId).ifPresent(this::sendPushNotificationIfUserHasMobileTokens);
    this.consultantService
        .getConsultant(userId)
        .ifPresent(this::sendPushNotificationIfConsultantHasMobileTokens);
  }

  private void sendPushNotificationIfUserHasMobileTokens(User user) {
    Stream.concat(Stream.of(user.getMobileToken()), extractMobileAppTokens(user))
        .filter(StringUtils::isNotBlank)
        .forEach(this.firebasePushMessageService::pushNewMessageEvent);
  }

  private Stream<String> extractMobileAppTokens(User user) {
    if (isNotEmpty(user.getUserMobileTokens())) {
      return user.getUserMobileTokens().stream().map(UserMobileToken::getMobileAppToken);
    }
    return Stream.empty();
  }

  private void sendPushNotificationIfConsultantHasMobileTokens(Consultant consultant) {
    if (isNotEmpty(consultant.getConsultantMobileTokens())) {
      consultant.getConsultantMobileTokens().stream()
          .map(ConsultantMobileToken::getMobileAppToken)
          .filter(StringUtils::isNotBlank)
          .forEach(this.firebasePushMessageService::pushNewMessageEvent);
    }
  }
}
