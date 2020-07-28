package de.caritas.cob.UserService.api.repository.user;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Type;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.userAgency.UserAgency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a user
 * 
 */
@Entity
@Table(name = "user")
@AllArgsConstructor
@Getter
@Setter
@ToString
public class User {

  protected User() {}

  @Id
  @Column(name = "user_id", updatable = false, nullable = false)
  @Size(max = 36)
  @NonNull
  private String userId;

  @Column(name = "id_old", updatable = false, nullable = true)
  private Long oldId;

  @Column(name = "username", updatable = false, nullable = false)
  @Size(max = 255)
  @NonNull
  private String username;

  @Column(name = "email", updatable = false, nullable = false)
  @Size(max = 255)
  @NonNull
  private String email;

  @Column(name = "rc_user_id", updatable = true, nullable = true)
  private String rcUserId;

  @Column(name = "language_formal", updatable = true, nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean languageFormal;

  @OneToMany(mappedBy = "user")
  private Set<Session> sessions;

  @OneToMany(mappedBy = "user")
  private Set<UserAgency> userAgencies;

  @Override
  public boolean equals(Object obj) {

    // If i´m compared to myself => true
    if (obj == this) {
      return true;
    }

    // If the obj is not an instance of User => false
    if (!(obj instanceof User)) {
      return false;
    }

    User other = (User) obj;

    if (!this.userId.equals(other.userId)) {
      return false;
    }

    if (this.oldId == null && other.oldId != null) {
      return false;
    }

    if (this.oldId != null && other.oldId == null) {
      return false;
    }

    if (this.oldId != null && other.oldId != null && !this.oldId.equals(other.oldId)) {
      return false;
    }

    if (!this.username.equals(other.username)) {
      return false;
    }

    if (!this.email.equals(other.email)) {
      return false;
    }

    if (this.rcUserId == null && other.rcUserId != null) {
      return false;
    }

    if (this.rcUserId != null && other.rcUserId == null) {
      return false;
    }

    if (this.rcUserId != null && other.rcUserId != null && !this.rcUserId.equals(other.rcUserId)) {
      return false;
    }

    if (this.languageFormal != other.languageFormal) {
      return false;
    }

    if (this.sessions == null && other.sessions != null) {
      return false;
    }

    if (this.sessions != null && other.sessions == null) {
      return false;
    }

    if (this.sessions != null && other.sessions != null && !this.sessions.equals(other.sessions)) {
      return false;
    }

    return true;
  }

  public User(@Size(max = 36) String userId, @Size(max = 255) String username,
      @Size(max = 255) String email, Set<Session> sessions) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.sessions = sessions;
  }

  public User(@Size(max = 36) String userId, Long oldId, @Size(max = 255) String username,
      @Size(max = 255) String email, boolean languageFormal) {
    this.userId = userId;
    this.oldId = oldId;
    this.username = username;
    this.email = email;
    this.languageFormal = languageFormal;
  }

}
