package de.caritas.cob.userservice.api.service.mobilepushmessage;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantMobileToken;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserMobileToken;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MobilePushNotificationServiceTest {

  @InjectMocks private MobilePushNotificationService mobilePushNotificationService;

  @Mock private UserService userService;

  @Mock private ConsultantService consultantService;

  @Mock private FirebasePushMessageService firebasePushMessageService;

  @Test
  void sendLiveDirectMessageEventToUsers_Should_sendPushMessage_When_usersHaveMobileToken() {
    var user = new User();
    user.setMobileToken("mobileToken");
    when(this.userService.getUser(anyString())).thenReturn(Optional.of(user));

    this.mobilePushNotificationService.triggerMobilePushNotification(asList("1", "2"));

    verify(this.firebasePushMessageService, times(2)).pushNewMessageEvent("mobileToken");
  }

  @Test
  void sendLiveDirectMessageEventToUsers_Should_sendPushMessageOnlyToUsersWithMobileToken() {
    var user = new User();
    user.setMobileToken("mobileToken");
    when(this.userService.getUser("1")).thenReturn(Optional.of(user));
    when(this.userService.getUser("2")).thenReturn(Optional.of(new User()));

    this.mobilePushNotificationService.triggerMobilePushNotification(asList("1", "2"));

    verify(this.firebasePushMessageService, times(1)).pushNewMessageEvent("mobileToken");
  }

  @Test
  void sendLiveDirectMessageEventToUsers_Should_notSendPushMessage_When_noUserHasMobileToken() {
    when(this.userService.getUser(any())).thenReturn(Optional.of(new User()));

    this.mobilePushNotificationService.triggerMobilePushNotification(asList("1", "2"));

    verifyNoInteractions(this.firebasePushMessageService);
  }

  @Test
  void sendLiveDirectMessageEventToUsers_Should_notSendPushMessage_When_userIdsAreEmpty() {
    this.mobilePushNotificationService.triggerMobilePushNotification(emptyList());

    verifyNoInteractions(this.firebasePushMessageService);
  }

  @Test
  void
      sendLiveDirectMessageEventToUsers_Should_sendPushMessageToAllDevices_When_usersHaveMultipleMobileTokensIncludingU25MobileTOken() {
    var user = new EasyRandom().nextObject(User.class);
    var allUserMobileTokens =
        Stream.concat(
                Stream.of(user.getMobileToken()),
                user.getUserMobileTokens().stream().map(UserMobileToken::getMobileAppToken))
            .collect(Collectors.toList());
    when(this.userService.getUser(anyString())).thenReturn(Optional.of(user));

    this.mobilePushNotificationService.triggerMobilePushNotification(List.of("user id"));

    verify(this.firebasePushMessageService, times(allUserMobileTokens.size()))
        .pushNewMessageEvent(anyString());
  }

  @Test
  void
      sendLiveDirectMessageEventToUsers_Should_sendPushMessageToAllDevices_When_usersHaveMultipleMobileTokens() {
    var user = new EasyRandom().nextObject(User.class);
    user.setMobileToken(null);
    var allUserMobileTokens =
        user.getUserMobileTokens().stream()
            .map(UserMobileToken::getMobileAppToken)
            .collect(Collectors.toList());
    when(this.userService.getUser(anyString())).thenReturn(Optional.of(user));

    this.mobilePushNotificationService.triggerMobilePushNotification(List.of("user id"));

    verify(this.firebasePushMessageService, times(allUserMobileTokens.size()))
        .pushNewMessageEvent(anyString());
  }

  @Test
  void sendLiveDirectMessageEventToUsers_Should_sendPushMessage_When_consultantsHaveMobileToken() {
    var consultant = new Consultant();
    var consultantMobileToken = new ConsultantMobileToken();
    consultantMobileToken.setMobileAppToken("mobileToken");
    consultant.setConsultantMobileTokens(Set.of(consultantMobileToken));
    when(this.consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));

    this.mobilePushNotificationService.triggerMobilePushNotification(asList("1", "2"));

    verify(this.firebasePushMessageService, times(2)).pushNewMessageEvent("mobileToken");
  }

  @Test
  void sendLiveDirectMessageEventToUsers_Should_sendPushMessageOnlyToConsultantsWithMobileToken() {
    var consultant = new Consultant();
    var consultantMobileToken = new ConsultantMobileToken();
    consultantMobileToken.setMobileAppToken("mobileToken");
    consultant.setConsultantMobileTokens(Set.of(consultantMobileToken));
    when(this.consultantService.getConsultant("1")).thenReturn(Optional.of(consultant));
    when(this.consultantService.getConsultant("2")).thenReturn(Optional.of(new Consultant()));

    this.mobilePushNotificationService.triggerMobilePushNotification(asList("1", "2"));

    verify(this.firebasePushMessageService, times(1)).pushNewMessageEvent("mobileToken");
  }

  @Test
  void
      sendLiveDirectMessageEventToUsers_Should_notSendPushMessage_When_noConsultantHasMobileToken() {
    when(this.consultantService.getConsultant(any())).thenReturn(Optional.of(new Consultant()));

    this.mobilePushNotificationService.triggerMobilePushNotification(asList("1", "2"));

    verifyNoInteractions(this.firebasePushMessageService);
  }

  @Test
  void
      sendLiveDirectMessageEventToUsers_Should_sendPushMessageToAllDevices_When_consultantsHaveMultipleMobileTokens() {
    var consultant = new EasyRandom().nextObject(Consultant.class);
    var allConsultantMobileTokens =
        consultant.getConsultantMobileTokens().stream()
            .map(ConsultantMobileToken::getMobileAppToken)
            .collect(Collectors.toList());
    when(this.consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));

    this.mobilePushNotificationService.triggerMobilePushNotification(List.of("consultant id"));

    verify(this.firebasePushMessageService, times(allConsultantMobileTokens.size()))
        .pushNewMessageEvent(anyString());
  }
}
