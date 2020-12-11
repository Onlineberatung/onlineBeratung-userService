package de.caritas.cob.userservice.api.manager.consultingType.registration;

import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingType.registration.mandatoryFields.MandatoryFields;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ConsultingTypeSettings} for registration
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Registration {

  private MandatoryFields mandatoryFields;
}
