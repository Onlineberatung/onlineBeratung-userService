package de.caritas.cob.userservice.api.service;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.MonitoringStructureProvider;
import de.caritas.cob.userservice.api.model.monitoring.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.port.out.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.monitoringoption.MonitoringOption;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Service for {@link Monitoring}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

  private final @NonNull MonitoringRepository monitoringRepository;
  private final @NonNull MonitoringStructureProvider monitoringStructureProvider;

  /**
   * Creates and inserts the initial monitoring data for the given {@link Session} into the database
   * if monitoring is activated for the given {@link ExtendedConsultingTypeResponseDTO}.
   *
   * @param session                           {@link Session}
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   * @throws CreateMonitoringException @link CreateMonitoringException}
   */
  public void createMonitoringIfConfigured(Session session,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO)
      throws CreateMonitoringException {

    var monitoring = extendedConsultingTypeResponseDTO.getMonitoring();
    if (nonNull(session) && nonNull(monitoring) && isTrue(monitoring.getInitializeMonitoring())) {
      try {
        updateMonitoring(session.getId(),
            monitoringStructureProvider.getMonitoringInitialList(session.getConsultingTypeId()));
      } catch (Exception exception) {
        CreateEnquiryExceptionInformation exceptionInformation = CreateEnquiryExceptionInformation
            .builder().session(session).rcGroupId(session.getGroupId()).build();
        throw new CreateMonitoringException(
            String.format("Could not create monitoring for session %s with consultingType %s",
                session.getId(), extendedConsultingTypeResponseDTO.getId()),
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
      return new MonitoringDTO(convertToMonitoringMap(monitoring, session.getConsultingTypeId()));

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while saving monitoring data.",
          LogService::logDatabaseError);
    }
  }

  /**
   * Updates the monitoring values of a {@link Session}.
   *
   * @param sessionId     the session id
   * @param monitoringDTO the {@link MonitoringDTO} of the {@link Session}
   */
  public void updateMonitoring(Long sessionId, MonitoringDTO monitoringDTO) {

    try {
      List<Monitoring> monitoringList =
          monitoringStructureProvider.createMonitoringList(monitoringDTO, sessionId);

      monitoringRepository.saveAll(monitoringList);

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while saving monitoring data.",
          LogService::logDatabaseError);
    }
  }

  /**
   * Deletes the monitoring values of a {@link Session}.
   *
   * @param sessionId     the session id
   * @param monitoringDTO the {@link MonitoringDTO} of the {@link Session}
   */
  public void deleteMonitoring(Long sessionId, MonitoringDTO monitoringDTO) {

    try {
      List<Monitoring> monitoringList =
          monitoringStructureProvider.createMonitoringList(monitoringDTO, sessionId);

      monitoringRepository.deleteAll(monitoringList);

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while deleting monitoring data.",
          LogService::logDatabaseError);
    }
  }

  private Map<String, Object> convertToMonitoringMap(List<Monitoring> monitoringList,
      int consultingTypeId) {

    Map<String, Object> map = new LinkedHashMap<>();

    if (nonNull(monitoringList)) {
      for (MonitoringType type : MonitoringType.values()) {
        if (type.getConsultingTypeId() == consultingTypeId) {
          map.put(type.getKey(), convertToMonitoring(type, monitoringList));
        }
      }
    }

    return monitoringStructureProvider.sortMonitoringMap(map, consultingTypeId);
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
            monitoringStructureProvider.getMonitoringInitialList(session.getConsultingTypeId()));

      } catch (InternalServerErrorException ex) {
        log.error("Internal Server Error: Error during monitoring rollback. Monitoring data could "
            + "not be deleted for session: {}", session, ex);
      }
    }
  }
}
