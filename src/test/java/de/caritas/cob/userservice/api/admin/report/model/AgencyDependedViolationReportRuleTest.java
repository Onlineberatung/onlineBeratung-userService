package de.caritas.cob.userservice.api.admin.report.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgencyDependedViolationReportRuleTest {

  private AgencyDependedViolationReportRule reportRule =
      new AgencyDependedViolationReportRule() {
        @Override
        public List<ViolationDTO> generateViolations() {
          return emptyList();
        }
      };

  @Test
  void getAllAgencies_Should_returnEmptyList_When_noAgenciesHaveBeenSet() {
    List<AgencyAdminResponseDTO> allAgencies = reportRule.getAllAgencies();

    assertThat(allAgencies, is(emptyList()));
  }

  @Test
  void getAllAgencies_Should_returnExpectedList_When_agenciesHaveBeenSet() {
    List<AgencyAdminResponseDTO> agencies =
        asList(new AgencyAdminResponseDTO(), new AgencyAdminResponseDTO());

    this.reportRule.setAllAgencies(agencies);
    List<AgencyAdminResponseDTO> allAgencies = reportRule.getAllAgencies();

    assertThat(allAgencies, hasSize(2));
    assertThat(allAgencies, is(agencies));
  }
}
