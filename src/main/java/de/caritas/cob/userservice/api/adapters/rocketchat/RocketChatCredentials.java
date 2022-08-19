package de.caritas.cob.userservice.api.adapters.rocketchat;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** Representation of Rocket.Chat credentials for the technical user. */
@Data
@Builder
public class RocketChatCredentials {

  private String rocketChatToken;
  private String rocketChatUserId;
  private String rocketChatUsername;
  private LocalDateTime timeStampCreated;
}
