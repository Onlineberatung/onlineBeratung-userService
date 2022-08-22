package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body object for Rocket.Chat API Call for posting a message
 * https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostMessageDTO {

  private String roomId;
  private String text;
}
