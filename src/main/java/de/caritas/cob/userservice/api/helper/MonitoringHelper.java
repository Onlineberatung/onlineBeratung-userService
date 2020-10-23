package de.caritas.cob.userservice.api.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.InitializeMonitoringException;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.monitoringOption.MonitoringOption;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Component
public class MonitoringHelper {

  private ConsultingTypeManager consultingTypeManager;

  @Autowired
  public MonitoringHelper(ConsultingTypeManager consultingTypeManager) {
    this.consultingTypeManager = consultingTypeManager;
  }

  /**
   * Returns a list of {@link Monitoring} objects for the given {@link MonitoringDTO} and
   * {@link Session}
   * 
   * @param monitoringDTO
   * @param sessionId
   * @return
   */
  public List<Monitoring> createMonitoringList(MonitoringDTO monitoringDTO, Long sessionId) {

    if (monitoringDTO != null && sessionId != null) {
      return createMonitoringList(monitoringDTO.getProperties(), sessionId, null, 0, null, null,
          null);
    }
    return null;
  }

  /**
   * Create a list of {@link Monitoring} objects recursively for the given {@link MonitoringDTO} and
   * {@link Session}
   * 
   * @param map
   * @param sessionId
   * @param type
   * @param level
   * @param monitoring
   * @param option
   * @param monitoringList
   * @return
   */
  @SuppressWarnings("unchecked")
  private List<Monitoring> createMonitoringList(LinkedHashMap<String, Object> map, Long sessionId,
      MonitoringType type, int level, Monitoring monitoring, MonitoringOption option,
      List<Monitoring> monitoringList) {

    if (monitoringList == null) {
      monitoringList = new ArrayList<Monitoring>();
    }

    for (Map.Entry<String, Object> entry : map.entrySet()) {

      switch (level) {
        case 0:
          type = getMonitoringType(entry.getKey());
          monitoring = null;
          option = null;
          break;

        case 1:
          if (entry.getValue() != null) {
            option = null;

            if (entry.getValue() instanceof Boolean) {
              monitoringList
                  .add(new Monitoring(sessionId, type, entry.getKey(), (Boolean) entry.getValue()));
              monitoring = null;
            } else {
              monitoring = new Monitoring(sessionId, type, entry.getKey(), null,
                  new ArrayList<MonitoringOption>());
              monitoringList.add(monitoring);
            }
          }
          break;

        case 2:
          if (entry.getValue() != null) {
            if (entry.getValue() instanceof Boolean && monitoring != null) {
              option = new MonitoringOption(sessionId, type, monitoring.getKey(), entry.getKey(),
                  (Boolean) entry.getValue(), monitoring);
            }
          }
          break;

        default:
          break;
      }

      if (monitoring != null && option != null) {
        monitoring.getMonitoringOptionList().add(option);
      }

      if (entry.getValue() instanceof LinkedHashMap<?, ?>) {
        createMonitoringList((LinkedHashMap<String, Object>) entry.getValue(), sessionId, type,
            level + 1, monitoring, option, monitoringList);
      }

    }

    return monitoringList;
  }

  /**
   * Creates the initial monitoring data of a session for the given {@link ConsultingType}. The
   * structure (JSON) is being imported from the JSON file provided in the
   * {@link ConsultingTypeSettings}.
   * 
   * @return
   */
  public MonitoringDTO getMonitoringInitalList(ConsultingType consultingType) {
    MonitoringDTO monitoring;
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<MonitoringDTO> typeReference = new TypeReference<MonitoringDTO>() {};
    InputStream inputStream = getMonitoringJSONStream(consultingType);
    try {
      monitoring = mapper.readValue(inputStream, typeReference);
    } catch (IOException ex) {
      throw new InitializeMonitoringException(ex);
    }

    return monitoring;
  }

