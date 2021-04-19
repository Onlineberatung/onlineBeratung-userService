package de.caritas.cob.userservice.api.repository.session;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<Session, Long> {

  /**
   * Find a {@link Session} by a consultant id and a session status.
   *
   * @param consultant    {@link Consultant}
   * @param sessionStatus {@link SessionStatus}
   * @return A list of {@link Session}s for the specific consultant id and status
   */
  List<Session> findByConsultantAndStatus(Consultant consultant, SessionStatus sessionStatus);

  /**
   * Find a {@link Session} with unassigned consultant by agency ids and status ordery by creation
   * date ascending.
   *
   * @param agencyIds     ids of agencies to search for
   * @param sessionStatus {@link SessionStatus} to search for
   * @return A list of {@link Session}s for the specific agency ids and status orderd by creation
   * date ascending
   */
  List<Session> findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
      List<Long> agencyIds, SessionStatus sessionStatus);

  /**
   * Find a {@link Session} by agency ids with status and teamberatung where consultant is not the
   * given consultant ordery by creation date ascending.
   *
   * @param agencyIds     ids of agencies to search for
   * @param sessionStatus {@link SessionStatus} to search for
   * @param isTeamSession boolean to filter or not team sessions
   * @return A list of {@link Session}s for the specific agency ids and status orderd by creation
   * date ascending
   */
  List<Session> findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
      List<Long> agencyIds, Consultant consultant, SessionStatus sessionStatus,
      boolean isTeamSession);

  List<Session> findByUser(User user);

  List<Session> findByUserAndConsultingId(User user, int consultingId);

  /**
   * Find all {@link Session}s by a user ID.
   *
   * @param userId Keycloak/MariaDB user ID
   * @return A list of {@link Session}s for the specified user ID
   */
  List<Session> findByUserUserId(String userId);

  /**
   * Find the {@link Session}s by user id and pageable.
   *
   * @param userId   the id to search for
   * @param pageable the pagination object
   * @return the result {@link Page}
   */
  Page<Session> findByUserUserId(String userId, Pageable pageable);

  /**
   * Find the {@link Session}s by Rocket.Chat group id and asker id.
   *
   * @param groupId the rc group id to search for
   * @param userId  the user id to search for
   * @return the result sessions
   */
  List<Session> findByGroupIdAndUserUserId(String groupId, String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat group id and consultant id.
   *
   * @param groupId      the rc group id to search for
   * @param consultantId the consultant id to search for
   * @return the result sessions
   */
  List<Session> findByGroupIdAndConsultantId(String groupId, String consultantId);

  /**
   * Find the {@link Session}s by Rocket.Chat group id and asker id.
   *
   * @param feedbackGroupId the feedback group id to search for
   * @param userId          the user id to search for
   * @return the result sessions
   */
  List<Session> findByFeedbackGroupIdAndUserUserId(String feedbackGroupId, String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat feedback group id and consultant id.
   *
   * @param feedbackGroupId the feedback group id to search for
   * @param consultantId    the consultant id to search for
   * @return the result sessions
   */
  List<Session> findByFeedbackGroupIdAndConsultantId(String feedbackGroupId, String consultantId);

  /**
   * Find the {@link Session} by Rocket.Chat feedback group id.
   *
   * @param feedbackGroupId the rocket chat feedback group id
   * @return an {@link Optional} of the session
   */
  Optional<Session> findByFeedbackGroupId(String feedbackGroupId);

  /**
   * Find the {@link Session} by Rocket.Chat group id.
   *
   * @param groupId the rocket chat group id
   * @return an {@link Optional} of the session
   */
  Optional<Session> findByGroupId(String groupId);

  /**
   * Find all {@link Session}s by a agency ID and SessionStatus where consultant is null.
   *
   * @param agencyId      the id to search for
   * @param sessionStatus {@link SessionStatus}
   * @return A list of {@link Session}s for the specified agency ID
   */
  List<Session> findByAgencyIdAndStatusAndConsultantIsNull(Long agencyId,
      SessionStatus sessionStatus);

  /**
   * Find all {@link Session}s by a agency ID and SessionStatus.
   *
   * @param agencyId      the id to search for
   * @param sessionStatus {@link SessionStatus}
   * @return A list of {@link Session}s for the specified agency ID
   */
  List<Session> findByAgencyIdAndStatusAndTeamSessionIsTrue(Long agencyId,
      SessionStatus sessionStatus);

  /**
   * Find the {@link Session}s by agency id and pageable.
   *
   * @param agencyId the id to search for
   * @param pageable the pagination object
   * @return the result {@link Page}
   */
  Page<Session> findByAgencyId(Long agencyId, Pageable pageable);

  /**
   * Find the {@link Session}s by consultant id and pageable.
   *
   * @param consultantId the id to search for
   * @param pageable     the pagination object
   * @return the result {@link Page}
   */
  Page<Session> findByConsultantId(String consultantId, Pageable pageable);

  /**
   * Find the {@link Session}s by consulting type and pageable.
   *
   * @param consultingId the consulting ID to search for
   * @param pageable     the pagination object
   * @return the result {@link Page}
   */
  Page<Session> findByConsultingId(int consultingId, Pageable pageable);

  Page<Session> findAll(Pageable pageable);

}
