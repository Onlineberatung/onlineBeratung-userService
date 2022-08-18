package de.caritas.cob.userservice.api.manager.consultingtype.registration;

import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** {@link ExtendedConsultingTypeResponseDTO} for registration */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Registration {

  private MandatoryFields mandatoryFields;
}