  /**
   * Returns the path of the monitoring JSON file according to the provided {@link ConsultingType}
   * 
   * @param consultingType
   * @return
   */
  private InputStream getMonitoringJSONStream(ConsultingType consultingType) {
    return TypeReference.class.getResourceAsStream(
        consultingTypeManager.getConsultantTypeSettings(consultingType).getMonitoringFile());
  }

  /**
   * Returns the corresponding {@link MonitoringType} for the given key String
   * 
   * @param key
   * @return
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
   * Returns a sorted {@link LinkedHashMap} of monitoring items according to the order that is
   * defined in the monitoring JSON file
   * 
   * @param unsortedMap
   * @param consultingType
   * @return
   */
  @SuppressWarnings("unchecked")
  public LinkedHashMap<String, Object> sortMonitoringMap(LinkedHashMap<String, Object> unsortedMap,
      ConsultingType consultingType) {

    List<DeleteEntry> deleteRecordList = new ArrayList<DeleteEntry>();
    LinkedHashMap<String, Object> sortedMap =
        getMonitoringInitalList(consultingType).getProperties();

    try {
      // Get root elements (level 0)
      for (Map.Entry<String, Object> rootEntry : sortedMap.entrySet()) {

        if (rootEntry.getValue() instanceof LinkedHashMap) {

          // Get parent elements (level 1)
          for (Map.Entry<String, Object> parentEntry : ((LinkedHashMap<String, Object>) rootEntry
              .getValue()).entrySet()) {

            if (parentEntry.getValue() instanceof LinkedHashMap) {

              // Get child elements (level 2)
              for (Map.Entry<String, Object> childEntry : ((LinkedHashMap<String, Object>) parentEntry
                  .getValue()).entrySet()) {

                if (childEntry.getValue() instanceof Boolean) {
                  // Update the sorted map value with the corresponding value of the unsorted map or
                  // remove this entry if it is not saved within this monitoring
                  Boolean unsortedChildValue =
                      (Boolean) ((LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) unsortedMap
                          .get(rootEntry.getKey())).get(parentEntry.getKey()))
                              .get(childEntry.getKey());
                  if (unsortedChildValue != null) {
                    childEntry.setValue(unsortedChildValue);

                  } else {
                    LinkedHashMap<String, Boolean> removeChildEntry =
                        (LinkedHashMap<String, Boolean>) ((LinkedHashMap<String, Object>) sortedMap
                            .get(rootEntry.getKey())).get(parentEntry.getKey());
                    deleteRecordList.add(new DeleteEntry(removeChildEntry, childEntry.getKey(),
                        childEntry.getValue()));
                  }
                }
              }

            } else if (parentEntry.getValue() instanceof Boolean) {
              // Update the sorted map value with the corresponding value of the unsorted map or
              // remove this entry if it is not saved within this monitoring
              Boolean unsortedParentValue =
                  (Boolean) ((LinkedHashMap<String, Object>) unsortedMap.get(rootEntry.getKey()))
                      .get(parentEntry.getKey());
              if (unsortedParentValue != null) {
                parentEntry.setValue(unsortedParentValue);
              } else {
                LinkedHashMap<String, Boolean> removeParentEntry =
                    (LinkedHashMap<String, Boolean>) sortedMap.get(rootEntry.getKey());
                deleteRecordList.add(new DeleteEntry(removeParentEntry, parentEntry.getKey(),
                    parentEntry.getValue()));
              }
            }
          }
        }
      }
    } catch (Exception exception) {
      LogService.logMonitoringHelperError(exception);
      return new LinkedHashMap<String, Object>();
    }

    deleteRecordList.forEach(entry -> {
      LinkedHashMap<String, Boolean> deleteEntry = entry.getEntry();
      deleteEntry.remove(entry.getKey(), entry.getValue());
    });

    return sortedMap;
  }

  /**
   * Object that holds the entries which should be deleted from the sorted monitoring map
   *
   */
  @AllArgsConstructor
  @Getter
  @Setter
  private class DeleteEntry {
    LinkedHashMap<String, Boolean> entry;
    String key;
    Object value;
  }
}
