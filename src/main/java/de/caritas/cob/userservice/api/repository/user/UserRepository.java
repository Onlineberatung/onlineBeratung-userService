package de.caritas.cob.userservice.api.repository.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

  Optional<User> findByUserIdAndDeleteDateIsNull(String userId);

  Optional<User> findByRcUserIdAndDeleteDateIsNull(String rcUserId);

  List<User> findAllByDeleteDateNotNull();

  Optional<User> findByUsernameInAndDeleteDateIsNull(Collection<String> usernames);
}
