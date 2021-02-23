package de.caritas.cob.userservice.api.helper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Structure mapping class to provide sorted monitoring structure representation.
 */
@Component
@RequiredArgsConstructor
public class MonitoringStructureProvider {

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
      return createMonitoringList(monitoringDTO.getProperties(), sessionId);
    }
    return emptyList();
  }

  private List<Monitoring> createMonitoringList(Map<String, Object> map, Long sessionId) {
    if (isNotEmpty(map)) {
      return map.entrySet().stream()
          .map(entry -> fromRootLevelEntry(entry, sessionId))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  @SuppressWarnings("unchecked")
  private List<Monitoring> fromRootLevelEntry(Entry<String, Object> entry, Long sessionId) {
    MonitoringType monitoringType = getMonitoringType(entry.getKey());
    if (entry.getValue() instanceof Map) {
      return fromSecondLevel((Map<String, Object>) entry.getValue(), sessionId, monitoringType);
    }
    return emptyList();
  }

  private List<Monitoring> fromSecondLevel(Map<String, Object> secondLevel, Long sessionId,
      MonitoringType monitoringType) {
    if (isNotEmpty(secondLevel)) {
      return secondLevel.entrySet().stream()
          .filter(entry -> nonNull(entry.getValue()))
          .map(entry -> fromSecondLevelEntry(entry, sessionId, monitoringType))
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  @SuppressWarnings("unchecked")
  private Monitoring fromSecondLevelEntry(Entry<String, Object> entry, Long sessionId,
      MonitoringType monitoringType) {
    if (entry.getValue() instanceof Boolean) {
      return new Monitoring(sessionId, monitoringType, entry.getKey(), (Boolean) entry.getValue());
    } else {
      Monitoring monitoring = new Monitoring(sessionId, monitoringType, entry.getKey(), null,
          new ArrayList<>());
      buildMonitoringOptions((Map<String, Object>) entry.getValue(), sessionId, monitoringType,
          monitoring);
      return monitoring;
    }
  }

  private void buildMonitoringOptions(Map<String, Object> thirdLevel, Long sessionId,
      MonitoringType monitoringType, Monitoring parentMonitoring) {
    thirdLevel.forEach((key, value) -> {
      MonitoringOption monitoringOption = new MonitoringOption(sessionId, monitoringType,
          parentMonitoring.getKey(), key, (Boolean) value, parentMonitoring);
      parentMonitoring.getMonitoringOptionList().add(monitoringOption);
    });
  }

  /**
   * Creates the initial monitoring data of a session for the given {@link ConsultingType}. The
   * structure (JSON) is being imported from the JSON file provided in the {@link
   * ConsultingTypeSettings}.
   *
   * @param consultingType the {@link ConsultingType} to load the initial monitoring
   * @return the generated {@link MonitoringDTO}
   */
  public MonitoringDTO getMonitoringInitialList(ConsultingType consultingType) {
    ObjectMapper mapper = new ObjectMapper();
    InputStream inputStream = getMonitoringJSONStream(consultingType);
    try {
      return mapper.readValue(inputStream, MonitoringDTO.class);
    } catch (IOException ex) {
      throw new InitializeMonitoringException(ex);
    }
  }

  private InputStream getMonitoringJSONStream(ConsultingType consultingType) {
    String monitoringFilePath = consultingTypeManager.getConsultingTypeSettings(consultingType)
        .getMonitoringFile();
    try {
      return TypeReference.class.getResourceAsStream(monitoringFilePath);
    } catch (NullPointerException e) {
      throw new InternalServerErrorException(String
          .format("Stream for monitoring json file with path \"%s\" can not be opened",
              monitoringFilePath), e, LogService::logInternalServerError);
    }
  }

  private MonitoringType getMonitoringType(String key) {
    return Stream.of(MonitoringType.values())
        .filter(type -> type.getKey().contains(key))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
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
        getMonitoringInitialList(consultingType).getProperties();
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
      setValuesForSortedMonitoringMap((Map<String, Object>) configEntry.getValue(),
          (Map<String, Object>) loadedInput.getOrDefault(configEntry.getKey(), emptyMap()));
    } else if (configEntry.getValue() instanceof Boolean) {
      Boolean value = findValueForKeyName(configEntry.getKey(), loadedInput);
      configEntry.setValue(value);
    }
  }

  @SuppressWarnings("unchecked")
  private Boolean findValueForKeyName(String key, Map<String, Object> loadedInput) {
    return loadedInput.containsKey(key) ? (Boolean) loadedInput.get(key) : Boolean.FALSE;
  }

}
