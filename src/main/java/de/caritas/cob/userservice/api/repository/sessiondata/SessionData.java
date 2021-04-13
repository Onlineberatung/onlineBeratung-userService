package de.caritas.cob.userservice.api.repository.sessiondata;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import de.caritas.cob.userservice.api.repository.session.Session;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "session_data")
@Getter
@Setter
@NoArgsConstructor
public class SessionData {

  public SessionData(@NonNull Session session, @NonNull SessionDataType sessionDataType,
      @NonNull String key, String value) {
    this.session = session;
    this.sessionDataType = sessionDataType;
    this.key = key;
    this.value = value;
  }

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_session_data")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  @NonNull
  private Session session;

  @Column(name = "type", updatable = false, nullable = false)
  @NonNull
  private SessionDataType sessionDataType;

  @Column(name = "key_name")
  @NonNull
  private String key;

  @Column(name = "value")
  @Size(max = 255)
  private String value;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SessionData)) {
      return false;
    }
    SessionData that = (SessionData) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
