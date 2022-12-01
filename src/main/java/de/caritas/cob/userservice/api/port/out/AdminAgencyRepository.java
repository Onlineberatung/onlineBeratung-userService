package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.model.AdminAgency.AdminAgencyBase;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AdminAgencyRepository extends CrudRepository<AdminAgency, Long> {

  List<AdminAgency> findByAdminId(String adminId);

  List<AdminAgency> findByAdminIdAndAgencyId(String adminId, Long agencyId);

  @Transactional
  void deleteByAdminIdAndAgencyId(String adminId, Long agencyId);

  @Transactional
  void deleteByAdminId(String adminId);

  @SuppressWarnings("all")
  List<AdminAgencyBase> findByAdminIdIn(Set<String> adminIds);
}
