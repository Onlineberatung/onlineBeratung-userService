package de.caritas.cob.userservice.api.repository.consultantagency;

import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.agency.AgencyLoader;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

/**
 * Represents a the relation between consultant and agency
 *
 */
@Entity
@Table(name = "consultant_agency")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ConsultantAgency {

  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1,
      sequenceName = "sequence_consultant_agency")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "consultant_id", nullable = false)
  private Consultant consultant;

  @Column(name = "agency_id")
  @Field
  @FieldBridge(impl = LongBridge.class)
  private Long agencyId;

  @Column(name = "create_date")
  private LocalDateTime createDate;

  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Column(name = "delete_date")
  private LocalDateTime deleteDate;

  public AgencyDTO getAgency() {
    return AgencyLoader.getAgency(this.agencyId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConsultantAgency)) {
      return false;
    }
    ConsultantAgency that = (ConsultantAgency) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
