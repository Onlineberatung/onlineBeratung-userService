package de.caritas.cob.UserService.api.repository.userAgency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import de.caritas.cob.UserService.api.repository.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a the relation between user and agency
 *
 */
@Entity
@Table(name = "user_agency")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserAgency {

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_user_agency")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "agency_id", updatable = true, nullable = true)
  private Long agencyId;


  public UserAgency(User user, Long agencyId) {
    this.user = user;
    this.agencyId = agencyId;
  }

}
