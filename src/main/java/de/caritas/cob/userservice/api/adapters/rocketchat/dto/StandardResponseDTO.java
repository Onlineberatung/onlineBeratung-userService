package de.caritas.cob.userservice.api.adapters.rocketchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Standard response object for Rocket.Chat API calls which contain a boolean success and an
 * optional error message.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponseDTO {

  private boolean success;
  private String error;
}
