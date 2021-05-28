package de.caritas.cob.userservice.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class PushMessageServiceTest {

  @InjectMocks
  private PushMessageService pushMessageService;

  @Mock
  private FirebaseMessaging firebaseMessaging;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setField(pushMessageService, "firebaseMessaging", firebaseMessaging);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void initializeFirebase_Should_notInitialiteFirebaseMessaging_When_firebaseIsDisabled() {
    setField(this.pushMessageService, "isEnabled", false);

    assertDoesNotThrow(() -> this.pushMessageService.initializeFirebase());

    Object firebaseMessaging = getField(pushMessageService, "firebaseMessaging");
    verify(this.logger, times(1)).info("Firebase push notifications are disabled");
  }

  @Test(expected = Exception.class)
  public void initializeFirebase_Should_throwException_When_configurationCanNotBeLoaded() {
    setField(this.pushMessageService, "isEnabled", true);
    this.pushMessageService.initializeFirebase();
  }

  @Test
  public void pushMessage_Should_pushFirebaseMessage() throws FirebaseMessagingException {
    setField(this.pushMessageService, "isEnabled", true);

    this.pushMessageService.pushNewMessageEvent("registrationToken");

    verify(this.firebaseMessaging, times(1)).send(any());
    verifyNoMoreInteractions(logger);
  }

  @Test
  public void pushMessage_Should_logWarning_When_sendFails() throws FirebaseMessagingException {
    setField(this.pushMessageService, "isEnabled", true);
    FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
    when(this.firebaseMessaging.send(any())).thenThrow(exception);

    this.pushMessageService.pushNewMessageEvent("registrationToken");

    verify(logger, times(1)).warn(anyString());
  }

  @Test
  public void pushMessage_Should_notSendNotification_When_firebaseIsDisabled()
      throws FirebaseMessagingException {
    setField(this.pushMessageService, "isEnabled", false);

    this.pushMessageService.pushNewMessageEvent("registrationToken");

    verifyNoMoreInteractions(this.firebaseMessaging);
  }

}
