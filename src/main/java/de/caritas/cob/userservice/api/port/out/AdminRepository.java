package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<Admin, String> {

  @Query(
      value =
          "SELECT a.id as id, a.firstName as firstName, a.lastName as lastName, a.email as email "
              + "FROM Admin a "
              + "WHERE"
              + "  ?1 = '*' "
              + "  OR ("
              + "    UPPER(a.firstName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(a.lastName) LIKE CONCAT('%', UPPER(?1), '%')"
              + "    OR UPPER(a.email) LIKE CONCAT('%', UPPER(?1), '%')"
              + "  )")
  Page<AdminBase> findAllByInfix(String infix, Pageable pageable);

  List<Admin> findAllByIdIn(Set<String> adminIds);
}