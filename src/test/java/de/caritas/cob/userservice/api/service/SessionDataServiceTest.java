package de.caritas.cob.userservice.api.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.UserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.sessionData.SessionData;
import de.caritas.cob.userservice.api.repository.sessionData.SessionDataRepository;
import de.caritas.cob.userservice.api.repository.user.User;

@RunWith(MockitoJUnitRunner.class)
public class SessionDataServiceTest {

  private final String USERNAME = "username";
  private final String USER_ID = "9b71cc46-650d-42bb-8299-f8e3f6d7249f";
  private final User USER = new User(USER_ID, USERNAME, "name@domain.de", null);
  private final String CONSULTANT_ID = "1b71cc46-650d-42bb-8299-f8e3f6d7249a";
  private final String CONSULTANT_ROCKETCHAT_ID = "xN3Mobksn3xdp7gEk";
  private final Consultant CONSULTANT =
      new Consultant(CONSULTANT_ID, CONSULTANT_ROCKETCHAT_ID, "consultant", "first name",
          "last name", "consultant@cob.de", false, false, null, false, null, null, null);
  private final Session INITALIZED_SESSION = new Session(1L, USER, CONSULTANT, ConsultingType.SUCHT,
      "99999", 0L, SessionStatus.INITIAL, null, null);
  private final UserDTO USER_DTO = new UserDTO(USERNAME, "99999", 99L, "xyz", "x@y.de", null, null,
      null, null, null, "true", "0");


  @InjectMocks
  private SessionDataService sessionDataService;
  @Mock
  private SessionDataRepository sessionDataRepository;
  @Mock
  private LogService logService;
  @Mock
  private SessionDataHelper sessionDataHelper;

  @Test
  public void createSessionDataList_Should_SaveSessionData() {

    sessionDataService.saveSessionDataFromRegistration(INITALIZED_SESSION, USER_DTO);
    verify(sessionDataRepository, times(1)).saveAll(Mockito.any());
  }

  @Test
  public void createSessionDataList_Should_LogAndThrowServiceException_WhenSaveSessionDataFails() {

    when(sessionDataHelper.createRegistrationSessionDataList(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<SessionData>());

    @SuppressWarnings("serial")
    DataAccessException dataAccessException = new DataAccessException("reson") {};
    when(sessionDataRepository.saveAll(Mockito.any())).thenThrow(dataAccessException);

    try {
      sessionDataService.saveSessionDataFromRegistration(INITALIZED_SESSION, USER_DTO);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(logService, times(1)).logDatabaseError(dataAccessException);
  }

  @Test
  public void createSessionDataList_Should_LogAndThrowIllegalArgumentException_WhenProvidedSessionIsNull() {

    when(sessionDataHelper.createRegistrationSessionDataList(Mockito.any(), Mockito.any()))
        .thenReturn(null);

    @SuppressWarnings("serial")
    IllegalArgumentException illegalArgumentException = new IllegalArgumentException("reson") {};
    when(sessionDataRepository.saveAll(Mockito.any())).thenThrow(illegalArgumentException);

    try {
      sessionDataService.saveSessionDataFromRegistration(INITALIZED_SESSION, USER_DTO);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }

    verify(logService, times(1)).logDatabaseError(illegalArgumentException);
  }

}
