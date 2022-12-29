package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import java.util.List;
import lombok.Data;

@Data
public class PresenceListDTO {

  private List<PresenceOtherDTO> users;

  private Boolean success;
}
