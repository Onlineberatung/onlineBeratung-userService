package de.caritas.cob.userservice.api.repository.chat;

import java.time.LocalDateTime;
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
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Representation of a chat
 * 
 */
@Entity
@Table(name = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Chat {

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_chat")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @Column(name = "topic", updatable = true, nullable = false)
  @Size(max = 255)
  @NonNull
  private String topic;

  @Column(name = "consulting_type", updatable = false, nullable = false)
  @NonNull
  private ConsultingType consultingType;

  @Column(name = "initial_start_date", updatable = true, nullable = false)
  @NonNull
  private LocalDateTime initialStartDate;

  @Column(name = "start_date", updatable = true, nullable = false)
  @NonNull
  private LocalDateTime startDate;

  @Column(name = "duration", updatable = true, nullable = false)
  private int duration;

  @Column(name = "is_repetitive", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean repetitive;

  @Enumerated(EnumType.STRING)
  @Column(name = "chat_interval", updatable = true, nullable = true)
  private ChatInterval chatInterval;

  @Column(name = "is_active", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean active;

  @Column(name = "max_participants", updatable = true, nullable = true)
  private Integer maxParticipants;

  @Column(name = "rc_group_id", updatable = true, nullable = true)
  private String groupId;

  @ManyToOne(optional = true)
  @JoinColumn(name = "consultant_id_owner", nullable = false)
  @Fetch(FetchMode.SELECT)
  private Consultant chatOwner;

  @OneToMany(mappedBy = "chat", orphanRemoval = true)
  private Set<ChatAgency> chatAgencies;

  public Chat(String topic, ConsultingType consultingType, LocalDateTime initialStartDate,
      LocalDateTime startDate, int duration, boolean repetitive, ChatInterval chatInterval,
      Consultant chatOwner) {
    this.topic = topic;
    this.consultingType = consultingType;
    this.initialStartDate = initialStartDate;
    this.startDate = startDate;
    this.duration = duration;
    this.repetitive = repetitive;
    this.chatInterval = chatInterval;
    this.chatOwner = chatOwner;
  }

  public Chat(LocalDateTime startDate, boolean repetitive, ChatInterval chatInterval,
      boolean active, String groupId) {
    this.startDate = startDate;
    this.repetitive = repetitive;
    this.chatInterval = chatInterval;
    this.active = active;
    this.groupId = groupId;
  }
}
