package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Consultant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantRepository extends CrudRepository<Consultant, String> {

  Optional<Consultant> findByIdAndDeleteDateIsNull(String id);

  Optional<Consultant> findByRocketChatIdAndDeleteDateIsNull(String id);

  Optional<Consultant> findByEmailAndDeleteDateIsNull(String email);

  Optional<Consultant> findByUsernameAndDeleteDateIsNull(String username);

  List<Consultant> findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(List<Long> agencyIds);

  List<Consultant> findByConsultantAgenciesAgencyIdAndDeleteDateIsNull(Long agencyId);

  List<Consultant> findAllByDeleteDateNotNull();

  @Query(
      value =
          "FROM Consultant c "
              + "WHERE c.deleteDate IS NULL"
              + "  AND ("
              + "   UPPER(c.firstName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "   OR UPPER(c.lastName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "   OR UPPER(c.email) LIKE CONCAT('%', UPPER(?1), '%')"
              + "  )"
  )
  List<Consultant> findAllByInfix(String infix, Pageable pageable);

  long countByDeleteDateIsNull();

}
