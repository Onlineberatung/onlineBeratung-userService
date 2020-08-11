package de.caritas.cob.userservice.api.repository.monitoring;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import de.caritas.cob.userservice.api.repository.monitoringOption.MonitoringOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents the monitoring of an asker
 *
 */

@Entity
@Table(name = "session_monitoring")
@IdClass(MonitoringKey.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Monitoring {

  public Monitoring(Long sessionId, MonitoringType monitoringType, @Size(max = 255) String key,
      Boolean value) {
    super();
    this.sessionId = sessionId;
    this.monitoringType = monitoringType;
    this.key = key;
    this.value = value;
  }

  @Id
  @Column(name = "session_id", updatable = false, nullable = false)
  @NonNull
  private Long sessionId;

  /**
   * The MonitoringType is needed to allocate the {@link MonitoringOption} to this repository. In
   * the {@link MonitoringOption} repository we need the {@link MonitoringType} to have a unique
   * entity because multiple keys can have the same identifier.
   */
  @Id
  @Column(name = "type", updatable = false, nullable = false)
  @NonNull
  private MonitoringType monitoringType;

  @Id
  @Column(name = "key_name", updatable = true, nullable = true)
  @NonNull
  @Size(max = 255)
  private String key;

  @Column(name = "value", updatable = true, nullable = true)
  private Boolean value;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "monitoring")
  private List<MonitoringOption> monitoringOptionList;
}
