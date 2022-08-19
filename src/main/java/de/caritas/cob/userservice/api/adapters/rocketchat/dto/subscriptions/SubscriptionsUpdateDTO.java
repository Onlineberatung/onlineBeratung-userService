package de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Rocket.Chat update DTO for subscriptions */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionsUpdateDTO {

  private String _id;
  private boolean open;
  private boolean alert;
  private Integer unread;
  private Integer userMentions;
  private Integer groupMentions;

  @JsonProperty("ts")
  private Date timestamp;

  @JsonProperty("rid")
  private String roomId;

  private String name;
  private String fname;

  @JsonProperty("t")
  private String roomType;

  @JsonProperty("u")
  private RocketChatUserDTO user;

  @JsonProperty("ls")
  private Date lastSeenTimestamp;

  @JsonProperty("_updatedAt")
  private Date updatedAt;

  private String[] roles;

  @JsonProperty("E2EKey")
  private String e2eKey;
}
