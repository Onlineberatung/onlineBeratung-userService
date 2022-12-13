package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PresenceDTO {

  public enum PresenceStatus {
    @JsonProperty("offline")
    OFFLINE,
    @JsonProperty("online")
    ONLINE,
    @JsonProperty("busy")
    BUSY,
    @JsonProperty("away")
    AWAY;
  }

  private PresenceStatus presence;

  private Boolean success;

  @JsonIgnore
  public boolean isPresent() {
    return nonNull(presence) && !presence.equals(PresenceStatus.OFFLINE);
  }

  @JsonIgnore
  public boolean isAvailable() {
    return nonNull(presence) && presence.equals(PresenceStatus.ONLINE);
  }
}
