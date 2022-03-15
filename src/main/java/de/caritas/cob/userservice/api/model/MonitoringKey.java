package de.caritas.cob.userservice.api.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key for {@link Monitoring}
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringKey implements Serializable {

  private Long sessionId;
  private Monitoring.MonitoringType monitoringType;
  private String key;
}
