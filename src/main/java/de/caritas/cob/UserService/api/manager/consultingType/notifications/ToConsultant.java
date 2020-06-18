package de.caritas.cob.UserService.api.manager.consultingType.notifications;

import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ConsultingTypeSettings} for (email) notifications when a new message was written in a team
 * session an the recipient is a consultant
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ToConsultant {

  private boolean allTeamConsultants;
}
