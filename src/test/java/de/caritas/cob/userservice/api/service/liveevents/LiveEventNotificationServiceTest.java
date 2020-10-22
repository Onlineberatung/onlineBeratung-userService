package de.caritas.cob.userservice.api.service.liveevents;


import static de.caritas.cob.userservice.liveservice.generated.web.model.EventType.DIRECTMESSAGE;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveEventNotificationServiceTest {

  @InjectMocks
  private LiveEventNotificationService liveEventNotificationService;

  @Mock
  private LiveControllerApi liveControllerApi;

  @Mock
  private UserIdsProviderFactory userIdsProviderFactory;

  @Mock
  private RelevantUserAccountIdsBySessionProvider bySessionProvider;

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_callFactoryAndLiveApi_When_rcGroupIdIsValid() {
    when(this.bySessionProvider.collectUserIds(any())).thenReturn(asList("1", "2"));
    when(this.userIdsProviderFactory.byRocketChatGroup(any())).thenReturn(bySessionProvider);

    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("valid");

    verify(userIdsProviderFactory, times(1)).byRocketChatGroup("valid");
    verify(liveControllerApi, times(1))
        .sendLiveEvent(eq(asList("1", "2")), eq(DIRECTMESSAGE.toString()));
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_doNothing_When_rcGroupIdIsEmpty() {
    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers("");

    verifyZeroInteractions(userIdsProviderFactory);
    verifyZeroInteractions(liveControllerApi);
  }

  @Test
  public void sendLiveDirectMessageEventToUsers_Should_doNothing_When_rcGroupIdIsNull() {
    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers(null);

    verifyZeroInteractions(userIdsProviderFactory);
    verifyZeroInteractions(liveControllerApi);
  }

}
