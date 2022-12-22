package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Rocket.Chat group member DTO
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDTO {

  private String _id;
  private String status;
  private String username;
  private String name;
  private String utcOffset;
}
