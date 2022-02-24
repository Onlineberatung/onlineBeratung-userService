package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.UserAgency;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.userservice.api.model.User;

public interface UserAgencyRepository extends CrudRepository<UserAgency, Long> {

  List<UserAgency> findByUser(User user);

}
