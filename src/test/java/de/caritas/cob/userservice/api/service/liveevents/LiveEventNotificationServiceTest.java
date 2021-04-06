package de.caritas.cob.userservice.api.service.liveevents;


import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.DIRECTMESSAGE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.PushMessageService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.userservice.liveservice.generated.web.model.LiveEventMessage;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.web.client.RestClientException;

@RunWith(MockitoJUnitRunner.class)
public class LiveEventNotificationServiceTest {

  private static final LiveEventMessage MESSAGE = new LiveEventMessage().eventType(DIRECTMESSAGE);

  @InjectMocks
  private LiveEventNotificationService liveEventNotificationService;

  @Mock
  private LiveControllerApi liveControllerApi;

  @Mock
  private UserIdsProviderFactory userIdsProviderFactory;

  @Mock
  private RelevantUserAccountIdsBySessionProvider bySessionProvider;

  @Mock
  private RelevantUserAccountIdsByChatProvider byChatProvider;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Mock
  private UserService userService;

  @Mock
  private PushMessageService pushMessageService;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_callFactoryAndLiveApi_When_rcGroupIdIsValid() {
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(asList("1", "2"));
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("valid");

    verify(userIdsProviderFactory, times(1)).byRocketChatGroup("valid");
    verify(liveControllerApi, times(1))
        .sendLiveEvent(eq(asList("1", "2")), eq(MESSAGE));
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_doNothing_When_rcGroupIdIsEmpty() {
    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("");

    verifyNoInteractions(userIdsProviderFactory);
    verifyNoInteractions(liveControllerApi);
    verifyNoInteractions(pushMessageService);
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_doNothing_When_rcGroupIdIsNull() {
    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers(null);

    verifyNoInteractions(userIdsProviderFactory);
    verifyNoInteractions(liveControllerApi);
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_logError_When_apiCallFailes() {
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(singletonList("test"));
    doThrow(new RestClientException("")).when(this.liveControllerApi)
        .sendLiveEvent(any(), any());

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("group id");

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_sendEventToAllUsersInsteadOfInitiatingUser() {
    List<String> userIds = asList("id1", "id2", "id3", "id4");
    when(this.byChatProvider.collectUserIds(any())).thenReturn(userIds);
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(this.byChatProvider);
    when(this.authenticatedUser.getUserId()).thenReturn("id2");

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("group id");

    List<String> expectedIds = asList("id1", "id3", "id4");
    verify(this.liveControllerApi, times(1)).sendLiveEvent(eq(expectedIds),
        eq(MESSAGE));
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_sendNothing_When_noIdsAreProvided() {
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(this.byChatProvider);

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("group id");

    verifyNoInteractions(this.liveControllerApi);
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_sendEventToAllUsers_When_initiatingUserIsAnother() {
    List<String> userIds = asList("id1", "id2", "id3", "id4");
    when(this.byChatProvider.collectUserIds(any())).thenReturn(userIds);
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(this.byChatProvider);
    when(this.authenticatedUser.getUserId()).thenReturn("another");

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("group id");

    verify(this.liveControllerApi, times(1)).sendLiveEvent(eq(userIds),
        eq(MESSAGE));
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_sendPushMessage_When_usersHaveMobileToken() {
    User user = new User();
    user.setMobileToken("mobileToken");
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(asList("1", "2"));
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);
    when(this.userService.getUser(anyString())).thenReturn(Optional.of(user));

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("valid");

    verify(this.pushMessageService, times(2)).pushNewMessageEvent("mobileToken");
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_sendPushMessageOnlyToUsersWithMobileToken() {
    User user = new User();
    user.setMobileToken("mobileToken");
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(asList("1", "2"));
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);
    when(this.userService.getUser(eq("1"))).thenReturn(Optional.of(user));
    when(this.userService.getUser(eq("2"))).thenReturn(Optional.of(new User()));

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("valid");

    verify(this.pushMessageService, times(1)).pushNewMessageEvent("mobileToken");
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_notSendPushMessage_When_noUserHasMobileToken() {
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(asList("1", "2"));
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);
    when(this.userService.getUser(any())).thenReturn(Optional.of(new User()));

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("valid");

    verifyNoInteractions(this.pushMessageService);
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_notSendPushMessage_When_userIdsAreEmpty() {
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(emptyList());
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("valid");

    verifyNoInteractions(this.pushMessageService);
  }

}
