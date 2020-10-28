package de.caritas.cob.userservice.api.repository.user;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

  Optional<User> findByUserId(String userId);

  Optional<User> findByRcUserId(String rcUserId);

}
