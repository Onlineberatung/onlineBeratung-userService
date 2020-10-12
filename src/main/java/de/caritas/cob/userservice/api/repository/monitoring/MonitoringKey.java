package de.caritas.cob.userservice.api.repository.monitoring;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key for {@link Monitoring}
 *
 */

@SuppressWarnings("serial")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringKey implements Serializable {

  private Long sessionId;
  private MonitoringType monitoringType;
  private String key;
}
