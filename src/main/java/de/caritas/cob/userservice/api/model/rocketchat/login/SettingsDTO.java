package de.caritas.cob.userservice.api.model.rocketchat.login;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SettingsDTO for LoginResponseDTO
 */
@Getter
@Setter
@NoArgsConstructor
public class SettingsDTO {

  private PreferencesDTO preferences;
}
