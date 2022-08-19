package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** EmailsDTO for LoginResponseDTO */
@Getter
@Setter
@NoArgsConstructor
public class EmailsDTO {

  private String address;
  private boolean verified;
}
