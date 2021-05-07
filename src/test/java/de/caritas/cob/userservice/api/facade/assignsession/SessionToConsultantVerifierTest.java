package de.caritas.cob.userservice.api.facade.assignsession;

import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import org.jeasy.random.EasyRandom;
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

  @Test
  public void verifyPreconditionsForAssignment_Should_notThrowException_When_anonymousSessionIsValid() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);
    session.setConsultingType(ConsultingType.U25);
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    AgencyDTO u25AgencyDTO = new AgencyDTO().consultingType(ConsultingType.U25);
    AgencyDTO otherAgencyDTO = new AgencyDTO().consultingType(ConsultingType.SUCHT);
    ConsultantAgency u25ConsultantAgency = mock(ConsultantAgency.class);
    when(u25ConsultantAgency.getAgency()).thenReturn(u25AgencyDTO);
    ConsultantAgency otherConsultantAgency = mock(ConsultantAgency.class);
    when(otherConsultantAgency.getAgency()).thenReturn(otherAgencyDTO);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(u25ConsultantAgency, otherConsultantAgency));

    assertDoesNotThrow(() -> new SessionToConsultantVerifier(session, consultant)
        .verifyPreconditionsForAssignment());
  }

  @Test(expected = ForbiddenException.class)
  public void verifyPreconditionsForAssignment_Should_notThrowException_When_anonymousSessionHasNotConsultingType() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);
    session.setConsultingType(ConsultingType.U25);
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(null);

    new SessionToConsultantVerifier(session, consultant).verifyPreconditionsForAssignment();
  }

}
