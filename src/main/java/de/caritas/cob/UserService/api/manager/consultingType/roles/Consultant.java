package de.caritas.cob.UserService.api.manager.consultingType.roles;

import java.util.LinkedHashMap;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Role settings of the {@link ConsultingTypeSettings} for consultants
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Consultant {

  private LinkedHashMap<String, List<String>> roleNames;

  @JsonAnyGetter
  public LinkedHashMap<String, List<String>> getRoleNames() {
    return roleNames;
  }

  @JsonAnySetter
  public void addRoleNames(String key, List<String> value) {
    if (this.roleNames == null) {
      this.roleNames = new LinkedHashMap<>();
    }

    this.roleNames.put(key, value);
  }
}
