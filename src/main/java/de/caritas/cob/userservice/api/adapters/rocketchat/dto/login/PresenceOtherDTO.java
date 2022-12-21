package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO.PresenceStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PresenceOtherDTO {

  @JsonProperty("_id")
  private String id;

  private String username;

  private PresenceStatus status;

  @JsonIgnore
  public boolean isAvailable() {
    return nonNull(status) && status.equals(PresenceStatus.ONLINE);
  }
}
