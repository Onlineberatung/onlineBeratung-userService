package de.caritas.cob.userservice.api.repository.session;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
   * Find a {@link Session} list by a consultant and a session status ordered by update date desc.
   *
   * @param consultant    {@link Consultant}
   * @param sessionStatus {@link SessionStatus}
   * @return A list of {@link Session}s for the specific consultant id and status ordered by update
   * date desc
   */
  List<Session> findByConsultantAndStatusOrderByUpdateDateDesc(Consultant consultant,
      SessionStatus sessionStatus);

  /**
   * Find a {@link Session} with unassigned consultant by agency ids and status ordery by creation
   * date ascending.
   *
   * @param agencyIds        ids of agencies to search for
   * @param sessionStatus    {@link SessionStatus} to search for
   * @param registrationType {@link RegistrationType} to search for
   * @return A list of {@link Session}s for the specific agency ids and status orderd by creation
   * date ascending
   */
  List<Session> findByAgencyIdInAndConsultantIsNullAndStatusAndRegistrationTypeOrderByEnquiryMessageDateAsc(
      List<Long> agencyIds, SessionStatus sessionStatus, RegistrationType registrationType);

  /**
   * Find a {@link Session} by agency ids with status and team session where consultant is not the
   * given consultant ordered by update date descending.
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

  /**
   * Find team {@link Session} list by agency ids and status where consultant is not the given
   * consultant ordered by creation date descending.
   *
   * @param agencyIds     ids of agencies to search for
   * @param sessionStatus {@link SessionStatus} to search for
   * @return A list of {@link Session}s for the specific agency ids and status ordered by update
   * date descending
   */
  List<Session> findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionIsTrueOrderByUpdateDateDesc(
      List<Long> agencyIds, Consultant consultant, SessionStatus sessionStatus);

  List<Session> findByUser(User user);

  List<Session> findByUserAndConsultingTypeId(User user, int consultingTypeId);

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
   * @param consultingTypeId the consulting ID to search for
   * @param pageable         the pagination object
   * @return the result {@link Page}
   */
  Page<Session> findByConsultingTypeId(int consultingTypeId, Pageable pageable);

  Page<Session> findAll(Pageable pageable);

  /**
   * Find the {@link Session}s by consulting type, registration type and pageable.
   *
   * @param consultingTypeIds the consulting type IDs to search for
   * @param registrationType  the {@link RegistrationType} to search for
   * @param pageable          the pagination object
   * @return the result {@link Page}
   */
  Page<Session> findByConsultingTypeIdInAndRegistrationTypeAndStatusOrderByCreateDateAsc(
      Set<Integer> consultingTypeIds, RegistrationType registrationType,
      SessionStatus sessionStatus, Pageable pageable);

  /**
   * Find all sessions by a given {@link SessionStatus}.
   */
  List<Session> findByStatus(SessionStatus status);

  /**
   * Find all sessions by a given {@link SessionStatus} and {@link RegistrationType}.
   */
  List<Session> findByStatusInAndRegistrationType(Set<SessionStatus> status,
      RegistrationType registrationType);

  /**
   * Count session by consultant, status and registration type.
   *
   * @param consultant        the {@link Consultant} to search for
   * @param sessionStatusList a {@link List} of {@link SessionStatus} to search for
   * @param registrationType  the {@link RegistrationType} to search for
   * @return the count
   */
  Long countByConsultantAndStatusInAndRegistrationType(Consultant consultant,
      List<SessionStatus> sessionStatusList, RegistrationType registrationType);

  /**
   * Find one session by assigned consultant and user.
   *
   * @param consultant       the consultant
   * @param user             the user
   * @param consultingTypeId the id of the consulting type
   * @return an {@link Optional} of the result
   */
  Optional<Session> findByConsultantAndUserAndConsultingTypeId(Consultant consultant, User user,
      Integer consultingTypeId);
}
