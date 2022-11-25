package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.AdminAgency;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface AdminAgencyRepository extends CrudRepository<AdminAgency, Long> {
  List<AdminAgency> findByAdminId(String adminId);
}
