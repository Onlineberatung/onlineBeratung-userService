package de.caritas.cob.userservice.api.admin.report.service;

import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.registry.ViolationRuleRegistry;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Generator for all {@link ViolationReportRule} beans.
 */
@Service
@RequiredArgsConstructor
public class ViolationReportGenerator {

  private final @NonNull ViolationRuleRegistry violationRuleRegistry;

  /**
   * Generates a list of all located known violations.
   *
   * @return all found {@link ViolationDTO} objects
   */
  public List<ViolationDTO> generateReport() {
    return this.violationRuleRegistry.getViolationReportRules().stream()
        .map(ViolationReportRule::generateViolations)
        .flatMap(Collection::parallelStream)
        .collect(Collectors.toList());
  }

}
