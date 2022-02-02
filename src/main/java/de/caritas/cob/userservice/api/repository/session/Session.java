package de.caritas.cob.userservice.api.repository.session;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
import org.springframework.lang.Nullable;

/**
 * Represents a session of a user
 */
@Entity
@Builder
@Table(name = "session")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Session {

  public Session(User user, int consultingTypeId, String postcode, Long agencyId,
      SessionStatus status, boolean teamSession, boolean monitoring) {
    this.user = user;
    this.consultingTypeId = consultingTypeId;
    this.postcode = postcode;
    this.agencyId = agencyId;
    this.status = status;
    this.teamSession = teamSession;
    this.monitoring = monitoring;
    this.registrationType = RegistrationType.REGISTERED;
  }

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_session")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "Id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "consultant_id")
  @Fetch(FetchMode.SELECT)
  private Consultant consultant;

  @Column(name = "consulting_type", updatable = false, nullable = false, columnDefinition = "tinyint")
  private int consultingTypeId;

  @Column(name = "registration_type", updatable = false, nullable = false, columnDefinition = "varchar(20) not null default 'REGISTERED'")
  @Enumerated(EnumType.STRING)
  @NonNull
  private RegistrationType registrationType;

  @Column(name = "postcode", nullable = false)
  @Size(max = 5)
  @NonNull
  private String postcode;

  @Column(name = "agency_id")
  private Long agencyId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(2) not null default 'de'", length = 2, nullable = false)
  private LanguageCode languageCode;

  @NonNull
  @Column(columnDefinition = "tinyint")
  private SessionStatus status;

  @Column(name = "message_date")
  @Nullable
  private LocalDateTime enquiryMessageDate;

  @Column(name = "rc_group_id")
  private String groupId;

  @Column(name = "rc_feedback_group_id")
  private String feedbackGroupId;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "session")
  @Exclude
  private List<SessionData> sessionData;

  @Column(name = "is_team_session", columnDefinition = "tinyint(4) default '0'")
  private boolean teamSession;

  @Column(name = "is_peer_chat", columnDefinition = "tinyint(4) unsigned default '0'")
  private boolean isPeerChat;

  @Column(name = "is_monitoring", columnDefinition = "tinyint(4) default '0'")
  private boolean monitoring;

  public boolean hasFeedbackChat() {
    return isNotBlank(feedbackGroupId);
  }

  @Column(name = "create_date", columnDefinition = "datetime")
  private LocalDateTime createDate;

  @Column(name = "update_date", columnDefinition = "datetime")
  private LocalDateTime updateDate;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Session)) {
      return false;
    }
    Session session = (Session) o;
    return id.equals(session.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
