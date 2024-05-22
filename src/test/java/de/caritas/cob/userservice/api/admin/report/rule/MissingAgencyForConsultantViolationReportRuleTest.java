package de.caritas.cob.userservice.api.admin.report.rule;

import static de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO.ViolationTypeEnum.CONSULTANT;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MissingAgencyForConsultantViolationReportRuleTest {

  @InjectMocks private MissingAgencyForConsultantViolationReportRule reportRule;

  @Mock private ConsultantRepository consultantRepository;

  @Test
  public void generateViolations_Should_returnEmptyList_When_noViolationExists() {
    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(0));
  }

  @Test
  public void generateViolations_Should_returnExpectedViolation_When_oneViolatedConsultantExists() {
    Consultant violatedConsultant = new EasyRandom().nextObject(Consultant.class);
    violatedConsultant.setConsultantAgencies(null);
    when(this.consultantRepository.findAll()).thenReturn(singletonList(violatedConsultant));

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    ViolationDTO resultViolation = violations.iterator().next();
    assertThat(resultViolation.getIdentifier(), is(violatedConsultant.getId()));
    assertThat(resultViolation.getViolationType(), is(CONSULTANT));
    assertThat(resultViolation.getReason(), is("Missing agency assignment for consultant"));
    assertThat(resultViolation.getAdditionalInformation(), hasSize(2));
    assertThat(resultViolation.getAdditionalInformation().get(0).getName(), is("Username"));
    assertThat(
        resultViolation.getAdditionalInformation().get(0).getValue(),
        is(violatedConsultant.getUsername()));
    assertThat(resultViolation.getAdditionalInformation().get(1).getName(), is("Email"));
    assertThat(
        resultViolation.getAdditionalInformation().get(1).getValue(),
        is(violatedConsultant.getEmail()));
  }

  @Test
  public void generateViolations_Should_returnViolationsOnlyForConsultantsWithoutAgency() {
    List<Consultant> consultants =
        new EasyRandom().objects(Consultant.class, 10).collect(Collectors.toList());
    consultants.get(0).setConsultantAgencies(null);
    consultants.get(2).setConsultantAgencies(null);
    consultants.get(4).setConsultantAgencies(null);
    consultants.get(6).setConsultantAgencies(null);
    consultants.get(9).setConsultantAgencies(null);
    when(this.consultantRepository.findAll()).thenReturn(consultants);

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(5));
  }
}
