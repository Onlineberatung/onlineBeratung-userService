package de.caritas.cob.UserService.api.repository.chatAgency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import de.caritas.cob.UserService.api.repository.chat.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the relation between a {@link Chat} and an agency
 *
 */
@Entity
@Table(name = "chat_agency")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatAgency {

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_chat_agency")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "chat_id", nullable = false)
  private Chat chat;

  @Column(name = "agency_id", nullable = false)
  private Long agencyId;


  public ChatAgency(Chat chat, Long agencyId) {
    this.chat = chat;
    this.agencyId = agencyId;
  }


  @Override
  public String toString() {
    return "ChatAgency [id=" + id + ", chat=" + chat.toString() + ", agencyId=" + agencyId + "]";
  }
}
