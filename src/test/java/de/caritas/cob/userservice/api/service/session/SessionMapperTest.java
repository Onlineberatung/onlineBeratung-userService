package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.model.Session;
import java.time.LocalDateTime;
import org.jeasy.random.EasyRandom;
import org.junit.Test;

public class SessionMapperTest {

  @Test
  public void
      convertToSessionDTO_Should_returnSessionDTOWithRegistrationType_When_registrationTypeIsAnonymous() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setRegistrationType(ANONYMOUS);

    SessionDTO sessionDTO = new SessionMapper().convertToSessionDTO(session);

    assertThat(sessionDTO.getRegistrationType(), is("ANONYMOUS"));
  }

  @Test
  public void
      convertToSessionDTO_Should_returnSessionDTOWithCreateDateInIsoFormat_When_registrationTypeIsAnonymous() {
    Session session = new EasyRandom().nextObject(Session.class);
    LocalDateTime createDate = new EasyRandom().nextObject(LocalDateTime.class);
    session.setCreateDate(createDate);

    SessionDTO sessionDTO = new SessionMapper().convertToSessionDTO(session);

    assertThat(sessionDTO.getCreateDate(), is(toIsoTime(createDate)));
  }

  @Test
  public void
      convertToSessionDTO_Should_returnSessionDTOWithRegistrationType_When_registrationTypeIsRegistered() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setRegistrationType(REGISTERED);

    SessionDTO sessionDTO = new SessionMapper().convertToSessionDTO(session);

    assertThat(sessionDTO.getRegistrationType(), is("REGISTERED"));
  }

  @Test
  public void convertToSessionDTO_Should_returnSessionDTOWithPeerChatInfo() {
    var session = new EasyRandom().nextObject(Session.class);

    var sessionDTO = new SessionMapper().convertToSessionDTO(session);

    assertThat(sessionDTO.getIsPeerChat(), is(session.isPeerChat()));
  }
}
