package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoomSettingsDTO {

  private final String rid;

  private final boolean encrypted;
}
