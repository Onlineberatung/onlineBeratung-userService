package de.caritas.cob.userservice.api;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Appointment;
import de.caritas.cob.userservice.api.model.Appointment.AppointmentStatus;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Consultant.ConsultantBase;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S1192") // String literals should not be duplicated
public class UserServiceMapper {

  private final UsernameTranscoder usernameTranscoder;

  public Map<String, Object> mapOf(Appointment appointment) {
    var map = new HashMap<String, Object>();
    map.put("id", appointment.getId().toString());
    map.put("description", appointment.getDescription());
    map.put("datetime", appointment.getDatetime().toString());
    map.put("status", appointment.getStatus().toString().toLowerCase());
    map.put("consultantId", appointment.getConsultant().getId());

    return map;
  }

  public Map<String, Object> mapOf(User user) {
    var map = new HashMap<String, Object>();
    map.put("id", user.getUserId());
    map.put("username", user.getUsername());
    map.put("email", user.getEmail());
    map.put("encourage2fa", user.getEncourage2fa());

    return map;
  }

  public Map<String, Object> mapOf(Consultant consultant, Map<String, Object> additionalMap) {
    var map = new HashMap<String, Object>();
    map.put("id", consultant.getId());
    map.put("firstName", consultant.getFirstName());
    map.put("lastName", consultant.getLastName());
    map.put("email", consultant.getEmail());
    map.put("encourage2fa", consultant.getEncourage2fa());
    map.put("walkThroughEnabled", consultant.getWalkThroughEnabled());

    if (additionalMap.containsKey("displayName")) {
      var displayName = (String) additionalMap.get("displayName");
      map.put("displayName", usernameTranscoder.decodeUsername(displayName));
    }

    return map;
  }

  public Map<String, Object> mapOf(Page<ConsultantBase> consultantPage,
      List<Consultant> fullConsultants, List<AgencyDTO> agencyDTOS) {

    var agencyLookupMap = agencyDTOS.stream()
        .collect(Collectors.toMap(AgencyDTO::getId, Function.identity()));

    var fullConsultantLookupMap = fullConsultants.stream()
        .collect(Collectors.toMap(Consultant::getId, Function.identity()));

    var consultants = new ArrayList<Map<String, Object>>();
    consultantPage.forEach(consultantBase -> {
      var fullConsultant = fullConsultantLookupMap.get(consultantBase.getId());
      var agencies = mapOf(fullConsultant.getConsultantAgencies(), agencyLookupMap);
      var consultantMap = mapOf(consultantBase, fullConsultant, agencies);
      consultants.add(consultantMap);
    });

    return Map.of(
        "totalElements", (int) consultantPage.getTotalElements(),
        "isFirstPage", consultantPage.isFirst(),
        "isLastPage", consultantPage.isLast(),
        "consultants", consultants
    );
  }

  private List<Map<String, Object>> mapOf(
      Set<ConsultantAgency> consultantAgencies, Map<Long, AgencyDTO> lookupMap) {

    var agencies = new ArrayList<Map<String, Object>>();
    if (nonNull(consultantAgencies)) {
      consultantAgencies.forEach(consultantAgency -> {
        var agencyId = consultantAgency.getAgencyId();
        if (lookupMap.containsKey(agencyId)) {
          var agencyDTO = lookupMap.get(agencyId);
          Map<String, Object> agencyMap = new HashMap<>();
          agencyMap.put("id", agencyId);
          agencyMap.put("name", agencyDTO.getName());
          agencyMap.put("postcode", agencyDTO.getPostcode());
          agencies.add(agencyMap);
        }
      });
    }

    return agencies;
  }

  public Map<String, Object> mapOf(ConsultantBase consultantBase, Consultant fullConsultant,
      List<Map<String, Object>> agencies) {
    var status = isNull(fullConsultant.getStatus())
        ? ConsultantStatus.ERROR.toString()
        : fullConsultant.getStatus().toString();

    return Map.of(
        "id", consultantBase.getId(),
        "email", consultantBase.getEmail(),
        "firstName", consultantBase.getFirstName(),
        "lastName", consultantBase.getLastName(),
        "status", status,
        "username", fullConsultant.getUsername(),
        "agencies", agencies
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
    if (patchMap.containsKey("email")) {
      adviceSeeker.setEmail((String) patchMap.get("email"));
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
    var status = (String) appointmentMap.get("status");
    appointment.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
    appointment.setConsultant(consultant);

    return appointment;
  }

  public List<Long> agencyIdsOf(List<Consultant> consultants) {
    return consultants.stream()
        .map(Consultant::getConsultantAgencies)
        .flatMap(Set::stream)
        .filter(consultantAgency -> isNull(consultantAgency.getDeleteDate()))
        .map(ConsultantAgency::getAgencyId)
        .distinct()
        .collect(Collectors.toList());
  }
}
