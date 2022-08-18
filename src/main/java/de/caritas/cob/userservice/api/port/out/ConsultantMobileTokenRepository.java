package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.ConsultantMobileToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantMobileTokenRepository
    extends CrudRepository<ConsultantMobileToken, Long> {

  Optional<ConsultantMobileToken> findByMobileAppToken(String mobileAppToken);
}
