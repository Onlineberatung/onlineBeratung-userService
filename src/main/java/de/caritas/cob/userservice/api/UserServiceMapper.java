package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceMapper {

  private final UsernameTranscoder usernameTranscoder;

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

  public Map<String, Object> mapOf(Consultant consultant, Map<String, Object> patchMap) {
    var map = new HashMap<String, Object>() {
      {
        put("id", consultant.getId());
        put("firstName", consultant.getFirstName());
        put("lastName", consultant.getLastName());
        put("email", consultant.getEmail());
        put("encourage2fa", consultant.getEncourage2fa());
      }
    };

    if (patchMap.containsKey("displayName")) {
      map.put("displayName", patchMap.get("displayName"));
    }

    return map;
  }

  @SuppressWarnings("unchecked")
  public List<String> bannedUsernamesOfMap(Map<String, Object> chatMetaInfoMap) {
    return (List<String>) chatMetaInfoMap.get("mutedUsers");
  }

  public Consultant consultantOf(Consultant consultant, Map<String, Object> patchMap) {
    if (patchMap.containsKey("email")) {
      consultant.setEmail((String) patchMap.get("email"));
    }
    if (patchMap.containsKey("firstName")) {
      consultant.setFirstName((String) patchMap.get("firstName"));
    }
    if (patchMap.containsKey("lastName")) {
      consultant.setLastName((String) patchMap.get("lastName"));
    }
    if (patchMap.containsKey("encourage2fa")) {
      consultant.setEncourage2fa((Boolean) patchMap.get("encourage2fa"));
    }

    return consultant;
  }

  public Optional<String> displayNameOf(Map<String, Object> patchMap) {
    if (patchMap.containsKey("displayName")) {
      var displayName = (String) patchMap.get("displayName");
      var encodedDisplayName = usernameTranscoder.encodeUsername(displayName);

      return Optional.of(encodedDisplayName);
    }

    return Optional.empty();
  }

  public User adviceSeekerOf(User adviceSeeker, Map<String, Object> patchMap) {
    if (patchMap.containsKey("email")) {
      adviceSeeker.setEmail((String) patchMap.get("email"));
    }
    if (patchMap.containsKey("encourage2fa")) {
      adviceSeeker.setEncourage2fa((Boolean) patchMap.get("encourage2fa"));
    }

    return adviceSeeker;
  }
}
