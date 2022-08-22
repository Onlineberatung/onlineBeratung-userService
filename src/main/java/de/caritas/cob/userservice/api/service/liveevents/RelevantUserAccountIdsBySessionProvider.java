package de.caritas.cob.userservice.api.service.liveevents;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Provider to observe assigned session user ids instead of initiator. */
@Component
@RequiredArgsConstructor
public class RelevantUserAccountIdsBySessionProvider implements UserIdsProvider {

  private final @NonNull SessionRepository sessionRepository;

  /**
   * Collects the relevant user id of a session, if consultant wrote, id of user will be returned
   * and vice versa.
   *
   * @param rcGroupId the rocket chat group id used to retrieve the {@link Session}
   * @return a {@link List} containing the user id to be notified
   */
  @Override
  public List<String> collectUserIds(String rcGroupId) {
    Session session =
        this.sessionRepository
            .findByGroupId(rcGroupId)
            .orElse(this.sessionRepository.findByFeedbackGroupId(rcGroupId).orElse(null));

    return extractDependentUserIds(session);
  }

  private List<String> extractDependentUserIds(Session session) {
    if (isNull(session) || isNull(session.getConsultant())) {
      return emptyList();
    }
    return Stream.of(session.getUser().getUserId(), session.getConsultant().getId())
        .collect(Collectors.toList());
  }
}
