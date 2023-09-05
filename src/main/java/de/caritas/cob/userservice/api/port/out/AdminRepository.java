package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<Admin, String> {

  @Query(
      value =
          "SELECT a.id as id, a.firstName as firstName, a.lastName as lastName, a.email as email, a.tenantId as tenantId "
              + "FROM Admin a "
              + "WHERE"
              + "  type = ?2 "
              + "AND ("
              + "  ?1 = '*' "
              + "  OR ("
              + "    UPPER(a.id) = UPPER(?1)"
              + "    OR UPPER(a.firstName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(a.lastName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(a.email) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR CONVERT(a.tenantId,char) LIKE CONCAT('%', UPPER(?1), '%')"
              + "  )"
              + " )")
  Page<AdminBase> findAllByInfix(String infix, Admin.AdminType type, Pageable pageable);

  @Query(value = "SELECT a FROM Admin a WHERE id = ?1 AND type = ?2")
  Optional<Admin> findByIdAndType(String adminId, Admin.AdminType type);

  @Query(value = "SELECT a FROM Admin a WHERE tenantId = ?1 AND type = ?2")
  List<Admin> findByTenantIdAndType(Long tenantId, Admin.AdminType type);

  List<Admin> findAllByIdIn(Set<String> adminIds);
}
