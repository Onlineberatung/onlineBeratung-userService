package de.caritas.cob.userservice.api.service.message;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RocketChatData {

  private final String message;
  private final RocketChatCredentials rocketChatCredentials;
  private final String rcGroupId;
  private String type;
}
