package de.caritas.cob.userservice.api.adapters.rocketchat.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MariaDbUser {

  String userId;
  String rocketChatId;
  String email;
}
