package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserServiceMapper {

  public Map<String, Object> mapOf(User user) {
    return new HashMap<>() {
      {
        put("id", user.getUserId());
        put("username", user.getUsername());
        put("email", user.getEmail());
        put("encourage2fa", user.getEncourage2fa());
      }
    };
  }

  public Map<String, Object> mapOf(Consultant consultant) {
    return new HashMap<>() {
      {
        put("id", consultant.getId());
        put("firstName", consultant.getFirstName());
        put("lastName", consultant.getLastName());
        put("email", consultant.getEmail());
        put("encourage2fa", consultant.getEncourage2fa());
      }
    };
  }

  @SuppressWarnings("unchecked")
  public List<String> bannedUsernamesOfMap(Map<String, Object> chatMetaInfoMap) {
    return (List<String>) chatMetaInfoMap.get("mutedUsers");
  }
}
