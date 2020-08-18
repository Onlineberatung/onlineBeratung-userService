package de.caritas.cob.userservice.api.manager.consultingType.notifications;

import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ConsultingTypeSettings} for (email) notifications when a new message was written in a team
 * session
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TeamSession {

  private ToConsultant toConsultant;
}
