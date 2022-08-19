package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.UserMobileToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserMobileTokenRepository extends CrudRepository<UserMobileToken, Long> {

  Optional<UserMobileToken> findByMobileAppToken(String mobileAppToken);
}
