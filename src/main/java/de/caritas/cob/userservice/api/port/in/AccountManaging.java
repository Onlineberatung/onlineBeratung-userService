package de.caritas.cob.userservice.api.port.in;

import java.util.Map;
import java.util.Optional;

public interface AccountManaging {

  Optional<Map<String, Object>> patchUser(Map<String, Object> patchMap);
}
