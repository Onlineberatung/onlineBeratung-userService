package de.caritas.cob.userservice.api.model;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "session_monitoring")
@IdClass(MonitoringKey.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Monitoring {

  @AllArgsConstructor
  @Getter
  public enum MonitoringType {
    ADDICTIVE_DRUGS("addictiveDrugs", 0),
    INTERVENTION("intervention", 0),
    GENERAL_DATA("generalData", 1),
    CONSULTING_DATA("consultingData", 1),
    GENERAL_DATA_GSE("generalData", 22),
    CONSULTING_DATA_GSE("consultingData", 22);

    private final String key;
    private final int consultingTypeId;
  }

  public Monitoring(@NonNull Long sessionId, @NonNull MonitoringType monitoringType,
      @Size(max = 255) @NonNull String key, Boolean value) {
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
  @Column(name = "type", updatable = false, nullable = false, columnDefinition = "tinyint(4) unsigned")
  @NonNull
  private MonitoringType monitoringType;

  @Id
  @Column(name = "key_name")
  @NonNull
  @Size(max = 255)
  private String key;

  @Column(name = "value")
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
