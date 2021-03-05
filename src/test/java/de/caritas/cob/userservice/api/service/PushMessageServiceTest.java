package de.caritas.cob.userservice.api.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.Test;

public class PushMessageServiceTest {

  private final PushMessageService pushMessageService = new PushMessageService();

  @Test
  public void pushMessage_Should_pushMessage() throws FirebaseMessagingException {
    String registrationToken = "crMYe-q0TYu5vHz7YnnNTY:APA91bEVtVeHPVNQYsolvhE9XU40BrJVzT8nFgLy6SZ-RH90YpLYuwnz9HHQHrRvYvpB2hraW1PQNYzAdjFSSapgbgNQ58PmIp44zyZmE-hVgswle_BnysxXTI4WERZMBBK7OAA3XPoJ";
    this.pushMessageService.pushNewMessageEvent(registrationToken);
  }

}
