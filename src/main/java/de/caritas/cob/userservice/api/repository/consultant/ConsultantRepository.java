package de.caritas.cob.userservice.api.repository.consultant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantRepository extends CrudRepository<Consultant, Long> {

  Optional<Consultant> findByIdAndDeleteDateIsNull(String id);

  Optional<Consultant> findByRocketChatIdAndDeleteDateIsNull(String id);

  Optional<Consultant> findByEmailAndDeleteDateIsNull(String email);

  Optional<Consultant> findByUsernameAndDeleteDateIsNull(String username);

  List<Consultant> findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(List<Long> agencyIds);

  List<Consultant> findAllByDeleteDateNotNull();
}
