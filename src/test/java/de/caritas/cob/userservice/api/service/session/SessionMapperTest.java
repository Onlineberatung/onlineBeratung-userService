package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import org.jeasy.random.EasyRandom;
import org.junit.Test;

public class SessionMapperTest {

  @Test
  public void convertToSessionDTO_Should_returnSessionDTOWithRegistrationType_When_registrationTypeIsAnonymous() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setRegistrationType(ANONYMOUS);

    SessionDTO sessionDTO = new SessionMapper().convertToSessionDTO(session);

    assertThat(sessionDTO.getRegistrationType(), is("ANONYMOUS"));
  }

  @Test
  public void convertToSessionDTO_Should_returnSessionDTOWithRegistrationType_When_registrationTypeIsRegistered() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setRegistrationType(REGISTERED);

    SessionDTO sessionDTO = new SessionMapper().convertToSessionDTO(session);

    assertThat(sessionDTO.getRegistrationType(), is("REGISTERED"));
  }

}
