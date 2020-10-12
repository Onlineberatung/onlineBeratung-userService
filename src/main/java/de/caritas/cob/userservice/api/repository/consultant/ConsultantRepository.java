package de.caritas.cob.userservice.api.repository.consultant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantRepository extends CrudRepository<Consultant, Long> {

  Optional<Consultant> findById(String id);

  Optional<Consultant> findByRocketChatId(String id);

  Optional<Consultant> findByEmail(String email);

  Optional<Consultant> findByUsername(String username);

  List<Consultant> findByConsultantAgenciesAgencyIdIn(List<Long> agencyIds);
}
