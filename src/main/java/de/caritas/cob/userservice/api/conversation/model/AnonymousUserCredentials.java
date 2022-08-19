package de.caritas.cob.userservice.api.conversation.model;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import lombok.Builder;
import lombok.Data;

/** Representation of Keycloak and Rocket.Chat credentials for an anonymous user. */
@Data
@Builder
public class AnonymousUserCredentials {

  private String userId;
  private String accessToken;
  private int expiresIn;
  private String refreshToken;
  private int refreshExpiresIn;
  private RocketChatCredentials rocketChatCredentials;
}
