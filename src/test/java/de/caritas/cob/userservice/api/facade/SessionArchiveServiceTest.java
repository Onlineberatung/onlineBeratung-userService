package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionArchiveServiceTest {

  @InjectMocks
  SessionArchiveService sessionArchiveService;
  @Mock
  SessionRepository sessionRepository;
  @Mock
  ConsultantAgencyRepository consultantAgencyRepository;
  @Mock
  AuthenticatedUser authenticatedUser;

  @Before
  public void setup() {
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
  }

  @Test(expected = NotFoundException.class)
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test(expected = ConflictException.class)
  public void archiveSession_Should_ThrowConflictException_WhenSessionIsNotInProgress() {

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.NEW);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoAuthorizationForTheSession() {

    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getId()).thenReturn(CONSULTANT_ID_2);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getConsultant()).thenReturn(consultant);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenSessionIsNotTeamSessionAndConsultantNotAssigned() {

    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getId()).thenReturn(CONSULTANT_ID_2);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getConsultant()).thenReturn(consultant);
    when(session.isTeamSession()).thenReturn(false);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenSessionIsAssignedToConsultant() {

    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getId()).thenReturn(CONSULTANT_ID);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getConsultant()).thenReturn(consultant);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenSessionIsTeamSessionAndConsultantInAgency() {

    Consultant consultant = Mockito.mock(Consultant.class);
    when(consultant.getId()).thenReturn(CONSULTANT_ID_2);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getConsultant()).thenReturn(consultant);
    when(session.getAgencyId()).thenReturn(AGENCY_ID);
    when(session.isTeamSession()).thenReturn(true);

    ConsultantAgency consultantAgency = Mockito.mock(ConsultantAgency.class);
    when(consultantAgency.getAgencyId()).thenReturn(AGENCY_ID);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultantAgencyRepository.findByConsultantId(CONSULTANT_ID)).thenReturn(
        Collections.singletonList(consultantAgency));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

}
