package de.caritas.cob.userservice.api.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents a the relation between user and mobile token. */
@Entity
@Table(name = "user_mobile_token")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserMobileToken {

  @Id
  @SequenceGenerator(
      name = "id_seq",
      allocationSize = 1,
      sequenceName = "sequence_user_mobile_token")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "mobile_app_token")
  @Lob
  private String mobileAppToken;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserMobileToken)) {
      return false;
    }
    UserMobileToken that = (UserMobileToken) o;
    return mobileAppToken.equals(that.mobileAppToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mobileAppToken);
  }
}
