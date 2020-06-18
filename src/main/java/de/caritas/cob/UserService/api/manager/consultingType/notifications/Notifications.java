package de.caritas.cob.UserService.api.manager.consultingType.notifications;

import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ConsultingTypeSettings} for (email) notifications
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notifications {
  private NewMessage newMessage;
}
