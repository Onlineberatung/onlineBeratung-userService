package de.caritas.cob.userservice.api.model.rocketchat.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetRoomReadOnlyBodyDTO {

  private String roomId;
  private boolean readOnly;

}
