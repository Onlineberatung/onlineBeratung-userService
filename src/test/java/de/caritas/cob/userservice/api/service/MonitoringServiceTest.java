package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.repository.session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WIT_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.POSTCODE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.MonitoringStructureProvider;
import de.caritas.cob.userservice.api.model.monitoring.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringServiceTest {

  @Spy
  @InjectMocks
  private MonitoringService monitoringService;
  @Mock
  private MonitoringRepository monitoringRepository;
  @Mock
  private MonitoringStructureProvider monitoringStructureProvider;
  @Mock
  private Logger logger;

  private final String ERROR = "error";
  private final Long SESSION_ID = 123L;
  private final Session SESSION =
      new Session(SESSION_ID, null, null, CONSULTING_TYPE_ID_SUCHT, REGISTERED, POSTCODE, null,
          IN_PROGRESS, null, null,
          null, null, false, false, null, null);
  private final MonitoringDTO MONITORING_DTO = new MonitoringDTO();

  @Before
  public void setUp() {
    HashMap<String, Object> drugsMap = new HashMap<>();
    drugsMap.put("others", false);
    HashMap<String, Object> addictiveDrugsMap = new HashMap<>();
    addictiveDrugsMap.put("drugs", drugsMap);
    MONITORING_DTO.addProperties("addictiveDrugs", addictiveDrugsMap);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: updateMonitoring Role: consultant
   */

  @Test
  public void updateMonitoring_Should_ThrowInternalServerErrorException_OnDatabaseError() {

    DataAccessException ex = new DataAccessException(ERROR) {
    };

    when(monitoringRepository.saveAll(Mockito.any())).thenThrow(ex);

    try {
      monitoringService.updateMonitoring(SESSION_ID, MONITORING_DTO);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void updateMonitoring_Should_SaveMonitoringData() {

    monitoringService.updateMonitoring(SESSION_ID, MONITORING_DTO);

    verify(monitoringRepository, times(1)).saveAll(Mockito.any());

  }

  /**
   * Method: deleteMonitoring Role: consultant
   */

  @Test
  public void deleteMonitoring_Should_ThrowInternalServerErrorException_OnDatabaseError() {

    DataAccessException ex = new DataAccessException(ERROR) {
    };

    doThrow(ex).when(monitoringRepository).deleteAll(Mockito.any());

    try {
      monitoringService.deleteMonitoring(SESSION_ID, MONITORING_DTO);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void deleteMonitoring_Should_DeleteMonitoringData() {

    monitoringService.deleteMonitoring(SESSION_ID, MONITORING_DTO);

    verify(monitoringRepository, times(1)).deleteAll(Mockito.any());

  }

  /**
   * Method: crateMonitoring
   */

  @Test
  public void createMonitoring_Should_UpdateMonitoring_WithInitialMonitoringListOfSessionsConsultingType()
      throws CreateMonitoringException {

    doReturn(MONITORING_DTO).when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());

    monitoringService
        .createMonitoringIfConfigured(SESSION, CONSULTING_TYPE_SETTINGS_WIT_MONITORING);

    verify(monitoringService, times(1)).updateMonitoring(SESSION_ID, MONITORING_DTO);
    verify(monitoringStructureProvider, times(1))
        .getMonitoringInitialList(SESSION.getConsultingTypeId());

  }

  /**
   * Method: deleteInitialMonitoring
   */

  @Test
  public void deleteInitialMonitoring_Should_DeleteMonitoring_WithInitialMonitoringListOfSessionsConsultingType() {

    doReturn(MONITORING_DTO).when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());

    monitoringService.rollbackInitializeMonitoring(SESSION);

    verify(monitoringService, times(1)).deleteMonitoring(SESSION_ID, MONITORING_DTO);
    verify(monitoringStructureProvider, times(1))
        .getMonitoringInitialList(SESSION.getConsultingTypeId());

  }
}
