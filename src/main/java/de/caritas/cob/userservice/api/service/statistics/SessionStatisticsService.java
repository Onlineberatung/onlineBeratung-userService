package de.caritas.cob.userservice.api.service.statistics;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.statistics.model.SessionStatisticsResultDTO;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionStatisticsService {

  private final @NonNull SessionRepository sessionRepository;

  /**
   * Retrieve session data via session id () or Rocket.Chat group id. If session id and Rocket.Chat
   * group id is given the session will be retrieved via session id.
   *
   * @param sessionId the session id
   * @param rcGroupId the Rocket.Chat group id
   * @return an {@link SessionStatisticsResultDTO} instance.
   */
  public SessionStatisticsResultDTO retrieveSession(Long sessionId, String rcGroupId) {

    checkRequestParameter(sessionId, rcGroupId);
    Optional<Session> session = retrieveSessionViaSessionIdOrRcGroupId(sessionId, rcGroupId);
    return buildSessionStatisticsResultDTO(
        session.orElseThrow(
            () ->
                new NotFoundException(
                    "Session with id %s or Rocket.Chat group id %s not found",
                    sessionId, rcGroupId)));
  }

  private void checkRequestParameter(Long sessionId, String rcGroupId) {
    if (isNull(sessionId) && isNull(rcGroupId)) {
      throw new BadRequestException("sessionId or rcGroupId required");
    }
  }

  private Optional<Session> retrieveSessionViaSessionIdOrRcGroupId(
      Long sessionId, String rcGroupId) {
    if (nonNull(sessionId)) {
      return sessionRepository.findById(sessionId);
    } else {
      return sessionRepository.findByGroupId(rcGroupId);
    }
  }

  private SessionStatisticsResultDTO buildSessionStatisticsResultDTO(Session session) {
    return new SessionStatisticsResultDTO()
        .id(session.getId())
        .rcGroupId(session.getGroupId())
        .agencyId(session.getAgencyId())
        .consultingType(session.getConsultingTypeId())
        .isTeamSession(session.isTeamSession())
        .createDate(String.valueOf(session.getCreateDate()))
        .messageDate(String.valueOf(session.getEnquiryMessageDate()))
        .postcode(session.getPostcode());
  }
}
