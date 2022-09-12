package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantAgency.ConsultantAgencyBase;
import de.caritas.cob.userservice.api.model.ConsultantAgencyStatus;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantAgencyRepository extends CrudRepository<ConsultantAgency, Long> {

  List<ConsultantAgency> findByAgencyIdAndDeleteDateIsNull(Long agencyId);

  List<ConsultantAgency> findByConsultantIdAndDeleteDateIsNull(String consultantId);

  List<ConsultantAgency> findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(
      Long agencyId);

  List<ConsultantAgency> findByConsultantIdAndAgencyIdAndDeleteDateIsNull(
      String consultantId, Long agencyId);

  boolean existsByConsultantIdAndAgencyIdAndDeleteDateIsNull(String consultantId, Long agencyId);

  List<ConsultantAgency> findByConsultantId(String consultantId);

  List<ConsultantAgency> findByConsultantIdAndStatusAndDeleteDateIsNull(
      String consultantId, ConsultantAgencyStatus status);

  ConsultantAgency findByConsultantIdAndAgencyIdAndStatusAndDeleteDateIsNull(
      String consultantId, Long agencyId, ConsultantAgencyStatus status);

  List<ConsultantAgency> findByAgencyIdInAndDeleteDateIsNull(Collection<Long> agencyIds);

  List<ConsultantAgency> findByConsultantIdIn(Set<String> consultantIds);

  @SuppressWarnings("all")
  List<ConsultantAgencyBase> findByConsultantIdIn(List<String> consultantIds);
}
