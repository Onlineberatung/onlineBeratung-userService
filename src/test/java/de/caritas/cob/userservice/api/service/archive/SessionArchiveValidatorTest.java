package de.caritas.cob.userservice.api.service.archive;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SessionArchiveValidatorTest {

  SessionArchiveValidator sessionArchiveValidator;

  @BeforeEach
  public void setup() {
    sessionArchiveValidator = new SessionArchiveValidator();
  }

  @Test
  void isValidForArchiving_ThrowConflictException_WhenSessionHasWrongState() {

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_ARCHIVE);
    when(session.getId()).thenReturn(1L);
    assertThrows(
        ConflictException.class, () -> sessionArchiveValidator.isValidForArchiving(session));
  }

  @Test
  void isValidForArchiving_Should_Not_ThrowConflictException_WhenSessionHasCorrectState() {

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    sessionArchiveValidator.isValidForArchiving(session);
    verify(session, times(1)).getStatus();
  }

  @Test
  void isValidForDearchiving_ThrowConflictException_WhenSessionHasWrongState() {

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getId()).thenReturn(1L);
    assertThrows(
        ConflictException.class, () -> sessionArchiveValidator.isValidForDearchiving(session));
  }

  @Test
  void isValidForDearchiving_Should_Not_ThrowConflictException_WhenSessionHasCorrectState() {

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_ARCHIVE);
    sessionArchiveValidator.isValidForDearchiving(session);
    verify(session, times(1)).getStatus();
  }
}
