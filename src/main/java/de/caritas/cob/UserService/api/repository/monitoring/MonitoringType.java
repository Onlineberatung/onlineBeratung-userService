package de.caritas.cob.UserService.api.repository.monitoring;

import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Monitoring types
 *
 */

@AllArgsConstructor
@Getter
public enum MonitoringType {

  ADDICTIVE_DRUGS("addictiveDrugs", ConsultingType.SUCHT), INTERVENTION("intervention",
      ConsultingType.SUCHT), GENERAL_DATA("generalData",
          ConsultingType.U25), CONSULTING_DATA("consultingData", ConsultingType.U25);
  private final String key;
  private final ConsultingType consultingType;
}
