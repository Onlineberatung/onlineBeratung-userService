package de.caritas.cob.userservice.api.repository.monitoringoption;

import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.Type;

/**
 * Represents the monitoring option of an asker
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
  @Type(type = "org.hibernate.type.ByteType")
  private MonitoringType monitoringType;

  @Id
  @Column(name = "monitoring_key_name")
  @NonNull
  @Size(max = 255)
  private String monitoringKey;

  @Id
  @Column(name = "key_name")
  @NonNull
  @Size(max = 255)
  private String key;

  @Column(name = "value")
  private Boolean value;

  @ManyToOne
  @JoinColumn(name = "session_id", referencedColumnName = "session_id", insertable = false,
      updatable = false)
  @JoinColumn(name = "monitoring_type", referencedColumnName = "type", insertable = false,
      updatable = false)
  @JoinColumn(name = "monitoring_key_name", referencedColumnName = "key_name",
      insertable = false, updatable = false)
  private Monitoring monitoring;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MonitoringOption)) {
      return false;
    }
    MonitoringOption that = (MonitoringOption) o;
    return sessionId.equals(that.sessionId)
        && monitoringType == that.monitoringType
        && monitoringKey.equals(that.monitoringKey)
        && key.equals(that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, monitoringType, monitoringKey, key);
  }
}
