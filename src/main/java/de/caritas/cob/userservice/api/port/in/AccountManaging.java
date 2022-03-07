package de.caritas.cob.userservice.api.port.in;

import de.caritas.cob.userservice.api.model.User;
import java.util.Map;
import java.util.Optional;

public interface AccountManaging {

  Optional<Map<String, Object>> patchUser(Map<String, Object> patchMap);

  boolean existsAdviceSeeker(String id);

  Optional<User> findAdviceSeeker(String id);
}
