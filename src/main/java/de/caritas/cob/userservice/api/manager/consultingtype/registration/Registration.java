package de.caritas.cob.userservice.api.manager.consultingtype.registration;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
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

  private int minPostcodeSize;
  private MandatoryFields mandatoryFields;
}
