package de.caritas.cob.userservice.api.service.rocketchat.dto.user;

import de.caritas.cob.userservice.api.service.rocketchat.dto.RocketChatUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rocket.Chat users.info DTO
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDTO {

  private RocketChatUserDTO user;
  private boolean success;
  private String error;
  private String errorType;

}
