package de.caritas.cob.userservice.api.port.in;

import de.caritas.cob.userservice.api.model.User;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface AccountManaging {

  Optional<Map<String, Object>> patchUser(Map<String, Object> patchMap);

  Optional<Map<String, Object>> findAdviceSeeker(String id);

  Optional<User> findAdviceSeekerByChatUserId(String chatId);

  Optional<Map<String, Object>> findConsultant(String id);

  Optional<Map<String, Object>> findConsultantByUsername(String username);

  Map<String, Object> findConsultantsByInfix(
      String infix,
      boolean shouldFilterByAgencies,
      Collection<Long> agenciesToFilterConsultants,
      int pageNumber,
      int pageSize,
      String fieldName,
      boolean isAscending);

  boolean isTeamAdvisedBy(Long sessionId, String consultantId);
}
