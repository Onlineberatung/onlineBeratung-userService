package de.caritas.cob.userservice.api.admin.report.rule;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.admin.report.builder.ViolationByConsultantBuilder;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Violation rule to find consultants without agency relation. */
@Component
@RequiredArgsConstructor
public class MissingAgencyForConsultantViolationReportRule implements ViolationReportRule {

  private final @NonNull ConsultantRepository consultantRepository;

  /**
   * Generates all violations for {@link Consultant} without agency assignments.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {
    return StreamSupport.stream(this.consultantRepository.findAll().spliterator(), false)
        .filter(consultant -> isEmpty(consultant.getConsultantAgencies()))
        .map(this::fromConsultant)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultant(Consultant consultant) {
    return ViolationByConsultantBuilder.getInstance(consultant)
        .withReason("Missing agency assignment for consultant")
        .build();
  }
}
