package de.caritas.cob.userservice.api.deleteworkflow.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionOrder {

  FIRST(0),
  SECOND(1),
  THIRD(2),
  FOURTH(3);

  private final int order;

}
