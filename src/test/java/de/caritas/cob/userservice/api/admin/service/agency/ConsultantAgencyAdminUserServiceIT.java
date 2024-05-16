package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantAgencyAdminUserServiceIT {

  @Autowired private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @MockBean private AgencyService agencyService;

  @MockBean private AgencyAdminService agencyAdminService;

  @MockBean private RemoveConsultantFromRocketChatService removeConsultantFromRocketChatService;

  @Test
  public void
      findConsultantAgencies_Should_returnAllConsultantAgenciesForGivenConsultantId_with_correctConsultantId() {
    var agencyAdminResponseDTO = new EasyRandom().nextObject(AgencyAdminResponseDTO.class);
    agencyAdminResponseDTO.setId(0L);
    var anotherAgencyAdminResponseDTO = new EasyRandom().nextObject(AgencyAdminResponseDTO.class);
    anotherAgencyAdminResponseDTO.setId(1L);
    when(this.agencyAdminService.retrieveAllAgencies())
        .thenReturn(List.of(agencyAdminResponseDTO, anotherAgencyAdminResponseDTO));

    var consultantAgencies =
        consultantAgencyAdminService.findConsultantAgencies("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe");

    assertThat(consultantAgencies, notNullValue());
    assertThat(consultantAgencies.getEmbedded(), hasSize(2));
    assertThat(consultantAgencies.getTotal(), is(2));
    assertThat(
        consultantAgencies.getLinks().getSelf().getHref(),
        is("http://localhost/useradmin/consultants/5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe/agencies"));
  }

  @Test
  public void findConsultantAgencies_Should_returnFullMappedSessionAdminDTO() {
    var agencyAdminResponseDTO = new EasyRandom().nextObject(AgencyAdminResponseDTO.class);
    agencyAdminResponseDTO.setId(0L);
    when(this.agencyAdminService.retrieveAllAgencies())
        .thenReturn(singletonList(agencyAdminResponseDTO));

    var consultantAgencies =
        consultantAgencyAdminService.findConsultantAgencies("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe");

    var consultantAgencyAdminDTO = consultantAgencies.getEmbedded().iterator().next();
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
      assertThat(
          e.getMessage(),
          is("Consultant with id 12345678-1234-1234-1234-1234567890ab does not exist"));
    }
  }

  @Test
  public void
      markAllAssignedConsultantsAsTeamConsultant_Should_markAssignedConsultantsAsTeamConsultant() {
    long teamCosnultantsBefore =
        this.consultantRepository
            .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(1L))
            .stream()
            .filter(Consultant::isTeamConsultant)
            .count();

    this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(1L);

    long teamConsultantsAfter =
        this.consultantRepository
            .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(1L))
            .stream()
            .filter(Consultant::isTeamConsultant)
            .count();

    assertThat(teamConsultantsAfter, is(not(teamCosnultantsBefore)));
    assertThat(teamConsultantsAfter, is(greaterThan(teamCosnultantsBefore)));
  }

  @Test
  public void
      removeConsultantsFromTeamSessionsByAgencyId_Should_removeTeamConsultantFlagAndCallServices() {
    when(this.agencyService.getAgency(any())).thenReturn(new AgencyDTO().teamAgency(false));

    long teamCosnultantsBefore =
        this.consultantRepository
            .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(0L))
            .stream()
            .filter(Consultant::isTeamConsultant)
            .count();

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(0L);

    long teamConsultantsAfter =
        this.consultantRepository
            .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(0L))
            .stream()
            .filter(Consultant::isTeamConsultant)
            .count();

    assertThat(teamConsultantsAfter, is(not(teamCosnultantsBefore)));
    assertThat(teamConsultantsAfter, is(lessThan(teamCosnultantsBefore)));
    verify(this.removeConsultantFromRocketChatService, times(1))
        .removeConsultantFromSessions(any());
  }

  @Test
  public void
      markConsultantAgencyForDeletion_Should_setDeletedFlagIndatabase_When_consultantAgencyCanBeDeleted() {
    ConsultantAgency validRelation = this.consultantAgencyRepository.findAll().iterator().next();
    String consultantId = validRelation.getConsultant().getId();
    Long agencyId = validRelation.getAgencyId();

    this.consultantAgencyAdminService.markConsultantAgencyForDeletion(consultantId, agencyId);

    ConsultantAgency deletedConsultantAgency =
        this.consultantAgencyRepository.findById(validRelation.getId()).orElseThrow();
    assertThat(deletedConsultantAgency.getDeleteDate(), notNullValue());
  }

  @Test
  public void markConsultantAgenciesForDeletionShouldMark() {
    var consultant =
        consultantRepository.findById("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe").orElseThrow();
    var consultantAgencies = consultant.getConsultantAgencies();
    assertTrue(consultantAgencies.size() > 0);
    consultantAgencies.forEach(
        consultantAgency -> assertTrue(isNull(consultantAgency.getDeleteDate())));

    consultantAgencyAdminService.markConsultantAgenciesForDeletion(
        consultant.getId(),
        consultantAgencies.stream().map(el -> el.getAgencyId()).collect(Collectors.toList()));

    var consultantAgencyIds =
        consultantAgencies.stream().map(ConsultantAgency::getId).collect(Collectors.toList());
    consultantAgencyRepository
        .findAllById(consultantAgencyIds)
        .forEach(
            consultantAgency -> {
              assertThat(consultantAgency.getDeleteDate(), notNullValue());
              consultantAgency.setDeleteDate(null);
              consultantAgencyRepository.save(consultantAgency);
            });
  }

  @Test
  public void
      findConsultantsForAgency_Should_returnExpectedConsultants_When_agencyHasConsultatns() {
    var consultantsOfAgency = this.consultantAgencyAdminService.findConsultantsForAgency(1L);

    assertThat(consultantsOfAgency.getEmbedded(), hasSize(4));
    consultantsOfAgency
        .getEmbedded()
        .forEach(
            consultant -> {
              assertThat(consultant.getEmbedded(), notNullValue());
              assertThat(consultant.getLinks(), notNullValue());
              assertThat(consultant.getLinks().getAddAgency(), notNullValue());
              assertThat(consultant.getLinks().getAgencies(), notNullValue());
              assertThat(consultant.getLinks().getDelete(), notNullValue());
              assertThat(consultant.getLinks().getSelf(), notNullValue());
              assertThat(consultant.getLinks().getUpdate(), notNullValue());
            });
  }

  @Test
  public void appendAgenciesForConsultants_Should_enrichConsultants_When_consultantHasAgencies() {
    var persistedConsultant = consultantRepository.findAll().iterator().next();
    var consultantDTO =
        de.caritas.cob.userservice.api.admin.service.consultant.ConsultantResponseDTOBuilder
            .getInstance(persistedConsultant)
            .buildResponseDTO()
            .getEmbedded();
    var agencyOfConsultant = new EasyRandom().nextObject(AgencyAdminResponseDTO.class);
    agencyOfConsultant.setId(1731L);
    when(agencyAdminService.retrieveAllAgencies()).thenReturn(List.of(agencyOfConsultant));

    this.consultantAgencyAdminService.appendAgenciesForConsultants(Set.of(consultantDTO));

    assertThat(consultantDTO.getAgencies(), hasSize(1));
    assertThat(consultantDTO.getAgencies().get(0).getName(), is(agencyOfConsultant.getName()));
  }
}
