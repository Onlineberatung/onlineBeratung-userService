package de.caritas.cob.userservice.api.repository.sessionData;

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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "session_data")
@Getter
@Setter
public class SessionData {

  public SessionData() {}

  public SessionData(Session session, SessionDataType sessionDataType, String key, String value) {
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

  @Column(name = "key_name", updatable = true, nullable = true)
  @NonNull
  private String key;

  @Column(name = "value", updatable = true, nullable = true)
  @NonNull
  @Size(max = 255)
  private String value;

  @Override
  public boolean equals(Object obj) {

    // If i´m compared to myself => true
    if (obj == this) {
      return true;
    }

    // If the obj ist not an instance of SessionData => false
    if (!(obj instanceof SessionData)) {
      return false;
    }

    SessionData other = (SessionData) obj;

    if (!this.id.equals(other.id)) {
      return false;
    }

    if (!this.session.equals(other.session)) {
      return false;
    }

    if (!this.sessionDataType.equals(other.sessionDataType)) {
      return false;
    }

    if (!this.key.equals(other.key)) {
      return false;
    }

    if (!this.value.equals(other.value)) {
      return false;
    }

    return true;
  }

}
