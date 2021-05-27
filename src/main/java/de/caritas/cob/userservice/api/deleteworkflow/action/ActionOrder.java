package de.caritas.cob.userservice.api.deleteworkflow.action;

import static java.lang.Integer.MAX_VALUE;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionOrder {

  FIRST(0),
  SECOND(1),
  THIRD(2),
  FOURTH(3),
  FIFTH(4),
  LAST(MAX_VALUE);

  private final int order;

}
