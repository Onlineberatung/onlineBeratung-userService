package de.caritas.cob.userservice.api.port.in;

import de.caritas.cob.userservice.api.model.User;
import java.util.Map;
import java.util.Optional;

public interface AccountManaging {

  Optional<Map<String, Object>> patchUser(Map<String, Object> patchMap);

  boolean existsAdviceSeeker(String id);

  Optional<User> findAdviceSeeker(String id);

  Optional<User> findAdviceSeekerByChatUserId(String chatId);

  Optional<Map<String, Object>> findConsultant(String id);

  Optional<Map<String, Object>> findConsultantByUsername(String username);

  Map<String, Object> findConsultantsByInfix(
      String infix, int pageNumber, int pageSize, String fieldName, boolean isAscending
  );
}
