package de.caritas.cob.userservice.api.admin.report.rule;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

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

/** Violation rule to find team consultants with missing flag is_team_consultant. */
@Component
@RequiredArgsConstructor
public class TeamConsultantWithoutRequiredFlagViolationReportRule
    extends AgencyDependedViolationReportRule {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;

  /**
   * Generates all violations for {@link Consultant} containing a team agency and no
   * is_team_consultant flag.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {
    return retrieveAllTeamAgencies().stream()
        .map(consultantAgencyRepository::findByAgencyIdAndDeleteDateIsNull)
        .flatMap(Collection::stream)
        .filter(consultantAgency -> !consultantAgency.getConsultant().isTeamConsultant())
        .map(this::fromConsultantAgency)
        .collect(Collectors.toList());
  }

  private List<Long> retrieveAllTeamAgencies() {
    return super.getAllAgencies().stream()
        .filter(agencyAdminResponseDTO -> isTrue(agencyAdminResponseDTO.getTeamAgency()))
        .map(AgencyAdminResponseDTO::getId)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultantAgency(ConsultantAgency consultantAgency) {
    Consultant consultant = consultantAgency.getConsultant();
    Long agencyId = consultantAgency.getAgencyId();
    return ViolationByConsultantBuilder.getInstance(consultant)
        .withReason(
            "Consultant is assigned to team agency "
                + agencyId
                + " but is not marked as "
                + "team consultant")
        .build();
  }
}
