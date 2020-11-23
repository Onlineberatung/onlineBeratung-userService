package de.caritas.cob.userservice.api.repository.session;

import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.springframework.lang.Nullable;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.sessionData.SessionData;
import de.caritas.cob.userservice.api.repository.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a session of a user
 * 
 */
@Entity
@Table(name = "session")
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Session {

  public Session() {}

  public Session(User user, ConsultingType consultingType, String postcode, Long agencyId,
      SessionStatus status, boolean teamSession, boolean monitoring) {
    this.user = user;
    this.consultingType = consultingType;
    this.postcode = postcode;
    this.agencyId = agencyId;
    this.status = status;
    this.teamSession = teamSession;
    this.monitoring = monitoring;
  }

  public Session(Long id, User user, Consultant consultant, ConsultingType consultingType,
      String postcode, Long agencyId, SessionStatus status, Date messageDate, String groupId) {
    this.id = id;
    this.user = user;
    this.consultant = consultant;
    this.consultingType = consultingType;
    this.postcode = postcode;
    this.agencyId = agencyId;
    this.status = status;
    this.enquiryMessageDate = messageDate;
    this.groupId = groupId;
  }

  public Session(Long id, User user, Consultant consultant, ConsultingType consultingType,
      @Size(max = 5) String postcode, Long agencyId, SessionStatus status, Date enquiryMessageDate,
      String groupId, List<SessionData> sessionData, boolean teamSession, boolean monitoring) {
    this.id = id;
    this.user = user;
    this.consultant = consultant;
    this.consultingType = consultingType;
    this.postcode = postcode;
    this.agencyId = agencyId;
    this.status = status;
    this.enquiryMessageDate = enquiryMessageDate;
    this.groupId = groupId;
    this.sessionData = sessionData;
    this.teamSession = teamSession;
    this.monitoring = monitoring;
  }

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_session")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "Id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(optional = true)
  @JoinColumn(name = "consultant_id", nullable = false)
  @Fetch(FetchMode.SELECT)
  private Consultant consultant;

  @Column(name = "consulting_type", updatable = false, nullable = false)
  @NonNull
  private ConsultingType consultingType;

  @Column(name = "postcode", updatable = true, nullable = false)
  @Size(max = 5)
  @NonNull
  private String postcode;

  @Column(name = "agency_id", updatable = true, nullable = true)
  private Long agencyId;

  @NonNull
  private SessionStatus status;

  @Column(name = "message_date", updatable = true, nullable = true)
  @Nullable
  private Date enquiryMessageDate;

  @Column(name = "rc_group_id", updatable = true, nullable = true)
  private String groupId;

  @Column(name = "rc_feedback_group_id", updatable = true, nullable = true)
  private String feedbackGroupId;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "session")
  private List<SessionData> sessionData;

  @Column(name = "is_team_session", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean teamSession;

  @Column(name = "is_monitoring", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean monitoring;

  public boolean hasFeedbackChat() {
    return feedbackGroupId != null;
  }

  @Column(name = "create_date")
  private Date createDate;

  @Column(name = "update_date")
  private Date updateDate;

  @Override
  public boolean equals(Object obj) {

    // If i´m compared to myself => true
    if (obj == this) {
      return true;
    }

    // If the obj ist not an instance of Session => false
    if (!(obj instanceof Session)) {
      return false;
    }

    Session otherSession = (Session) obj;

    if (this.id != otherSession.id) {
      return false;
    }

    if (this.agencyId != otherSession.agencyId) {
      return false;
    }

    if (this.teamSession != otherSession.teamSession) {
      return false;
    }

    if (!this.status.equals(otherSession.status)) {
      return false;
    }

    if (!this.consultingType.equals(otherSession.consultingType)) {
      return false;
    }

    if (!this.enquiryMessageDate.equals(otherSession.enquiryMessageDate)) {
      return false;
    }

    if (!this.postcode.equals(otherSession.postcode)) {
      return false;
    }

    if (!this.groupId.equals(otherSession.groupId)) {
      return false;
    }

    if (!this.feedbackGroupId.equals(otherSession.feedbackGroupId)) {
      return false;
    }

    if (!sessionData.equals(otherSession.sessionData)) {
      return false;
    }

    if (!consultant.equals(otherSession.consultant)) {
      return false;
    }

    if (!user.equals(otherSession.user)) {
      return false;
    }

    return true;
  }

}
