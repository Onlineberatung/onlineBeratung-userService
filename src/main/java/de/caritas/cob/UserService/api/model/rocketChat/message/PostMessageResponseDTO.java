package de.caritas.cob.UserService.api.model.rocketChat.message;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * Response object for Rocket.Chat API Call for posting a message
 * https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/
 * 
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
