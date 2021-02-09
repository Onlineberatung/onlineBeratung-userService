package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.ConsultantAgencyRelationCreatorService;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantAgencyAdminServiceIT {

  @Autowired
  private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @MockBean
  private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @MockBean
  private AgencyServiceHelper agencyServiceHelper;

  @MockBean
  private RemoveConsultantFromRocketChatService removeConsultantFromRocketChatService;

  @Test
  public void findConsultantAgencies_Should_returnAllConsultantAgenciesForGivenConsultantId_with_correctConsultantId() {

    ConsultantAgencyAdminResultDTO consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe");

    assertThat(consultantAgencies, notNullValue());
    assertThat(consultantAgencies.getEmbedded(), hasSize(24));
  }

  @Test
  public void findConsultantAgencies_Should_returnFullMappedSessionAdminDTO() {

    ConsultantAgencyAdminResultDTO consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe");

    ConsultantAgencyAdminDTO consultantAgencyAdminDTO = consultantAgencies.getEmbedded().iterator()
        .next();
    assertThat(consultantAgencyAdminDTO.getConsultantId(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getAgencyId(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getCreateDate(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getUpdateDate(), notNullValue());
  }

  @Test
  public void findConsultantAgencies_Should_returnEmptyResult_with_incorrectConsultantId() {
    try {
      ConsultantAgencyAdminResultDTO consultantAgencies = consultantAgencyAdminService
          .findConsultantAgencies("12345678-1234-1234-1234-1234567890ab");
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(BadRequestException.class));
      assertThat(e.getMessage(),
          is("Consultant with id 12345678-1234-1234-1234-1234567890ab does not exist"));
    }
  }

  @Test
  public void markAllAssignedConsultantsAsTeamConsultant_Should_markAssignedConsultantsAsTeamConsultant() {
    long teamCosnultantsBefore = this.consultantRepository
        .findByConsultantAgenciesAgencyIdIn(singletonList(1L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(1L);

    long teamConsultantsAfter = this.consultantRepository
        .findByConsultantAgenciesAgencyIdIn(singletonList(1L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    assertThat(teamConsultantsAfter, is(not(teamCosnultantsBefore)));
    assertThat(teamConsultantsAfter, is(greaterThan(teamCosnultantsBefore)));
  }

  @Test
  public void removeConsultantsFromTeamSessionsByAgencyId_Should_removeTeamConsultantFlagAndCallServices()
      throws AgencyServiceHelperException {
    when(this.agencyServiceHelper.getAgency(any())).thenReturn(new AgencyDTO().teamAgency(false));

    long teamCosnultantsBefore = this.consultantRepository
        .findByConsultantAgenciesAgencyIdIn(singletonList(0L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(0L);

    long teamConsultantsAfter = this.consultantRepository
        .findByConsultantAgenciesAgencyIdIn(singletonList(0L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    assertThat(teamConsultantsAfter, is(not(teamCosnultantsBefore)));
    assertThat(teamConsultantsAfter, is(lessThan(teamCosnultantsBefore)));
    verify(this.removeConsultantFromRocketChatService, times(1))
        .removeConsultantFromSessions(any());
  }

}
