package de.caritas.cob.userservice.api.repository.monitoring;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import de.caritas.cob.userservice.api.repository.monitoringoption.MonitoringOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.Type;

/**
 * Represents the monitoring of an asker
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
  @Type(type = "org.hibernate.type.ByteType")
  private MonitoringType monitoringType;

  @Id
  @Column(name = "key_name")
  @NonNull
  @Size(max = 255)
  private String key;

  @Column(name = "value", updatable = true, nullable = true)
  private Boolean value;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "monitoring")
  private List<MonitoringOption> monitoringOptionList;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Monitoring)) {
      return false;
    }
    Monitoring that = (Monitoring) o;
    return sessionId.equals(that.sessionId)
        && monitoringType == that.monitoringType
        && key.equals(that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, monitoringType, key);
  }
}
