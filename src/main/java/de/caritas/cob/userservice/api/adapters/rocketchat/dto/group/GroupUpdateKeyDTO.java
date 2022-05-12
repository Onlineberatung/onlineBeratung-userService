package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class GroupUpdateKeyDTO {

  private String uid;

  private String rid;

  private String key;
}
