package de.caritas.cob.userservice.api.service.rocketchat.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {

  private String userId;
  private UserUpdateDataDTO data;

}
