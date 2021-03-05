package de.caritas.cob.userservice.api.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.InputStream;
import lombok.SneakyThrows;

public class PushMessageService {

  public PushMessageService() {
    initializeFirebase();
  }

  public void pushNewMessageEvent(String registrationToken) throws FirebaseMessagingException {
    Message message = Message.builder()
        .setNotification(Notification.builder()
            .setTitle("Neue Nachricht")
            .setBody("Sie haben eine neue Nachricht")
            .build())
        .putData("text", "test")
        .setToken(registrationToken)
        .build();

    FirebaseMessaging.getInstance().send(message);
  }

  @SneakyThrows
  private void initializeFirebase() {
    InputStream inputStream =
        PushMessageService.class
            .getResourceAsStream("/firebase/firebase-credentials.json");

    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(inputStream))
        .build();

    FirebaseApp.initializeApp(options);
  }

}
