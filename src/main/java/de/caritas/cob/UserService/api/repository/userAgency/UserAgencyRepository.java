package de.caritas.cob.UserService.api.repository.userAgency;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.UserService.api.repository.user.User;

public interface UserAgencyRepository extends CrudRepository<UserAgency, Long> {

  List<UserAgency> findByUser(User user);

}
