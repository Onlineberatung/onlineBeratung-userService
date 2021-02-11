package de.caritas.cob.userservice.api.repository.consultantagency;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ConsultantAgencyRepository extends CrudRepository<ConsultantAgency, Long> {

  List<ConsultantAgency> findByAgencyId(Long agencyId);

  List<ConsultantAgency> findByConsultantId(String consultantId);

  List<ConsultantAgency> findByAgencyIdOrderByConsultantFirstNameAsc(Long agencyId);

  List<ConsultantAgency> findByConsultantIdAndAgencyId(String consultantId, Long agencyId);

}
