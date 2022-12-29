package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Response object for Rocket.Chat API Call for getting the group counters
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupCounterResponseDTO {

  private Boolean joined;
  private int members;
  private Integer unreads;
  private Date unreadsFrom;
  private int msgs;
  private Date latest;
  private Integer userMentions;
  private Boolean success;
}
