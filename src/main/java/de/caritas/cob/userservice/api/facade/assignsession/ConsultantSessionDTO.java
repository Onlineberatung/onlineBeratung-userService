package de.caritas.cob.userservice.api.facade.assignsession;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConsultantSessionDTO {

  private Consultant consultant;
  private Session session;
}
