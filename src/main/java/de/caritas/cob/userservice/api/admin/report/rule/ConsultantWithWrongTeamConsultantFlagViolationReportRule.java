package de.caritas.cob.userservice.api.admin.report.rule;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.admin.report.builder.ViolationByConsultantBuilder;
import de.caritas.cob.userservice.api.admin.report.model.AgencyDependedViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.rule.analyzer.ConsultantAgencyAnalyzer;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Violation rule to find consultants with wrong flag is_team_consultant. */
@Component
@RequiredArgsConstructor
public class ConsultantWithWrongTeamConsultantFlagViolationReportRule
    extends AgencyDependedViolationReportRule {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull ConsultantRepository consultantRepository;

  /**
   * Generates all violations for {@link Consultant} containing flag is_team_consultant with
   * assigned agencies which are all not team agencies.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {

    List<ConsultantAgency> allConsultantAgencies =
        StreamSupport.stream(consultantAgencyRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());

    List<AgencyAdminResponseDTO> allAgencies =
        super.getAllAgencies().parallelStream().collect(Collectors.toList());

    ConsultantAgencyAnalyzer consultantAgencyAnalyzer =
        new ConsultantAgencyAnalyzer(allConsultantAgencies, allAgencies);

    return StreamSupport.stream(consultantRepository.findAll().spliterator(), false)
        .filter(Consultant::isTeamConsultant)
        .filter(consultantAgencyAnalyzer::hasNoTeamAgencyAssigned)
        .map(this::fromConsultant)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultant(Consultant consultant) {
    return ViolationByConsultantBuilder.getInstance(consultant)
        .withReason("Consultant has flag is_team_consultant but has no team agency assigned")
        .build();
  }
}
