package de.caritas.cob.userservice.api.admin.report.model;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import java.util.List;
import lombok.Setter;

@Setter
public abstract class AgencyDependedViolationReportRule implements ViolationReportRule {

  private List<AgencyAdminResponseDTO> allAgencies;

  public List<AgencyAdminResponseDTO> getAllAgencies() {
    return isNotEmpty(this.allAgencies) ? this.allAgencies : emptyList();
  }
}
