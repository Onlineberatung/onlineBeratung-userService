package de.caritas.cob.userservice.api.service;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.helper.MonitoringHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.monitoringOption.MonitoringOption;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;

/**
 * Service for {@link Monitoring}
 */
@Service
public class MonitoringService {

  private final MonitoringRepository monitoringRepository;
  private final MonitoringHelper monitoringHelper;

  @Autowired
  public MonitoringService(MonitoringRepository monitoringRepository,
      MonitoringHelper monitoringHelper) {
    this.monitoringRepository = monitoringRepository;
    this.monitoringHelper = monitoringHelper;
  }

  /**
   * Creates and inserts the initial monitoring data for the given {@link Session} into the database
   * if monitoring is activated for the given {@link ConsultingTypeSettings}.
   *
   * @param session {@link Session}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   * @throws CreateMonitoringException @link CreateMonitoringException}
   */
  public void createMonitoringIfConfigured(Session session, ConsultingTypeSettings consultingTypeSettings)
      throws CreateMonitoringException {

    if (nonNull(session) && consultingTypeSettings.isMonitoring()) {
      try {
        updateMonitoring(session.getId(),
            monitoringHelper.getMonitoringInitalList(session.getConsultingType()));
      } catch (Exception exception) {
        CreateEnquiryExceptionInformation exceptionInformation = CreateEnquiryExceptionInformation
            .builder().session(session).rcGroupId(session.getGroupId()).build();
        throw new CreateMonitoringException(
            String.format("Could not create monitoring for session %s with consultingType %s",
                session.getId(), consultingTypeSettings.getConsultingType()),
            exception, exceptionInformation);
      }
    }
  }

  /**
   * Returns the monitoring for the given session.
   *
   * @param session the {@link Session}
   * @return the {@link MonitoringDTO} for the {@link Session}
   */
  public MonitoringDTO getMonitoring(Session session) {

    try {
      List<Monitoring> monitoring = monitoringRepository.findBySessionId(session.getId());
      return new MonitoringDTO(convertToMonitoringMap(monitoring, session.getConsultingType()));

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while saving monitoring data.",
          LogService::logDatabaseError);
    }
  }

  /**
   * Updates the monitoring values of a {@link Session}.
   *
   * @param sessionId the session id
   * @param monitoringDTO the {@link MonitoringDTO} of the {@link Session}
   */
  public void updateMonitoring(Long sessionId, MonitoringDTO monitoringDTO) {

    try {
      List<Monitoring> monitoringList =
          monitoringHelper.createMonitoringList(monitoringDTO, sessionId);

      monitoringRepository.saveAll(monitoringList);

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while saving monitoring data.",
          LogService::logDatabaseError);
    }
  }

  /**
   * Deletes the monitoring values of a {@link Session}.
   *
   * @param sessionId the session id
   * @param monitoringDTO the {@link MonitoringDTO} of the {@link Session}
   */
  public void deleteMonitoring(Long sessionId, MonitoringDTO monitoringDTO) {

    try {
      List<Monitoring> monitoringList =
          monitoringHelper.createMonitoringList(monitoringDTO, sessionId);

      monitoringRepository.deleteAll(monitoringList);

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while deleting monitoring data.",
          LogService::logDatabaseError);
    }
  }

  private LinkedHashMap<String, Object> convertToMonitoringMap(List<Monitoring> monitoringList,
      ConsultingType consultingType) {

    LinkedHashMap<String, Object> map = new LinkedHashMap<>();

    if (nonNull(monitoringList)) {
      for (MonitoringType type : MonitoringType.values()) {
        if (type.getConsultingType().equals(consultingType)) {
          map.put(type.getKey(),
              convertToMonitoring(type, monitoringList));
        }
      }
    }

    return monitoringHelper.sortMonitoringMap(map, consultingType);
  }

  private LinkedHashMap<String, Object> convertToMonitoring(MonitoringType type,
      List<Monitoring> monitoringList) {

    LinkedHashMap<String, Object> map = new LinkedHashMap<>();

    for (Monitoring monitoring : monitoringList) {
      if (monitoring.getMonitoringType().getKey().equals(type.getKey())) {
        map.put(monitoring.getKey(), nonNull(monitoring.getValue()) ? monitoring.getValue()
            : convertToMonitoringOption(type, monitoring.getKey(), monitoringList));
      }
    }

    return map;
  }

  private LinkedHashMap<String, Object> convertToMonitoringOption(MonitoringType type,
      String monitoringKey, List<Monitoring> monitoringList) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();

    for (Monitoring monitoring : monitoringList) {
      if (monitoring.getMonitoringType().getKey().equals(type.getKey())
          && monitoringKey.equals(monitoring.getKey())) {
        for (MonitoringOption option : monitoring.getMonitoringOptionList()) {
          map.put(option.getKey(), option.getValue());
        }
      }
    }

    return map;
  }

  /**
   * Roll back the initialization of the monitoring data for a {@link Session}.
   *
   * @param session {@link Session}
   */
  public void rollbackInitializeMonitoring(Session session) {
    if (nonNull(session)) {
      try {
        deleteMonitoring(session.getId(),
            monitoringHelper.getMonitoringInitalList(session.getConsultingType()));

      } catch (InternalServerErrorException ex) {
        LogService.logInternalServerError(String.format(
            "Error during monitoring rollback. Monitoring data could not be deleted for session: %s",
            session.toString()), ex);
      }
    }
  }
}
