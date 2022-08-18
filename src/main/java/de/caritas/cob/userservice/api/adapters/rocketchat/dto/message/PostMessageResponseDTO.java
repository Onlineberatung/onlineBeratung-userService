package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response object for Rocket.Chat API Call for posting a message
 * https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostMessageResponseDTO {

  @JsonProperty("ts")
  private Date timestamp;

  private String channel;
  private boolean success;
  private String error;
  private String errorType;
}
