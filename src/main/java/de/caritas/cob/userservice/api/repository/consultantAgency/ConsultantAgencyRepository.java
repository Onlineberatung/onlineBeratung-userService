package de.caritas.cob.userservice.api.repository.consultantAgency;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;

public interface ConsultantAgencyRepository extends CrudRepository<ConsultantAgency, Long> {

  List<ConsultantAgency> findByConsultant(Consultant consultant);

  List<ConsultantAgency> findByAgencyId(Long agencyId);

  List<ConsultantAgency> findByAgencyIdOrderByConsultantFirstNameAsc(Long agencyId);

  List<ConsultantAgency> findByConsultantIdAndAgencyId(String consultantId, Long agencyId);

}
