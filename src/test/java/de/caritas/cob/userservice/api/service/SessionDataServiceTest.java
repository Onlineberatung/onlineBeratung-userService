package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.model.validation.MandatoryFieldsValidator;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionDataServiceTest {

  @InjectMocks
  private SessionDataService sessionDataService;
  @Mock
  private SessionDataRepository sessionDataRepository;
  @Mock
  private SessionDataProvider sessionDataProvider;
  @Mock
  private SessionService sessionService;
  @Mock
  private MandatoryFieldsValidator mandatoryFieldsValidator;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void createSessionDataList_Should_SaveValidatedSessionData() {
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    Session session = easyRandom.nextObject(Session.class);
    when(sessionService.getSession(any())).thenReturn(Optional.of(session));

    sessionDataService.saveSessionData(SESSION_ID, sessionData);

    verify(mandatoryFieldsValidator, times(1)).validateFields(any(), any());
    verify(sessionDataProvider, times(1)).createSessionDataList(any(), any());
    verify(sessionDataRepository, times(1)).saveAll(any());
  }

  @Test(expected = NotFoundException.class)
  public void createSessionDataList_Should_ThrowNotFoundException_When_SessionDoesNotExist() {
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    when(sessionService.getSession(any())).thenReturn(Optional.empty());

    sessionDataService.saveSessionData(SESSION_ID, sessionData);
  }
}
