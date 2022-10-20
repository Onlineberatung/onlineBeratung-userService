package de.caritas.cob.userservice.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents the relation between an {@link User} and a {@link Chat} */
@Entity
@Table(
    name = "user_chat",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UniqueUserAndChat",
          columnNames = {"user_id", "chat_id"})
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserChat {

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_user_chat")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "chat_id", nullable = false)
  private Chat chat;

  @Override
  public String toString() {
    return "ChatAgency [id=" + id + ", chat=" + chat.toString() + ", user=" + user.toString() + "]";
  }
}
