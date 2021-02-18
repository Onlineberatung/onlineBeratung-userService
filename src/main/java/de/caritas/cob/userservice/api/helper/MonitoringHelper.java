package de.caritas.cob.userservice.api.helper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.InitializeMonitoringException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.monitoring.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.monitoringoption.MonitoringOption;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builder class to provide sorted monitoring structure representation.
 */
@Component
@RequiredArgsConstructor
public class MonitoringHelper {

  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Returns a list of {@link Monitoring} objects for the given {@link MonitoringDTO} and {@link
   * Session}.
   *
   * @param monitoringDTO the given {@link MonitoringDTO}
   * @param sessionId     the given id of the session
   * @return the created monitoring {@link List}
   */
  public List<Monitoring> createMonitoringList(MonitoringDTO monitoringDTO, Long sessionId) {

    if (nonNull(monitoringDTO) && nonNull(sessionId)) {
      return createMonitoringList(monitoringDTO.getProperties(), sessionId, null, 0, null, null,
          null);
    }
    return emptyList();
  }

  private List<Monitoring> createMonitoringList(Map<String, Object> map, Long sessionId,
      MonitoringType type, int level, Monitoring monitoring, MonitoringOption option,
      List<Monitoring> monitoringList) {

    if (isNull(monitoringList)) {
      monitoringList = new ArrayList<>();
    }

    for (Map.Entry<String, Object> entry : map.entrySet()) {

      switch (level) {
        case 0:
          type = getMonitoringType(entry.getKey());
          monitoring = null;
          option = null;
          break;

        case 1:
          if (nonNull(entry.getValue())) {
            option = null;

            if (entry.getValue() instanceof Boolean) {
              monitoringList
                  .add(new Monitoring(sessionId, type, entry.getKey(), (Boolean) entry.getValue()));
              monitoring = null;
            } else {
              monitoring = new Monitoring(sessionId, type, entry.getKey(), null,
                  new ArrayList<>());
              monitoringList.add(monitoring);
            }
          }
          break;

        case 2:
          if (nonNull(entry.getValue()) && entry.getValue() instanceof Boolean
              && monitoring != null) {
            option = new MonitoringOption(sessionId, type, monitoring.getKey(), entry.getKey(),
                (Boolean) entry.getValue(), monitoring);
          }
          break;

        default:
          break;
      }

      if (nonNull(monitoring) && nonNull(option)) {
        monitoring.getMonitoringOptionList().add(option);
      }

      if (entry.getValue() instanceof Map) {
        createMonitoringList((Map<String, Object>) entry.getValue(), sessionId, type,
            level + 1, monitoring, option, monitoringList);
      }

    }

    return monitoringList;
  }

  /**
   * Creates the initial monitoring data of a session for the given {@link ConsultingType}. The
   * structure (JSON) is being imported from the JSON file provided in the {@link
   * ConsultingTypeSettings}.
   */
  public MonitoringDTO getMonitoringInitalList(ConsultingType consultingType) {
    MonitoringDTO monitoring;
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<MonitoringDTO> typeReference = new TypeReference<MonitoringDTO>() {
    };
    InputStream inputStream = getMonitoringJSONStream(consultingType);
    try {
      monitoring = mapper.readValue(inputStream, typeReference);
    } catch (IOException ex) {
      throw new InitializeMonitoringException(ex);
    }

    return monitoring;
  }

  /**
   * Returns the path of the monitoring JSON file according to the provided {@link ConsultingType}.
   *
   * @param consultingType the {@link ConsultingType} to load the json file for
   * @return the {@link InputStream} containing the json content
   */
  private InputStream getMonitoringJSONStream(ConsultingType consultingType) {
    String monitoringFilePath = consultingTypeManager.getConsultingTypeSettings(consultingType)
        .getMonitoringFile();
    try {
      return TypeReference.class.getResourceAsStream(monitoringFilePath);
    } catch (NullPointerException e) {
      throw new InternalServerErrorException(String
          .format("Stream for monitoring json file with path \" %s \" can not be opened",
              monitoringFilePath), e, LogService::logInternalServerError);
    }
  }

  /**
   * Returns the corresponding {@link MonitoringType} for the given key String
   */
  private MonitoringType getMonitoringType(String key) {
    for (MonitoringType type : MonitoringType.values()) {
      if (type.getKey().contains(key)) {
        return type;
      }
    }
    return null;
  }

  /**
   * Returns a sorted {@link Map} of monitoring items according to the order that is defined in the
   * monitoring JSON file.
   *
   * @param unsortedMap    the {@link Map} before sorting
   * @param consultingType the {@link ConsultingType} to use for sorting
   * @return the sorted {@link Map}
   */
  public Map<String, Object> sortMonitoringMap(Map<String, Object> unsortedMap,
      ConsultingType consultingType) {

    Map<String, Object> sortedMap =
        getMonitoringInitalList(consultingType).getProperties();
    setValuesForSortedMonitoringMap(sortedMap, unsortedMap);

    return sortedMap;
  }

  private void setValuesForSortedMonitoringMap(Map<String, Object> sortedConfiguration,
      Map<String, Object> loadedInput) {
    sortedConfiguration.entrySet()
        .forEach(entry -> handleEntryValueType(loadedInput, entry));
  }

  @SuppressWarnings("unchecked")
  private void handleEntryValueType(Map<String, Object> loadedInput,
      Entry<String, Object> configEntry) {
    if (configEntry.getValue() instanceof Map) {
      setValuesForSortedMonitoringMap((Map<String, Object>) configEntry.getValue(), loadedInput);
    } else if (configEntry.getValue() instanceof Boolean) {
      Boolean value = findValueForKeyName(configEntry.getKey(), loadedInput);
      configEntry.setValue(value);
    }
  }

  @SuppressWarnings("unchecked")
  private Boolean findValueForKeyName(String key, Map<String, Object> loadedInput) {
    if (loadedInput.containsKey(key)) {
      return (Boolean) loadedInput.get(key);
    }
    for (Entry<String, Object> loadedEntry : loadedInput.entrySet()) {
      if (loadedEntry.getValue() instanceof Map) {
        return findValueForKeyName(key, (Map<String, Object>) loadedEntry.getValue());
      }
    }
    return false;
  }

}
