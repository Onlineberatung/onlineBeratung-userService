package de.caritas.cob.userservice.api.repository.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, String> {

  Optional<User> findByUserIdAndDeleteDateIsNull(String userId);

  Optional<User> findByRcUserIdAndDeleteDateIsNull(String rcUserId);

  List<User> findAllByDeleteDateNotNull();

  Optional<User> findByUsernameInAndDeleteDateIsNull(Collection<String> usernames);

  /**
   * Find all users whose create date is older than given date and having no new registered session
   * after the given create date and no running sessions.
   *
   * @param createDate the create date to check
   * @return a list of {@link User}
   */
  @Query(
      value = "SELECT u.user_id, u.id_old, u.username, u.email, u.rc_user_id, "
          + "u.language_formal, u.mobile_token, u.delete_date "
          + "FROM user u "
          + "WHERE u.delete_date IS NULL "
          + "AND create_date < :create_date "
          + "AND NOT EXISTS ( "
          + "SELECT 1 "
          + "FROM user_agency ua "
          + "WHERE u.user_id = ua.user_id) "
          + "AND NOT EXISTS ( "
          + "SELECT 1 "
          + "FROM session s1 "
          + "WHERE u.user_id = s1.user_id "
          + "AND s1.status > 0 "
          + "AND s1.message_date IS NOT NULL "
          + "AND s1.rc_group_id IS NOT NULL) "
          + "AND EXISTS ( "
          + "SELECT 1 "
          + "FROM session s2 "
          + "WHERE u.user_id = s2.user_id "
          + "AND s2.status = 0 "
          + "AND s2.message_date IS NULL "
          + "AND s2.rc_group_id IS NULL "
          + "AND s2.create_date < :create_date) "
          + "AND NOT EXISTS ( "
          + "SELECT 1 "
          + "FROM session s3 "
          + "WHERE u.user_id = s3.user_id "
          + "AND s3.status = 0 "
          + "AND s3.message_date IS NULL "
          + "AND s3.rc_group_id IS NULL "
          + "AND s3.create_date >= :create_date) ",
      nativeQuery = true)
  List<User> findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
      @Param(value = "create_date") LocalDateTime createDate);
}
