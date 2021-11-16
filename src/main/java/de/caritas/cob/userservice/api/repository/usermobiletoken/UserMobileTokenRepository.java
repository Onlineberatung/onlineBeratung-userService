package de.caritas.cob.userservice.api.repository.usermobiletoken;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserMobileTokenRepository extends CrudRepository<UserMobileToken, Long> {

  Optional<UserMobileToken> findByMobileAppToken(String mobileAppToken);

}
