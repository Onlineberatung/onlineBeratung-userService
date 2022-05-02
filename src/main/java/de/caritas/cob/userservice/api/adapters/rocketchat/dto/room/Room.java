package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class Room {

  @JsonProperty("_id")
  private String id;

  private String name;

  private List<String> muted;
}
