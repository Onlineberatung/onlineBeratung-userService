package de.caritas.cob.UserService.api.repository.consultantAgency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a the relation between consultant and agency
 *
 */
@Entity
@Table(name = "consultant_agency")
@AllArgsConstructor
@Getter
@Setter
public class ConsultantAgency {

  public ConsultantAgency() {}

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1,
      sequenceName = "sequence_consultant_agency")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "consultant_id", nullable = false)
  private Consultant consultant;

  @Column(name = "agency_id", updatable = true, nullable = true)
  private Long agencyId;

}
