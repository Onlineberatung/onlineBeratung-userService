package de.caritas.cob.userservice.api.admin.report.model;

import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import java.util.List;

/** Representation of an rule used to generate violation reports. */
public interface ViolationReportRule {

  /**
   * Generates violations on implemented condition.
   *
   * @return a list of found {@link ViolationDTO}
   */
  List<ViolationDTO> generateViolations();
}
