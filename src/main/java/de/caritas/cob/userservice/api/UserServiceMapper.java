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
public class UserServiceMapper {

  private static final String EMAIL = "email";
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";
  private static final String USERNAME = "username";
  private static final String STATUS = "status";
  private static final String DESCRIPTION = "description";
  private static final String ENCOURAGE_2FA = "encourage2fa";

  private final UsernameTranscoder usernameTranscoder;

  public Map<String, Object> mapOf(Appointment appointment) {
    var map = new HashMap<String, Object>();
    map.put("id", appointment.getId().toString());
    map.put(DESCRIPTION, appointment.getDescription());
    map.put("datetime", appointment.getDatetime().toString());
    map.put(STATUS, appointment.getStatus().toString().toLowerCase());
    map.put("consultantId", appointment.getConsultant().getId());

    return map;
  }

  public Map<String, Object> mapOf(User user) {
    var map = new HashMap<String, Object>();
    map.put("id", user.getUserId());
    map.put(USERNAME, user.getUsername());
    map.put(EMAIL, user.getEmail());
    map.put(ENCOURAGE_2FA, user.getEncourage2fa());

    return map;
  }

  public Map<String, Object> mapOf(Consultant consultant, Map<String, Object> additionalMap) {
    var map = new HashMap<String, Object>();
    map.put("id", consultant.getId());
    map.put(FIRST_NAME, consultant.getFirstName());
    map.put(LAST_NAME, consultant.getLastName());
    map.put(EMAIL, consultant.getEmail());
    map.put(ENCOURAGE_2FA, consultant.getEncourage2fa());
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
        var agencyDTO = lookupMap.get(agencyId);
        Map<String, Object> agencyMap = new HashMap<>();
        agencyMap.put("id", agencyId);
        agencyMap.put("name", agencyDTO.getName());
        agencyMap.put("postcode", agencyDTO.getPostcode());
        agencies.add(agencyMap);
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
        EMAIL, consultantBase.getEmail(),
        FIRST_NAME, consultantBase.getFirstName(),
        LAST_NAME, consultantBase.getLastName(),
        STATUS, status,
        USERNAME, fullConsultant.getUsername(),
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
    if (patchMap.containsKey(EMAIL)) {
      consultant.setEmail((String) patchMap.get(EMAIL));
    }
    if (patchMap.containsKey(FIRST_NAME)) {
      consultant.setFirstName((String) patchMap.get(FIRST_NAME));
    }
    if (patchMap.containsKey(LAST_NAME)) {
      consultant.setLastName((String) patchMap.get(LAST_NAME));
    }
    if (patchMap.containsKey(ENCOURAGE_2FA)) {
      consultant.setEncourage2fa((Boolean) patchMap.get(ENCOURAGE_2FA));
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
    if (patchMap.containsKey(ENCOURAGE_2FA)) {
      adviceSeeker.setEncourage2fa((Boolean) patchMap.get(ENCOURAGE_2FA));
    }

    return adviceSeeker;
  }

  public Appointment appointmentOf(Map<String, Object> appointmentMap, Consultant consultant) {
    var appointment = new Appointment();
    if (appointmentMap.containsKey("id")) {
      appointment.setId(UUID.fromString((String) appointmentMap.get("id")));
    }
    if (appointmentMap.containsKey(DESCRIPTION)) {
      appointment.setDescription((String) appointmentMap.get(DESCRIPTION));
    }
    appointment.setDatetime(Instant.parse((String) appointmentMap.get("datetime")));
    var status = (String) appointmentMap.get(STATUS);
    appointment.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
    appointment.setConsultant(consultant);

    return appointment;
  }

  public List<Long> agencyIdsOf(List<Consultant> consultants) {
    return consultants.stream()
        .map(Consultant::getConsultantAgencies)
        .flatMap(Set::stream)
        .map(ConsultantAgency::getAgencyId)
        .distinct()
        .collect(Collectors.toList());
  }
}
