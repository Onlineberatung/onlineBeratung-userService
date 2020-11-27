package de.caritas.cob.userservice.api.admin.report.rule;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.admin.report.builder.ViolationByConsultantBuilder;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.service.AgencyAdminService;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Violation rule to find consultants with wrong flag is_team_consultant.
 */
@Component
@RequiredArgsConstructor
public class ConsultantWithWrongTeamConsultantFlagViolationReportRule implements
    ViolationReportRule {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyAdminService agencyAdminService;

  /**
   * Generates all violations for {@link Consultant} containing flag is_team_consultant with
   * assigned agency which is not a team agency.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {
    return retrieveAllNonTeamAgencies().stream()
        .map(consultantAgencyRepository::findByAgencyId)
        .flatMap(Collection::stream)
        .filter(consultantAgency -> consultantAgency.getConsultant().isTeamConsultant())
        .map(this::fromConsultantAgency)
        .collect(Collectors.toList());
  }

  private List<Long> retrieveAllNonTeamAgencies() {
    return this.agencyAdminService.retrieveAllAgencies().parallelStream()
        .filter(agencyAdminResponseDTO -> isFalse(agencyAdminResponseDTO.getTeamAgency()))
        .map(AgencyAdminResponseDTO::getAgencyId)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultantAgency(ConsultantAgency consultantAgency) {
    Consultant consultant = consultantAgency.getConsultant();
    Long agencyId = consultantAgency.getAgencyId();
    return ViolationByConsultantBuilder.getInstance(consultant)
        .withReason("Consultant has flag is_team_consultant but assigned agency " + agencyId
            + " is not a team agency")
        .build();
  }

}
