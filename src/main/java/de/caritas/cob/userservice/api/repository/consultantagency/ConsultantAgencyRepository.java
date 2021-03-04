package de.caritas.cob.userservice.api.repository.consultantagency;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantAgencyRepository extends CrudRepository<ConsultantAgency, Long> {

  List<ConsultantAgency> findByAgencyIdAndDeleteDateIsNull(Long agencyId);

  List<ConsultantAgency> findByConsultantIdAndDeleteDateIsNull(String consultantId);

  List<ConsultantAgency> findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(Long agencyId);

  List<ConsultantAgency> findByConsultantIdAndAgencyIdAndDeleteDateIsNull(String consultantId, Long agencyId);

  List<ConsultantAgency> findByConsultantId(String consultantId);

}
