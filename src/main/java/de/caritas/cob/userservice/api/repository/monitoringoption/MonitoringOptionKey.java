package de.caritas.cob.userservice.api.repository.monitoringoption;

import java.io.Serializable;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key for {@link MonitoringOption}.
 */
@SuppressWarnings("serial")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringOptionKey implements Serializable {

  private Long sessionId;
  private MonitoringType monitoringType;
  private String monitoringKey;
  private String key;
}
