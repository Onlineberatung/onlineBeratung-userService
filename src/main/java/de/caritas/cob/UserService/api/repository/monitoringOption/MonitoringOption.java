package de.caritas.cob.UserService.api.repository.monitoringOption;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import de.caritas.cob.UserService.api.repository.monitoring.Monitoring;
import de.caritas.cob.UserService.api.repository.monitoring.MonitoringType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents the monitoring option of an asker
 *
 */

@Entity
@Table(name = "session_monitoring_option")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@IdClass(MonitoringOptionKey.class)
public class MonitoringOption {

  @Id
  @Column(name = "session_id", updatable = false, nullable = false)
  @NonNull
  private Long sessionId;

  @Id
  @Column(name = "monitoring_type", updatable = false, nullable = false)
  @NonNull
  private MonitoringType monitoringType;

  @Id
  @Column(name = "monitoring_key_name", updatable = true, nullable = true)
  @NonNull
  @Size(max = 255)
  private String monitoring_key;

  @Id
  @Column(name = "key_name", updatable = true, nullable = true)
  @NonNull
  @Size(max = 255)
  private String key;

  @Column(name = "value", updatable = true, nullable = true)
  private Boolean value;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "session_id", referencedColumnName = "session_id", insertable = false,
          updatable = false),
      @JoinColumn(name = "monitoring_type", referencedColumnName = "type", insertable = false,
          updatable = false),
      @JoinColumn(name = "monitoring_key_name", referencedColumnName = "key_name",
          insertable = false, updatable = false)})
  private Monitoring monitoring;
}
