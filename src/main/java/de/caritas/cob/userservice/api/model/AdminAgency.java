package de.caritas.cob.userservice.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "admin_agency")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class AdminAgency implements Serializable {
  @Id
  @SequenceGenerator(name = "id_seq", allocationSize = 1, sequenceName = "sequence_admin_agency")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "admin_id", nullable = false)
  private Admin admin;

  @Column(name = "agency_id")
  @Field
  private Long agencyId;

  @CreatedDate
  @Column(name = "create_date")
  private LocalDateTime createDate;

  @LastModifiedDate
  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AdminAgency)) {
      return false;
    }
    AdminAgency that = (AdminAgency) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public interface AdminAgencyBase {

    Long getId();

    Long getAgencyId();

    String getAdminId();
  }
}
