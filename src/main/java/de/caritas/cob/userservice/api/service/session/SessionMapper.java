package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.toIsoTime;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.toUnixTime;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.user.SessionUserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataKeyRegistration;
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
        .monitoring(session.isMonitoring())
        .registrationType(session.getRegistrationType().name())
        .createDate(toIsoTime(session.getCreateDate()));
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

}
