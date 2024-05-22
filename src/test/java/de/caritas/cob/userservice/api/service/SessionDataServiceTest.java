package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionDataDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionDataRepository;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionDataServiceTest {

  @InjectMocks private SessionDataService sessionDataService;
  @Mock private SessionDataRepository sessionDataRepository;
  @Mock private SessionDataProvider sessionDataProvider;
  @Mock private SessionService sessionService;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void saveSessionDataForSessionId_Should_ThrowNotFoundException_When_SessionNotFound() {
    assertThrows(
        NotFoundException.class,
        () -> {
          SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
          when(sessionService.getSession(any())).thenReturn(Optional.empty());

          sessionDataService.saveSessionData(SESSION_ID, sessionData);
        });
  }

  @Test
  public void saveSessionDataForSessionId_Should_SaveSessionData() {
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    Session session = easyRandom.nextObject(Session.class);
    when(sessionService.getSession(any())).thenReturn(Optional.of(session));

    sessionDataService.saveSessionData(SESSION_ID, sessionData);

    verify(sessionDataProvider, times(1)).createSessionDataList(any(), any());
    verify(sessionDataRepository, times(1)).saveAll(any());
  }

  @Test
  public void saveSessionData_Should_SaveValidatedSessionData() {
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    Session session = easyRandom.nextObject(Session.class);
    when(sessionService.getSession(any())).thenReturn(Optional.of(session));

    sessionDataService.saveSessionData(SESSION_ID, sessionData);

    verify(sessionDataProvider, times(1)).createSessionDataList(any(), any());
    verify(sessionDataRepository, times(1)).saveAll(any());
  }
}
