package de.caritas.cob.userservice.api.service.liveevents;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provider to observe assigned session user ids instead of initiator.
 */
@Component
public class RelevantUserAccountIdsBySessionProvider extends UserIdsProvider {

  private final SessionRepository sessionRepository;

  @Autowired
  public RelevantUserAccountIdsBySessionProvider(AuthenticatedUser authenticatedUser,
      SessionRepository sessionRepository) {
    super(authenticatedUser);
    this.sessionRepository = requireNonNull(sessionRepository);
  }

  /**
   * Collects the relevant user id of a session, if consultant wrote, id of user will be returned
   * and vice versa.
   *
   * @param rcGroupId the rocket chat group id used to retrieve the {@link Session}
   * @return a {@link List} containing the user id to be notified
   */
  @Override
  List<String> collectUserIds(String rcGroupId) {
    Session session = this.sessionRepository.findByGroupId(rcGroupId)
            .orElse(this.sessionRepository.findByFeedbackGroupId(rcGroupId)
                .orElse(null));

    return extractDependentUserIds(session);
  }

  private List<String> extractDependentUserIds(Session session) {
    if (isNull(session)) {
      return emptyList();
    }
    return Stream.of(session.getUser().getUserId(), session.getConsultant().getId())
        .filter(this::notInitiatingUser)
        .collect(Collectors.toList());
  }

}
