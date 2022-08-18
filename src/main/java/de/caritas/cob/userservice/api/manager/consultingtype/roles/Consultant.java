package de.caritas.cob.userservice.api.manager.consultingtype.roles;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Role settings of the {@link ExtendedConsultingTypeResponseDTO} for consultants */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Consultant {

  private LinkedHashMap<String, List<String>> roleSets;

  @JsonAnyGetter
  public Map<String, List<String>> getRoleSets() {
    return roleSets;
  }

  @JsonAnySetter
  public void addRoleNames(String key, List<String> value) {
    if (this.roleSets == null) {
      this.roleSets = new LinkedHashMap<>();
    }

    this.roleSets.put(key, value);
  }
}
