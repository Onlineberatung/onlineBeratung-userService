package de.caritas.cob.userservice.api.manager.consultingType.notifications;

import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ConsultingTypeSettings} for (email) notifications when a new message was written
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewMessage {
  private TeamSession teamSession;
}
