package de.caritas.cob.userservice.api.container;

import de.caritas.cob.userservice.api.service.session.SessionFilter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionListQueryParameter {

  private int sessionStatus;
  private int offset;
  private int count;
  SessionFilter sessionFilter;
}
