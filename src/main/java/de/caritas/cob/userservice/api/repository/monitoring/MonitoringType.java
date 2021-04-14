package de.caritas.cob.userservice.api.repository.monitoring;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Monitoring types
 */

@AllArgsConstructor
@Getter
public enum MonitoringType {

  ADDICTIVE_DRUGS("addictiveDrugs", 0), INTERVENTION("intervention",
      0), GENERAL_DATA("generalData",
      1), CONSULTING_DATA("consultingData", 1);
  private final String key;
  private final int consultingID;
}
