package de.caritas.cob.userservice.api.admin.report.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.admin.report.registry.ViolationRuleRegistry;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ViolationReportGeneratorTest {

  @InjectMocks private ViolationReportGenerator violationReportGenerator;

  @Mock private ViolationRuleRegistry violationRuleRegistry;

  @Mock private AgencyAdminService agencyAdminService;

  @After
  public void cleanGeneratedFiles() throws IOException {
    FileUtils.deleteDirectory(new File("report"));
  }

  @Test
  public void generateReport_Should_returnEmptyList_When_noViolationExist() {
    List<ViolationDTO> violations = this.violationReportGenerator.generateReport();

    assertThat(violations, hasSize(0));
  }

  @Test
  public void generateReport_Should_returnFlattenedViolations_When_violationsAreFound() {
    when(this.violationRuleRegistry.getViolationReportRules(any()))
        .thenReturn(
            asList(
                () -> asList(identifiedViolation("first"), identifiedViolation("second")),
                () -> asList(identifiedViolation("third"), identifiedViolation("fourth")),
                () -> asList(identifiedViolation("fifth"), identifiedViolation("sixth"))));

    List<ViolationDTO> violations = this.violationReportGenerator.generateReport();

    assertThat(violations, hasSize(6));
    violations.forEach(violationDTO -> assertThat(violationDTO.getIdentifier(), notNullValue()));
  }

  private ViolationDTO identifiedViolation(String identifier) {
    return new ViolationDTO().identifier(identifier);
  }
}
