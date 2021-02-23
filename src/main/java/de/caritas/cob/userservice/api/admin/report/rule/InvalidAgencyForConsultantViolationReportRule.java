package de.caritas.cob.userservice.api.admin.report.rule;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.admin.report.builder.ViolationByConsultantBuilder;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.service.AgencyAdminService;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Violation rule to find consultants with deleted agency relation.
 */
@Component
@RequiredArgsConstructor
public class InvalidAgencyForConsultantViolationReportRule implements ViolationReportRule {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyAdminService agencyAdminService;

  /**
   * Generates all violations for {@link Consultant} containing a reference to a deleted agency.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {
    return retrieveAllDeletedAgencies().stream()
        .map(consultantAgencyRepository::findByAgencyIdAndDeleteDateIsNull)
        .flatMap(Collection::stream)
        .map(this::fromConsultantAgency)
        .collect(Collectors.toList());
  }

  private List<Long> retrieveAllDeletedAgencies() {
    return this.agencyAdminService.retrieveAllAgencies().stream()
        .filter(agencyAdminResponseDTO -> !"null".equals(agencyAdminResponseDTO.getDeleteDate()))
        .map(AgencyAdminResponseDTO::getAgencyId)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultantAgency(ConsultantAgency consultantAgency) {
    Consultant consultant = consultantAgency.getConsultant();
    Long agencyId = consultantAgency.getAgencyId();
    return ViolationByConsultantBuilder.getInstance(consultant)
        .withReason("Assigned agency with id " + agencyId + " has been deleted")
        .build();
  }

}
