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
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.ConsultantAgencyRelationCreatorService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = "multitenancy.enabled=true")
@Transactional
@Sql(value = "/database/setTenantsSpecificData.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ConsultantAgencyAdminServiceTenantAwareIT {

  @Autowired
  private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private ConsultantAgencyRepository consultantAgencyRepository;

  @MockBean
  private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @MockBean
  private AgencyService agencyService;

  @MockBean
  private AgencyAdminService agencyAdminService;

  @MockBean
  private RemoveConsultantFromRocketChatService removeConsultantFromRocketChatService;

  @Before
  public void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @After
  public void afterTests() {
    TenantContext.clear();
  }

  @Test
  public void findConsultantAgencies_Should_returnAllConsultantAgenciesForGivenConsultantId_with_correctConsultantId() {
    var agencyAdminResponseDTO = new EasyRandom().nextObject(AgencyAdminResponseDTO.class);
    agencyAdminResponseDTO.setId(0L);
    var anotherAgencyAdminResponseDTO = new EasyRandom().nextObject(AgencyAdminResponseDTO.class);
    anotherAgencyAdminResponseDTO.setId(1L);
    when(this.agencyAdminService.retrieveAllAgencies())
        .thenReturn(List.of(agencyAdminResponseDTO, anotherAgencyAdminResponseDTO));

    var consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("0b3b1cc6-be98-4787-aa56-212259d811b8");

    assertThat(consultantAgencies, notNullValue());
    assertThat(consultantAgencies.getEmbedded(), hasSize(1));
    assertThat(consultantAgencies.getTotal(), is(1));
    assertThat(consultantAgencies.getLinks().getSelf().getHref(),
        is("http://localhost/useradmin/consultants/0b3b1cc6-be98-4787-aa56-212259d811b8/agencies"));
  }

  @Test
  public void findConsultantAgencies_Should_returnFullMappedSessionAdminDTO() {
    var agencyAdminResponseDTO = new EasyRandom()
        .nextObject(AgencyAdminResponseDTO.class);
    agencyAdminResponseDTO.setId(1L);
    when(this.agencyAdminService.retrieveAllAgencies())
        .thenReturn(singletonList(agencyAdminResponseDTO));

    var consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("0b3b1cc6-be98-4787-aa56-212259d811b7");

    var consultantAgencyAdminDTO = consultantAgencies.getEmbedded().iterator()
        .next();
    assertThat(consultantAgencyAdminDTO.getEmbedded().getCity(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getEmbedded().getId(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getEmbedded().getCreateDate(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getEmbedded().getUpdateDate(), notNullValue());
  }

  @Test
  public void findConsultantAgencies_Should_returnEmptyResult_with_incorrectConsultantId() {
    try {
      consultantAgencyAdminService.findConsultantAgencies("12345678-1234-1234-1234-1234567890ab");
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
        .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(1L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(1L);

    long teamConsultantsAfter = this.consultantRepository
        .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(1L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    assertThat(teamConsultantsAfter, is(not(teamCosnultantsBefore)));
    assertThat(teamConsultantsAfter, is(greaterThan(teamCosnultantsBefore)));
  }

  @Test
  public void removeConsultantsFromTeamSessionsByAgencyId_Should_removeTeamConsultantFlagAndCallServices() {
    when(this.agencyService.getAgency(any())).thenReturn(new AgencyDTO().teamAgency(false));

    long teamCosnultantsBefore = this.consultantRepository
        .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(1L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(1L);

    long teamConsultantsAfter = this.consultantRepository
        .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(1L))
        .stream()
        .filter(Consultant::isTeamConsultant)
        .count();

    assertThat(teamConsultantsAfter, is(not(teamCosnultantsBefore)));
    assertThat(teamConsultantsAfter, is(lessThan(teamCosnultantsBefore)));
    verify(this.removeConsultantFromRocketChatService, times(1))
        .removeConsultantFromSessions(any());
  }

  @Test
  public void markConsultantAgencyForDeletion_Should_setDeletedFlagIndatabase_When_consultantAgencyCanBeDeleted() {
    ConsultantAgency validRelation = this.consultantAgencyRepository.findAll().iterator().next();
    String consultantId = validRelation.getConsultant().getId();
    Long agencyId = validRelation.getAgencyId();

    this.consultantAgencyAdminService.markConsultantAgencyForDeletion(consultantId, agencyId);

    ConsultantAgency deletedConsultantAgency =
        this.consultantAgencyRepository.findById(validRelation.getId()).get();
    assertThat(deletedConsultantAgency.getDeleteDate(), notNullValue());
  }

  @Test
  public void findConsultantsForAgency_Should_returnExpectedConsultants_When_agencyHasConsultatns() {
    var consultantsOfAgency = this.consultantAgencyAdminService.findConsultantsForAgency(1L);

    assertThat(consultantsOfAgency.getEmbedded(), hasSize(2));
    consultantsOfAgency.getEmbedded().forEach(consultant -> {
      assertThat(consultant.getEmbedded(), notNullValue());
      assertThat(consultant.getLinks(), notNullValue());
      assertThat(consultant.getLinks().getAddAgency(), notNullValue());
      assertThat(consultant.getLinks().getAgencies(), notNullValue());
      assertThat(consultant.getLinks().getDelete(), notNullValue());
      assertThat(consultant.getLinks().getSelf(), notNullValue());
      assertThat(consultant.getLinks().getUpdate(), notNullValue());
    });
  }

}
