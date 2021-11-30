package de.caritas.cob.userservice.api.repository.consultantmobiletoken;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantMobileTokenRepository extends
    CrudRepository<ConsultantMobileToken, Long> {

  Optional<ConsultantMobileToken> findByMobileAppToken(String mobileAppToken);

}
