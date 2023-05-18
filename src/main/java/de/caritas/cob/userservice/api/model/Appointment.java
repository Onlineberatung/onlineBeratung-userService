package de.caritas.cob.userservice.api.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Table
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Appointment {

  public enum AppointmentStatus {
    CREATED,
    STARTED,
    PAUSED
  }

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(columnDefinition = "char(36)")
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID id;

  @Column(length = 300)
  private Integer bookingId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      columnDefinition = "varchar(36)",
      name = "consultant_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "appointment_consultant_constraint"))
  @ToString.Exclude
  private Consultant consultant;

  @Column(length = 300)
  private String description;

  @Column(nullable = false)
  private Instant datetime;

  @Enumerated(EnumType.STRING)
  @Column(length = 7, nullable = false)
  private AppointmentStatus status;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Appointment that = (Appointment) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
