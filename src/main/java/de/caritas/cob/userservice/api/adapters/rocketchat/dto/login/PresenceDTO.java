package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PresenceDTO {

  private String presence;

  private Boolean success;

  @JsonIgnore
  public boolean isPresent() {
    return nonNull(presence) && presence.equals("online");
  }
}
