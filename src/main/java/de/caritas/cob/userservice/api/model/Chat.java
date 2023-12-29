package de.caritas.cob.userservice.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Chat {

  public enum ChatInterval {
    WEEKLY
  }

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_chat")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @Column(name = "topic", nullable = false)
  @Size(max = 255)
  @NonNull
  private String topic;

  @Column(name = "consulting_type", updatable = false, columnDefinition = "tinyint(4) unsigned")
  private Integer consultingTypeId;

  @Column(name = "initial_start_date", nullable = false)
  @NonNull
  private LocalDateTime initialStartDate;

  @Column(name = "start_date", nullable = false)
  @NonNull
  private LocalDateTime startDate;

  @Column(name = "duration", nullable = false, columnDefinition = "smallint")
  private int duration;

  @Column(name = "is_repetitive", nullable = false)
  private boolean repetitive;

  @Enumerated(EnumType.STRING)
  @Column(name = "chat_interval")
  private ChatInterval chatInterval;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "max_participants", columnDefinition = "tinyint(4) unsigned NULL")
  private Integer maxParticipants;

  @Column(name = "rc_group_id")
  private String groupId;

  @ManyToOne
  @JoinColumn(name = "consultant_id_owner", nullable = false)
  @Fetch(FetchMode.SELECT)
  private Consultant chatOwner;

  @OneToMany(mappedBy = "chat", orphanRemoval = true)
  @Exclude
  private Set<ChatAgency> chatAgencies;

  @OneToMany(mappedBy = "chat", orphanRemoval = true)
  @Exclude
  private Set<UserChat> chatUsers;

  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Column(name = "create_date")
  private LocalDateTime createDate;

  @Column(name = "hint_message")
  private String hintMessage;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Chat)) {
      return false;
    }
    var chat = (Chat) o;
    return id.equals(chat.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @JsonIgnore
  public LocalDateTime nextStart() {
    if (!repetitive) {
      return null;
    }

    if (!ChatInterval.WEEKLY.equals(chatInterval)) {
      var message = "Repetitive chat with id %s does not have a valid interval.";
      throw new InternalServerErrorException(String.format(message, id));
    }

    return startDate.plusWeeks(1);
  }
}
