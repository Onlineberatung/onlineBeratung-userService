package de.caritas.cob.userservice.api.admin.report.registry;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.admin.report.model.AgencyDependedViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Registry to hold all implementation beans of {@link ViolationReportRule}. */
@Component
@RequiredArgsConstructor
public class ViolationRuleRegistry {

  private final @NonNull ApplicationContext applicationContext;
  private Collection<ViolationReportRule> violationReportRules;

  /**
   * Initializes the registry with all implemented {@link ViolationReportRule} beans. It´s necessary
   * to perform the registration after application startup is finished because all depending beans
   * must be initialized first.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initialize() {
    this.violationReportRules =
        this.applicationContext.getBeansOfType(ViolationReportRule.class).values();
  }

  /**
   * Provides all registered {@link ViolationReportRule} instances.
   *
   * @return all {@link ViolationReportRule} beans
   */
  public List<ViolationReportRule> getViolationReportRules(
      List<AgencyAdminResponseDTO> allAgencies) {
    List<ViolationReportRule> reportingRules =
        isEmpty(this.violationReportRules)
            ? emptyList()
            : new ArrayList<>(this.violationReportRules);
    reportingRules.forEach(
        reportRule -> {
          if (reportRule instanceof AgencyDependedViolationReportRule) {
            ((AgencyDependedViolationReportRule) reportRule).setAllAgencies(allAgencies);
          }
        });
    return reportingRules;
  }
}
