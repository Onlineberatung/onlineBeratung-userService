package de.caritas.cob.userservice.api.admin.service;

import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to handle administrative operations on consultant-agencies.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAgencyAdminService {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;

  public ConsultantAgencyAdminResultDTO findConsultantAgencies(String consultantId) {
    List<ConsultantAgency> agencyList = consultantAgencyRepository
        .findByConsultantId(consultantId);

    return ConsultantAgencyAdminResultDTOBuilder.getInstance().withConsultantId(consultantId)
        .withResult(agencyList).build();
  }

}
