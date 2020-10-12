package de.caritas.cob.userservice.api.facade.assignsession;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import org.junit.Test;

public class SessionToConsultantVerifierTest {

  @Test(expected = ConflictException.class)
  public void verifySessionIsNotInProgress_Should_throwConflict_When_SessionIsInProgress() {
    Session session = new Session();
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setId(1L);
    Consultant consultant = new Consultant();
    consultant.setId("id");
    new SessionToConsultantVerifier(session, consultant).verifySessionIsNotInProgress();
  }

}
