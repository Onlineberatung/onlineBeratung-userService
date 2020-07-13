package de.caritas.cob.UserService.api.container;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Representation of Rocket.Chat credentials for the technical user.
 *
 */

@Setter
@Getter
@Builder
public class RocketChatCredentials {

  private String RocketChatToken;
  private String RocketChatUserId;
  private String RocketChatUsername;
  private LocalDateTime TimeStampCreated;
}
