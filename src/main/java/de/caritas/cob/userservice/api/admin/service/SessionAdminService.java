package de.caritas.cob.userservice.api.admin.service;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.admin.pageprovider.PageProviderFactory;
import de.caritas.cob.userservice.api.admin.pageprovider.SessionPageProvider;
import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.model.SessionAdminDTO;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service class to handle administrative operations on sessions.
 */
@Service
@RequiredArgsConstructor
public class SessionAdminService {

  private final @NonNull SessionRepository sessionRepository;

  /**
   * Finds existing sessions filtered by {@link Filter} and retrieves all sessions if no filter
   * is set.
   *
   * @param page the current page
   * @param perPage number of items per page
   * @param filter criteria to filter on sessions
   * @return a generated {@link SessionAdminResultDTO} containing the results
   */
  public SessionAdminResultDTO findSessions(Integer page, Integer perPage, Filter filter) {
    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(perPage, 1));

    SessionPageProvider sessionPageProvider =
        PageProviderFactory.getInstance(this.sessionRepository, filter)
            .retrieveFirstSupportedSessionPageProvider();

    return buildSessionAdminResult(sessionPageProvider.executeQuery(pageable));
  }

  private SessionAdminResultDTO buildSessionAdminResult(Page<Session> resultPage) {
    List<SessionAdminDTO> resultSessions = resultPage.get()
        .map(this::fromSession)
        .collect(Collectors.toList());

    return new SessionAdminResultDTO()
        .embedded(resultSessions);
  }

  private SessionAdminDTO fromSession(Session session) {
    return new SessionAdminDTO()
        .agencyId(session.getAgencyId().intValue())
        .consultantId(nonNull(session.getConsultant()) ? session.getConsultant().getId() : null)
        .consultingType(session.getConsultingType().getValue())
        .email(session.getUser().getEmail())
        .postcode(session.getPostcode())
        .userId(session.getUser().getUserId())
        .username(session.getUser().getUsername())
        .isTeamSession(session.isTeamSession())
        .messageDate(String.valueOf(session.getEnquiryMessageDate()))
        .createDate(String.valueOf(session.getCreateDate()))
        .updateDate(String.valueOf(session.getUpdateDate()));
  }

}
