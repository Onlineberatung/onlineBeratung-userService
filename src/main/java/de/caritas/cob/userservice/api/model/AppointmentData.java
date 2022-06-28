package de.caritas.cob.userservice.api.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AppointmentData {
  private final String title;
  private final String userName;
  private final String counselor;
  private final String date;
  private final String duration;
}
