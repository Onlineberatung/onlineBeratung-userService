package de.caritas.cob.UserService.api.model;

import java.util.LinkedHashMap;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.annotations.ApiModel;
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

  private LinkedHashMap<String, Object> properties;

  @JsonAnyGetter
  public LinkedHashMap<String, Object> getProperties() {
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
