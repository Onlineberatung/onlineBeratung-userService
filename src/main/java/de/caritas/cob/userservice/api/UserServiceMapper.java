package de.caritas.cob.userservice.api;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.api.client.util.ArrayMap;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Appointment;
import de.caritas.cob.userservice.api.model.Appointment.AppointmentStatus;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Consultant.ConsultantBase;
import de.caritas.cob.userservice.api.model.ConsultantAgency.ConsultantAgencyBase;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    map.put("chatUserId", user.getRcUserId());

    return map;
  }

  public Map<String, Object> mapOf(Consultant consultant, Map<String, Object> additionalMap) {
    var map = new HashMap<String, Object>();
    map.put("id", consultant.getId());
    map.put("firstName", consultant.getFirstName());
    map.put("lastName", consultant.getLastName());
    map.put("email", consultant.getEmail());
    map.put("encourage2fa", consultant.getEncourage2fa());
    map.put("notifyEnquiriesRepeating", consultant.getNotifyEnquiriesRepeating());
    map.put("walkThroughEnabled", consultant.getWalkThroughEnabled());
    map.put("chatUserId", consultant.getRocketChatId());

    if (additionalMap.containsKey("displayName")) {
      var displayName = (String) additionalMap.get("displayName");
      map.put("displayName", usernameTranscoder.decodeUsername(displayName));
    }

    return map;
  }

  public Map<String, Object> mapOf(Page<ConsultantBase> consultantPage,
      List<Consultant> fullConsultants, List<AgencyDTO> agencyDTOS,
      List<ConsultantAgencyBase> consultantAgencies) {

    var agencyLookupMap = agencyDTOS.stream()
        .collect(Collectors.toMap(AgencyDTO::getId, Function.identity()));

    var fullConsultantLookupMap = fullConsultants.stream()
        .collect(Collectors.toMap(Consultant::getId, Function.identity()));

    var consultantAgencyLookupMap = consultantAgencies.stream()
        .collect(Collectors.groupingBy(ConsultantAgencyBase::getConsultantId));

    var consultants = new ArrayList<Map<String, Object>>();
    consultantPage.forEach(consultantBase -> {
      var fullConsultant = fullConsultantLookupMap.get(consultantBase.getId());
      var agencies = mapOf(fullConsultant, agencyLookupMap, consultantAgencyLookupMap);
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

  private List<Map<String, Object>> mapOf(Consultant consultant,
      Map<Long, AgencyDTO> agencyLookupMap, Map<String, List<ConsultantAgencyBase>> caLookupMap) {
    var agencies = new ArrayList<Map<String, Object>>();
    var agencyIdsAdded = new HashSet<Long>();

    if (caLookupMap.containsKey(consultant.getId())) {
      caLookupMap.get(consultant.getId()).forEach(coAgency -> {
        var agencyId = coAgency.getAgencyId();
        if (agencyLookupMap.containsKey(agencyId)
            && isDeletionConsistent(consultant, coAgency)
            && isAgencyUnique(agencyIdsAdded, agencyId)) {
          var agencyDTO = agencyLookupMap.get(agencyId);
          agencies.add(mapOf(agencyDTO));
          agencyIdsAdded.add(agencyId);
        }
      });
    }

    return agencies;
  }

  private Map<String, Object> mapOf(AgencyDTO agencyDTO) {
    Map<String, Object> agencyMap = new HashMap<>();
    agencyMap.put("id", agencyDTO.getId());
    agencyMap.put("name", agencyDTO.getName());
    agencyMap.put("postcode", agencyDTO.getPostcode());
    agencyMap.put("city", agencyDTO.getCity());
    agencyMap.put("description", agencyDTO.getDescription());
    agencyMap.put("isTeamAgency", agencyDTO.getTeamAgency());
    agencyMap.put("isOffline", agencyDTO.getOffline());
    agencyMap.put("consultingType", agencyDTO.getConsultingType());

    return agencyMap;
  }

  public Map<String, Object> mapOf(ConsultantBase consultantBase, Consultant fullConsultant,
      List<Map<String, Object>> agencies) {
    var status = isNull(fullConsultant.getStatus())
        ? ConsultantStatus.ERROR.toString()
        : fullConsultant.getStatus().toString();

    Map<String, Object> map = new HashMap<>();
    map.put("id", consultantBase.getId());
    map.put("email", consultantBase.getEmail());
    map.put("firstName", consultantBase.getFirstName());
    map.put("lastName", consultantBase.getLastName());
    map.put("status", status);
    map.put("username", fullConsultant.getUsername());
    map.put("absenceMessage", fullConsultant.getAbsenceMessage());
    map.put("isAbsent", fullConsultant.isAbsent());
    map.put("isLanguageFormal", fullConsultant.isLanguageFormal());
    map.put("isTeamConsultant", fullConsultant.isTeamConsultant());
    map.put("createdAt",
        nonNull(fullConsultant.getCreateDate()) ? fullConsultant.getCreateDate().toString() : null);
    map.put("updatedAt",
        nonNull(fullConsultant.getUpdateDate()) ? fullConsultant.getUpdateDate().toString() : null);
    map.put("deletedAt",
        nonNull(fullConsultant.getDeleteDate()) ? fullConsultant.getDeleteDate().toString() : null);
    map.put("agencies", agencies);

    return map;
  }

  public Optional<Map<String, String>> mapOf(Optional<Session> optionalSession) {
    if (optionalSession.isEmpty()) {
      return Optional.empty();
    }

    var session = optionalSession.get();
    var map = new ArrayMap<String, String>();
    if (nonNull(session.getGroupId())) {
      map.put("chatId", session.getGroupId());
    }

    return Optional.of(map);
  }

  private boolean isAgencyUnique(HashSet<Long> agencyIdsAdded, Long agencyId) {
    return !agencyIdsAdded.contains(agencyId);
  }

  private boolean isDeletionConsistent(Consultant consultant,
      ConsultantAgencyBase consultantAgency) {
    return !(isNull(consultant.getDeleteDate()) && nonNull(consultantAgency.getDeleteDate()));
  }

  @SuppressWarnings("unchecked")
  public List<String> bannedUsernamesOfMap(Map<String, Object> chatMetaInfoMap) {
    return (List<String>) chatMetaInfoMap.get("mutedUsers");
  }

  public Optional<String> e2eKeyOf(Map<String, String> chatMap) {
    return chatMap.containsKey("e2eKey") && chatMap.get("e2eKey").matches("tmp\\..{12,}")
        ? Optional.of(chatMap.get("e2eKey"))
        : Optional.empty();
  }

  public String roomIdOf(Map<String, String> chatMap) {
    return chatMap.get("roomId");
  }

  public String userIdOf(Map<String, String> chatMap) {
    return chatMap.get("userId");
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
    if (patchMap.containsKey("notifyEnquiriesRepeating")) {
      consultant.setNotifyEnquiriesRepeating((Boolean) patchMap.get("notifyEnquiriesRepeating"));
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

  public List<Long> agencyIdsOf(List<ConsultantAgencyBase> consultantAgencies) {
    return consultantAgencies.stream()
        .map(ConsultantAgencyBase::getAgencyId)
        .distinct()
        .collect(Collectors.toList());
  }

  public List<String> chatUserIdOf(List<Map<String, String>> groupMembers) {
    return groupMembers.stream()
        .map(map -> map.get("chatUserId"))
        .collect(Collectors.toList());
  }
}