package de.caritas.cob.userservice.api;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Appointment;
import de.caritas.cob.userservice.api.model.Appointment.AppointmentStatus;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Consultant.ConsultantBase;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceMapper {

  private static final String EMAIL = "email";
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";
  private static final String USERNAME = "username";
  private static final String STATUS = "status";

  private final UsernameTranscoder usernameTranscoder;

  public Map<String, Object> mapOf(Appointment appointment) {
    return new HashMap<>() {
      {
        put("id", appointment.getId().toString());
        put("description", appointment.getDescription());
        put("datetime", appointment.getDatetime().toString());
        put(STATUS, appointment.getStatus().toString().toLowerCase());
        put("consultantId", appointment.getConsultant().getId());
      }
    };
  }

  public Map<String, Object> mapOf(User user) {
    return new HashMap<>() {
      {
        put("id", user.getUserId());
        put(USERNAME, user.getUsername());
        put(EMAIL, user.getEmail());
        put("encourage2fa", user.getEncourage2fa());
      }
    };
  }

  public Map<String, Object> mapOf(Consultant consultant, Map<String, Object> additionalMap) {
    var map = new HashMap<String, Object>() {
      {
        put("id", consultant.getId());
        put(FIRST_NAME, consultant.getFirstName());
        put(LAST_NAME, consultant.getLastName());
        put(EMAIL, consultant.getEmail());
        put("encourage2fa", consultant.getEncourage2fa());
        put("walkThroughEnabled", consultant.getWalkThroughEnabled());
      }
    };

    if (additionalMap.containsKey("displayName")) {
      var displayName = (String) additionalMap.get("displayName");
      map.put("displayName", usernameTranscoder.decodeUsername(displayName));
    }

    return map;
  }

  public Map<String, Object> mapOf(Page<ConsultantBase> consultantPage,
      Iterable<Consultant> fullConsultants) {

    var consultants = new ArrayList<Map<String, String>>();
    var fullConsultantIterator = fullConsultants.iterator();
    consultantPage.forEach(consultantBase -> {
      var fullConsultant = fullConsultantIterator.next();
      var consultantMap = mapOf(consultantBase, fullConsultant);
      consultants.add(consultantMap);
    });

    return Map.of(
        "totalElements", (int) consultantPage.getTotalElements(),
        "isFirstPage", consultantPage.isFirst(),
        "isLastPage", consultantPage.isLast(),
        "consultants", consultants
    );
  }

  public Map<String, String> mapOf(ConsultantBase consultantBase, Consultant fullConsultant) {
    var status = isNull(fullConsultant.getStatus())
        ? ConsultantStatus.ERROR.toString()
        : fullConsultant.getStatus().toString();

    return Map.of(
        "id", consultantBase.getId(),
        EMAIL, consultantBase.getEmail(),
        FIRST_NAME, consultantBase.getFirstName(),
        LAST_NAME, consultantBase.getLastName(),
        STATUS, status,
        USERNAME, fullConsultant.getUsername()
    );
  }

  @SuppressWarnings("unchecked")
  public List<String> bannedUsernamesOfMap(Map<String, Object> chatMetaInfoMap) {
    return (List<String>) chatMetaInfoMap.get("mutedUsers");
  }

  public String consultantIdOf(Map<String, Object> appointmentMap) {
    return (String) appointmentMap.get("consultantId");
  }

  public Consultant consultantOf(Consultant consultant, Map<String, Object> patchMap) {
    if (patchMap.containsKey(EMAIL)) {
      consultant.setEmail((String) patchMap.get(EMAIL));
    }
    if (patchMap.containsKey(FIRST_NAME)) {
      consultant.setFirstName((String) patchMap.get(FIRST_NAME));
    }
    if (patchMap.containsKey(LAST_NAME)) {
      consultant.setLastName((String) patchMap.get(LAST_NAME));
    }
    if (patchMap.containsKey("encourage2fa")) {
      consultant.setEncourage2fa((Boolean) patchMap.get("encourage2fa"));
    }
    if (patchMap.containsKey("walkThroughEnabled")) {
      consultant.setWalkThroughEnabled((Boolean) patchMap.get("walkThroughEnabled"));
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
    if (patchMap.containsKey(EMAIL)) {
      adviceSeeker.setEmail((String) patchMap.get(EMAIL));
    }
    if (patchMap.containsKey("encourage2fa")) {
      adviceSeeker.setEncourage2fa((Boolean) patchMap.get("encourage2fa"));
    }

    return adviceSeeker;
  }

  public Appointment appointmentOf(Map<String, Object> appointmentMap, Consultant consultant) {
    var appointment = new Appointment();
    if (appointmentMap.containsKey("id")) {
      appointment.setId(UUID.fromString((String) appointmentMap.get("id")));
    }
    if (appointmentMap.containsKey("description")) {
      appointment.setDescription((String) appointmentMap.get("description"));
    }
    appointment.setDatetime(Instant.parse((String) appointmentMap.get("datetime")));
    var status = (String) appointmentMap.get(STATUS);
    appointment.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
    appointment.setConsultant(consultant);

    return appointment;
  }
}
