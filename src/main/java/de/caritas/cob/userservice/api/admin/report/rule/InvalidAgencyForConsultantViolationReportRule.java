package de.caritas.cob.userservice.api.admin.report.rule;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.admin.report.builder.ViolationByConsultantBuilder;
import de.caritas.cob.userservice.api.admin.report.model.AgencyDependedViolationReportRule;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Violation rule to find consultants with deleted agency relation. */
@Component
@RequiredArgsConstructor
public class InvalidAgencyForConsultantViolationReportRule
    extends AgencyDependedViolationReportRule {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;

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
    return super.getAllAgencies().stream()
        .filter(agencyAdminResponseDTO -> !"null".equals(agencyAdminResponseDTO.getDeleteDate()))
        .map(AgencyAdminResponseDTO::getId)
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
