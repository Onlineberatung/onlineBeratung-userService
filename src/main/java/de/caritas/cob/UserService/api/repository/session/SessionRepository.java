package de.caritas.cob.UserService.api.repository.session;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.user.User;

public interface SessionRepository extends CrudRepository<Session, Long> {

  /**
   * Find a {@link Session} by a consultant id and a session status
   * 
   * @param consultant {@link Consultant}
   * @param sessionStatus {@link SessionStatus}
   * @return A list of {@link Session}s for the specific consultant id and status
   */
  List<Session> findByConsultantAndStatus(Consultant consultant, SessionStatus sessionStatus);

  /**
   * Find a {@link Session} with unassigned consultant by agency ids and status ordery by creation
   * date ascending
   * 
   * @param agencyIds
   * @param sessionStatus
   * @return A list of {@link Session}s for the specific agency ids and status orderd by creation
   *         date ascending
   */
  List<Session> findByAgencyIdInAndConsultantIsNullAndStatusOrderByEnquiryMessageDateAsc(
      List<Long> agencyIds, SessionStatus sessionStatus);

  /**
   * Find a {@link Session} by agency ids with status and teamberatung where consultant is not the
   * given consultant ordery by creation date ascending
   *
   * @param agencyIds
   * @param sessionStatus
   * @param isTeamSession
   * @return A list of {@link Session}s for the specific agency ids and status orderd by creation
   *         date ascending
   */
  List<Session> findByAgencyIdInAndConsultantNotAndStatusAndTeamSessionOrderByEnquiryMessageDateAsc(
      List<Long> agencyIds, Consultant consultant, SessionStatus sessionStatus,
      boolean isTeamSession);

  List<Session> findByUser(User user);

  List<Session> findByUserAndConsultingType(User user, ConsultingType consultingType);

  /**
   * Find all {@link Session}s by a user ID
   * 
   * @param userId Keycloak/MariaDB user ID
   * @return A list of {@link Session}s for the specified user ID
   */
  List<Session> findByUser_UserId(String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat group id and asker id
   * 
   * @param groupId
   * @param userId
   * @return
   */
  List<Session> findByGroupIdAndUserUserId(String groupId, String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat group id and consultant id
   * 
   * @param groupId
   * @param userId
   * @return
   */
  List<Session> findByGroupIdAndConsultantId(String groupId, String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat group id and asker id
   * 
   * @param feedbackGroupId
   * @param userId
   * @return
   */
  List<Session> findByFeedbackGroupIdAndUserUserId(String feedbackGroupId, String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat feedback group id and consultant id
   * 
   * @param feedbackGroupId
   * @param userId
   * @return
   */
  List<Session> findByFeedbackGroupIdAndConsultantId(String feedbackGroupId, String userId);

  /**
   * Find the {@link Session}s by Rocket.Chat feedback group id
   * 
   * @param feedbackGroupId
   * @return
   */
  List<Session> findByFeedbackGroupId(String feedbackGroupId);

}
