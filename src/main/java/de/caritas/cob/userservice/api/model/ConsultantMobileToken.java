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

/** Represents the relation between consultant and mobile token. */
@Entity
@Table(name = "consultant_mobile_token")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConsultantMobileToken {

  @Id
  @SequenceGenerator(
      name = "id_seq",
      allocationSize = 1,
      sequenceName = "sequence_consultant_mobile_token")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "consultant_id", nullable = false)
  private Consultant consultant;

  @Column(name = "mobile_app_token", nullable = false)
  @Lob
  private String mobileAppToken;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConsultantMobileToken)) {
      return false;
    }
    ConsultantMobileToken that = (ConsultantMobileToken) o;
    return Objects.equals(mobileAppToken, that.mobileAppToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mobileAppToken);
  }
}
