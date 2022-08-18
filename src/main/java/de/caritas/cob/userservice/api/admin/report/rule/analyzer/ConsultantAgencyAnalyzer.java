package de.caritas.cob.userservice.api.admin.report.rule.analyzer;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Analyzer class to figure out if a given {@link Consultant} has no team agency assigned. */
@RequiredArgsConstructor
public class ConsultantAgencyAnalyzer {

  private final @NonNull List<ConsultantAgency> allConsultantAgencies;
  private final @NonNull List<AgencyAdminResponseDTO> allAgencies;

  /**
   * Analyzes the agency relations of given {@link Consultant} to check if no of the assigned
   * agencies has flag is_team_agency.
   *
   * @param consultant the {@link Consultant} to perform the analysis on
   * @return true if no related agency is a team agency
   */
  public boolean hasNoTeamAgencyAssigned(Consultant consultant) {
    return allConsultantAgencies.stream()
        .filter(assignedToConsultant(consultant))
        .map(this::fromConsultantAgency)
        .filter(Objects::nonNull)
        .noneMatch(agencyAdminResponseDTO -> isTrue(agencyAdminResponseDTO.getTeamAgency()));
  }

  private Predicate<ConsultantAgency> assignedToConsultant(Consultant consultant) {
    return consultantAgency -> consultant.getId().equals(consultantAgency.getConsultant().getId());
  }

  private AgencyAdminResponseDTO fromConsultantAgency(ConsultantAgency consultantAgency) {
    return this.allAgencies.stream()
        .filter(byConsultantAgency(consultantAgency))
        .findFirst()
        .orElse(null);
  }

  private Predicate<AgencyAdminResponseDTO> byConsultantAgency(ConsultantAgency consultantAgency) {
    return agencyAdminResponseDTO ->
        consultantAgency.getAgencyId().equals(agencyAdminResponseDTO.getId());
  }
}
