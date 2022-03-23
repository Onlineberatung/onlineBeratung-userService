package de.caritas.cob.userservice.api.adapters.web.dto;

import java.util.LinkedHashMap;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.annotations.ApiModel;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/***
 * Generic Monitoring model
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel()
public class MonitoringDTO {

  private Map<String, Object> properties;

  @JsonAnyGetter
  public Map<String, Object> getProperties() {
    return properties;
  }

  @JsonAnySetter
  public void addProperties(String key, Object value) {
    if (this.properties == null) {
      this.properties = new LinkedHashMap<>();
    }

    this.properties.put(key, value);
  }
}
