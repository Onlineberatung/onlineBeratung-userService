package de.caritas.cob.userservice.api.helper;

import java.util.LinkedHashMap;
import java.util.Map;

public class MonitoringStructureBuilder {

  private final Map<String, Object> monitoringStructure;

  private MonitoringStructureBuilder() {
    this.monitoringStructure = new LinkedHashMap<>();
  }

  static MonitoringStructureBuilder getInstance() {
    return new MonitoringStructureBuilder();
  }

  MonitoringStructureBuilder addEntry(String key, Object value) {
    this.monitoringStructure.put(key, value);
    return this;
  }

  MonitoringStructureBuilder addSubLevel(String name, Map<String, Object> childLevel) {
    monitoringStructure.put(name, childLevel);
    return this;
  }

  public Map<String, Object> getMonitoringStructure() {
    return this.monitoringStructure;
  }
}
