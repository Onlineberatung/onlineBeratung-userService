package de.caritas.cob.userservice.api.admin.report.rule;

import static de.caritas.cob.userservice.api.model.ViolationDTO.ViolationTypeEnum.CONSULTANT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.admin.report.service.AgencyAdminService;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TeamConsultantWithoutRequiredFlagViolationReportRuleTest {

  @InjectMocks
  private TeamConsultantWithoutRequiredFlagViolationReportRule reportRule;

  @Mock
  private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock
  private AgencyAdminService agencyAdminService;

  @Test
  public void generateViolations_Should_returnEmptyList_When_noViolationExists() {
    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(0));
  }

  @Test
  public void generateViolations_Should_returnExpectedViolation_When_oneViolatedConsultantExists() {
    ConsultantAgency violatedConsultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    violatedConsultantAgency.setAgencyId(1L);
    violatedConsultantAgency.getConsultant().setTeamConsultant(false);
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(singletonList(violatedConsultantAgency));
    when(this.agencyAdminService.retrieveAllAgencies())
        .thenReturn(singletonList(new AgencyAdminResponseDTO().agencyId(1L).teamAgency(true)));

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    ViolationDTO resultViolation = violations.iterator().next();
    assertThat(resultViolation.getIdentifier(),
        is(violatedConsultantAgency.getConsultant().getId()));
    assertThat(resultViolation.getViolationType(), is(CONSULTANT));
    assertThat(resultViolation.getReason(),
        is("Consultant is assigned to team agency 1 but is not marked as team consultant"));
    assertThat(resultViolation.getAdditionalInformation(), hasSize(2));
    assertThat(resultViolation.getAdditionalInformation().get(0).getName(), is("Username"));
    assertThat(resultViolation.getAdditionalInformation().get(0).getValue(),
        is(violatedConsultantAgency.getConsultant().getUsername()));
    assertThat(resultViolation.getAdditionalInformation().get(1).getName(), is("Email"));
    assertThat(resultViolation.getAdditionalInformation().get(1).getValue(),
        is(violatedConsultantAgency.getConsultant().getEmail()));
  }

  @Test
  public void generateViolations_Should_returnViolationsOnlyForViolatedConsultants() {
    List<ConsultantAgency> consultantAgencies = new EasyRandom().objects(ConsultantAgency.class, 10)
        .collect(Collectors.toList());
    consultantAgencies.get(0).setAgencyId(1L);
    consultantAgencies.get(0).getConsultant().setTeamConsultant(false);
    consultantAgencies.get(2).setAgencyId(2L);
    consultantAgencies.get(2).getConsultant().setTeamConsultant(false);
    consultantAgencies.get(4).setAgencyId(3L);
    consultantAgencies.get(4).getConsultant().setTeamConsultant(false);
    consultantAgencies.get(6).setAgencyId(4L);
    consultantAgencies.get(6).getConsultant().setTeamConsultant(false);
    consultantAgencies.get(9).setAgencyId(5L);
    consultantAgencies.get(9).getConsultant().setTeamConsultant(false);
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(eq(1L)))
        .thenReturn(singletonList(consultantAgencies.get(0)));
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(eq(2L)))
        .thenReturn(singletonList(consultantAgencies.get(2)));
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(eq(3L)))
        .thenReturn(singletonList(consultantAgencies.get(4)));
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(eq(4L)))
        .thenReturn(singletonList(consultantAgencies.get(6)));
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(eq(5L)))
        .thenReturn(singletonList(consultantAgencies.get(9)));
    when(this.agencyAdminService.retrieveAllAgencies()).thenReturn(asList(
        new AgencyAdminResponseDTO().agencyId(1L).teamAgency(true),
        new AgencyAdminResponseDTO().agencyId(2L).teamAgency(true),
        new AgencyAdminResponseDTO().agencyId(3L).teamAgency(true),
        new AgencyAdminResponseDTO().agencyId(4L).teamAgency(true),
        new AgencyAdminResponseDTO().agencyId(5L).teamAgency(true)
    ));

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(5));
  }

}
