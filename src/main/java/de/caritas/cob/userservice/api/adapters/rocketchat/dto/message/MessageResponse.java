package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message;

import lombok.Data;

@Data
public class MessageResponse {

  private String message;

  private Boolean success;
}
