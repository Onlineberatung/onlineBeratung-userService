package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

  Optional<User> findByUserIdAndDeleteDateIsNull(String userId);

  Optional<User> findByEmailAndDeleteDateIsNull(String email);

  Optional<User> findByRcUserIdAndDeleteDateIsNull(String rcUserId);

  List<User> findAllByDeleteDateNotNull();

  Optional<User> findByUsernameInAndDeleteDateIsNull(Collection<String> usernames);

  /**
   * Find all users whose create date is older than given date and having no new registered session
   * after the given create date and no running sessions.
   *
   * @param date the date to check
   * @return a list of {@link User}
   */
  @Query(
      value =
          "SELECT u FROM User u "
              + "WHERE u.deleteDate IS NULL "
              + "  AND u.createDate < ?1 "
              + "  AND NOT EXISTS ( SELECT 1 FROM UserAgency ua WHERE u = ua.user)"
              + "  AND NOT EXISTS ( "
              + "    SELECT 1 FROM Session s1 "
              + "    WHERE u = s1.user "
              + "      AND s1.status > 0 "
              + "      AND s1.enquiryMessageDate IS NOT NULL "
              + "      AND s1.groupId IS NOT NULL "
              + "  )"
              + "  AND EXISTS ("
              + "    SELECT 1 FROM Session s2 "
              + "      WHERE u = s2.user "
              + "        AND s2.status = 0 "
              + "        AND s2.enquiryMessageDate IS NULL "
              + "        AND s2.groupId IS NULL "
              + "        AND s2.createDate < ?1 "
              + "  )"
              + "  AND NOT EXISTS ("
              + "    SELECT 1 FROM Session s3 "
              + "    WHERE u = s3.user "
              + "    AND s3.status = 0 "
              + "    AND s3.enquiryMessageDate IS NULL "
              + "    AND s3.groupId IS NULL "
              + "    AND s3.createDate >= ?1"
              + ")")
  List<User> findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(LocalDateTime date);
}
