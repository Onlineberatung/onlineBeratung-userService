package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toUnixTime;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.GroupSessionConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.GroupSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionTopicDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.helper.SessionDataKeyRegistration;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Mapper class to map a {@link Session} to possible dto objects.
 */
@RequiredArgsConstructor
public class SessionMapper {

  /**
   * Maps the given {@link Session} to a {@link ConsultantSessionResponseDTO}.
   *
   * @return the mapped {@link ConsultantSessionResponseDTO}
   */
  public ConsultantSessionResponseDTO toConsultantSessionDto(Session session) {
    return new ConsultantSessionResponseDTO()
        .session(convertToSessionDTO(session))
        .user(convertToSessionUserDTO(session))
        .consultant(convertToSessionConsultantForConsultantDTO(session.getConsultant()))
        .latestMessage(extractEnquiryMessageDate(session));
  }

  private Date extractEnquiryMessageDate(Session session) {
    LocalDateTime enquiryMessageDate = session.getEnquiryMessageDate();
    return nonNull(enquiryMessageDate) ? Date.valueOf(enquiryMessageDate.toLocalDate()) : null;
  }

  /**
   * Maps the given {@link Session} to a {@link SessionDTO}.
   *
   * @param session the session to be mapped
   * @return the mapped {@link SessionDTO}
   */
  public SessionDTO convertToSessionDTO(Session session) {
    return new SessionDTO()
        .id(session.getId())
        .agencyId(session.getAgencyId())
        .consultingType(session.getConsultingTypeId())
        .status(session.getStatus().getValue())
        .postcode(session.getPostcode())
        .groupId(session.getGroupId())
        .feedbackGroupId(
            nonNull(session.getFeedbackGroupId()) ? session.getFeedbackGroupId() : null)
        .askerRcId(nonNull(session.getUser()) && nonNull(session.getUser().getRcUserId())
            ? session.getUser().getRcUserId() : null)
        .messageDate(toUnixTime(session.getEnquiryMessageDate()))
        .isTeamSession(session.isTeamSession())
        .language(LanguageCode.fromValue(session.getLanguageCode().name()))
        .isPeerChat(session.isPeerChat())
        .monitoring(session.isMonitoring())
        .registrationType(session.getRegistrationType().name())
        .createDate(toIsoTime(session.getCreateDate()))
        .topic(new SessionTopicDTO().id(session.getMainTopicId()));

  }

  private SessionUserDTO convertToSessionUserDTO(Session session) {
    if (nonNull(session.getUser()) && nonNull(session.getSessionData())) {
      var sessionUserDto = new SessionUserDTO();
      sessionUserDto
          .setUsername(new UsernameTranscoder().decodeUsername(session.getUser().getUsername()));
      sessionUserDto.setSessionData(buildSessionDataMapFromSession(session));
      return sessionUserDto;
    }
    return null;
  }

  private SessionConsultantForConsultantDTO convertToSessionConsultantForConsultantDTO(
      Consultant consultant) {
    return nonNull(consultant) ? new SessionConsultantForConsultantDTO()
        .id(consultant.getId())
        .firstName(consultant.getFirstName())
        .lastName(consultant.getLastName()) : null;
  }

  public Map<String, Object> buildSessionDataMapFromSession(Session session) {
    Map<String, Object> sessionDataMap = new LinkedHashMap<>();
    session.getSessionData().stream()
        .filter(sessionData -> SessionDataKeyRegistration.containsKey(sessionData.getKey()))
        .forEach(sessionData -> sessionDataMap.put(sessionData.getKey(), sessionData.getValue()));

    return sessionDataMap;
  }

  public GroupSessionResponseDTO toGroupSessionResponse(
      UserSessionResponseDTO userSessionResponse) {
    var response = new GroupSessionResponseDTO()
        .session(userSessionResponse.getSession())
        .agency(userSessionResponse.getAgency())
        .chat(userSessionResponse.getChat())
        .latestMessage(userSessionResponse.getLatestMessage());

    var sessionConsultant = userSessionResponse.getConsultant();
    if (sessionConsultant == null) {
      return response;
    }
    var consultant = GroupSessionConsultantDTO.builder()
        .username(sessionConsultant.getUsername())
        .displayName(sessionConsultant.getDisplayName())
        .isAbsent(sessionConsultant.isAbsent())
        .absenceMessage(sessionConsultant.getAbsenceMessage());
    return response.consultant(consultant.build());
  }

  public GroupSessionResponseDTO toGroupSessionResponse(
      ConsultantSessionResponseDTO consultantSessionResponse) {
    var response = new GroupSessionResponseDTO()
        .session(consultantSessionResponse.getSession())
        .user(consultantSessionResponse.getUser())
        .chat(consultantSessionResponse.getChat())
        .latestMessage(consultantSessionResponse.getLatestMessage());

    var sessionConsultant = consultantSessionResponse.getConsultant();
    if (sessionConsultant == null) {
      return response;
    }
    var consultant = GroupSessionConsultantDTO.builder()
        .id(sessionConsultant.getId())
        .firstName(sessionConsultant.getFirstName())
        .lastName(sessionConsultant.getLastName());
    return response.consultant(consultant.build());
  }
}
