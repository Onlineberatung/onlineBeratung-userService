package de.caritas.cob.userservice.api.adapters.rocketchat.dto;

import lombok.Data;

@Data
public class RoomResponse {

  private Room room;

  private Boolean success;
}
