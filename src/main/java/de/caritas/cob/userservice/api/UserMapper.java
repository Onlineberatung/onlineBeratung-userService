package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

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
}
