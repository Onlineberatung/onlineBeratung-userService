package de.caritas.cob.userservice.api.model;

import java.time.LocalDateTime;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
@Table(name = "session_topic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionTopic {

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_session_topic")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  @NonNull
  private Session session;

  @Column(name = "topic_id")
  @NonNull
  private Long topicId;

  @Column(name = "create_date")
  private LocalDateTime createDate;

  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SessionTopic)) {
      return false;
    }
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
