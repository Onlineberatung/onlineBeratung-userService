package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Consultant.ConsultantBase;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
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

  List<Consultant> findAllByIdIn(List<String> ids);

  @Query(
      value =
          "SELECT c.id as id, c.firstName as firstName, c.lastName as lastName, c.email as email "
              + "FROM Consultant c "
              + "WHERE"
              + "  ?1 = '*' "
              + "  OR ("
              + "    UPPER(c.id) = UPPER(?1)"
              + "    OR UPPER(c.firstName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(c.lastName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(c.email) LIKE CONCAT('%', UPPER(?1), '%')"
              + "  )")
  Page<ConsultantBase> findAllByInfix(String infix, Pageable pageable);

  @Query(
      value =
          "SELECT distinct c.id as id, c.firstName as firstName, c.lastName as lastName, c.email as email "
              + "FROM Consultant c "
              + "INNER JOIN ConsultantAgency ca ON c.id = ca.consultant.id "
              + "WHERE"
              + " ca.agencyId IN (?2) "
              + " AND ("
              + "  ?1 = '*' "
              + "  OR ("
              + "    UPPER(c.id) = UPPER(?1)"
              + "    OR UPPER(c.firstName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(c.lastName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(c.email) LIKE CONCAT('%', UPPER(?1), '%')"
              + "  )"
              + ")")
  Page<ConsultantBase> findAllByInfixAndAgencyIds(
      String infix, Collection<Long> agencyIds, Pageable pageable);

  long countByDeleteDateIsNull();

  long countByTenantIdAndDeleteDateIsNull(Long tenantId);

  @Query(
      value =
          "SELECT DISTINCT c.rocketChatId "
              + "FROM Consultant c "
              + "INNER JOIN ConsultantAgency ca ON c.id = ca.consultant.id "
              + "WHERE ca.agencyId IN (?1) "
              + "AND ca.deleteDate IS NULL")
  Set<String> findAllByAgencyIds(Set<Long> agencyIds);
}
