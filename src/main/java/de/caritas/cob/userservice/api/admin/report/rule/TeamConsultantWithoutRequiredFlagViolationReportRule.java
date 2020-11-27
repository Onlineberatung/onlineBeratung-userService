package de.caritas.cob.userservice.api.admin.report.rule;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

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
 * Violation rule to find team consultants with missing flag is_team_consultant.
 */
@Component
@RequiredArgsConstructor
public class TeamConsultantWithoutRequiredFlagViolationReportRule implements ViolationReportRule {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyAdminService agencyAdminService;

  /**
   * Generates all violations for {@link Consultant} containing a team agency and no
   * is_team_consultant_flag.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {
    return retrieveAllTeamAgencies().stream()
        .map(consultantAgencyRepository::findByAgencyId)
        .flatMap(Collection::stream)
        .filter(consultantAgency -> !consultantAgency.getConsultant().isTeamConsultant())
        .map(this::fromConsultantAgency)
        .collect(Collectors.toList());
  }

  private List<Long> retrieveAllTeamAgencies() {
    return this.agencyAdminService.retrieveAllAgencies().parallelStream()
        .filter(agencyAdminResponseDTO -> isTrue(agencyAdminResponseDTO.getTeamAgency()))
        .map(AgencyAdminResponseDTO::getAgencyId)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultantAgency(ConsultantAgency consultantAgency) {
    Consultant consultant = consultantAgency.getConsultant();
    Long agencyId = consultantAgency.getAgencyId();
    return ViolationByConsultantBuilder.getInstance(consultant)
        .withReason("Consultant is assigned to team agency " + agencyId + " but is not marked as "
            + "team consultant")
        .build();
  }

}
