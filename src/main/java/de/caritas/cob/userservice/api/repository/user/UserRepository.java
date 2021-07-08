package de.caritas.cob.userservice.api.repository.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {

  Optional<User> findByUserIdAndDeleteDateIsNull(String userId);

  Optional<User> findByRcUserIdAndDeleteDateIsNull(String rcUserId);

  List<User> findAllByDeleteDateNotNull();

  Optional<User> findByUsernameInAndDeleteDateIsNull(Collection<String> usernames);

  /**
   * Find all users which create date is older than give date and having no running sessions.
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
              + "SELECT * "
              + "FROM session s "
              + "WHERE u.user_id = s.user_id "
              + "AND s.status > 0 "
              + "AND s.message_date IS NOT NULL "
              + "AND s.rc_group_id IS NOT NULL)",
      nativeQuery = true)
  List<User> findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
      @Param(value = "create_date") LocalDateTime createDate);
}
