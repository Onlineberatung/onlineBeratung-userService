package de.caritas.cob.userservice.api.admin.facade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.admin.model.ConsultantFilter;
import de.caritas.cob.userservice.api.admin.model.Sort;
import de.caritas.cob.userservice.api.admin.model.Sort.FieldEnum;
import de.caritas.cob.userservice.api.admin.model.Sort.OrderEnum;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ConsultantAdminFacadeIT {

  @Autowired
  private ConsultantAdminFacade consultantAdminFacade;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private ConsultantAgencyRepository consultantAgencyRepository;

  @MockBean
  private AgencyAdminService agencyAdminService;

  @Test
  public void findFilteredConsultants_Should_retrieveDeletedAgencyRelations_When_consultantIsDeleted() {
    var consultant = givenAPersistedDeletedConsultantWithTenAgencies();

    var searchResult = this.consultantAdminFacade
        .findFilteredConsultants(1, 100, new ConsultantFilter(),
            new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.ASC));
    var resultConsultant = searchResult.getEmbedded().stream()
        .filter(consultantResponse -> consultantResponse.getEmbedded().getId()
            .equals(consultant.getId()))
        .collect(Collectors.toSet())
        .iterator().next();

    assertThat(resultConsultant.getEmbedded().getDeleteDate(), notNullValue());
    assertThat(resultConsultant.getEmbedded().getAgencies(), hasSize(10));
  }

  @Test
  public void findFilteredConsultants_Should_retrieveOnlyNonDeletedAgencyRelations_When_consultantIsNotDeleted() {
    var consultant = givenAPersistedNonDeletedConsultantWithDeletedAndNotDeletedAgencies();

    var searchResult = this.consultantAdminFacade
        .findFilteredConsultants(1, 100, new ConsultantFilter(),
            new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.ASC));
    var resultConsultant = searchResult.getEmbedded().stream()
        .filter(consultantResponse -> consultantResponse.getEmbedded().getId()
            .equals(consultant.getId()))
        .collect(Collectors.toSet())
        .iterator().next();

    assertThat(resultConsultant.getEmbedded().getDeleteDate(), is("null"));
    assertThat(resultConsultant.getEmbedded().getAgencies(), hasSize(5));
    resultConsultant.getEmbedded().getAgencies().forEach(agency ->
        assertThat(agency.getDeleteDate(), is("null")));
  }

  private Consultant givenAPersistedNonDeletedConsultantWithDeletedAndNotDeletedAgencies() {
    var parameters = baseConsultantParameters()
        .excludeField(FieldPredicates.named("deleteDate"));
    var consultant = new EasyRandom(parameters).nextObject(Consultant.class);
    consultantRepository.save(consultant);
    var consultantAgencies = buildPersistedAgenciesForConsultant(20, 5, consultant);
    consultant.setConsultantAgencies(consultantAgencies);
    mockAgencyServiceResponse(consultantAgencies);

    return consultant;
  }

  private void mockAgencyServiceResponse(Set<ConsultantAgency> consultantAgencies) {
    var mockedAgencies = consultantAgencies.stream()
        .map(consultantAgency -> new AgencyAdminResponseDTO()
            .id(consultantAgency.getAgencyId())
            .deleteDate(String.valueOf(consultantAgency.getDeleteDate()))
        ).collect(Collectors.toList());
    when(agencyAdminService.retrieveAllAgencies()).thenReturn(mockedAgencies);
  }

  private EasyRandomParameters baseConsultantParameters() {
    return new EasyRandomParameters()
        .stringLengthRange(1, 17)
        .excludeField(FieldPredicates.named("consultantAgencies"))
        .excludeField(FieldPredicates.named("languages"))
        .excludeField(FieldPredicates.named("consultantMobileTokens"))
        .excludeField(FieldPredicates.named("appointments"))
        .excludeField(FieldPredicates.named("sessions"));
  }

  private Set<ConsultantAgency> buildPersistedAgenciesForConsultant(int amount,
      int notDeletedAmount, Consultant consultant) {
    var consultantAgencies = new EasyRandom().objects(ConsultantAgency.class, amount)
        .peek(agencyRelation -> agencyRelation.setConsultant(consultant))
        .collect(Collectors.toList());
    for (int i = 0; i < notDeletedAmount; i++) {
      consultantAgencies.get(i).setDeleteDate(null);
    }
    consultantAgencyRepository.saveAll(consultantAgencies);

    return new HashSet<>(consultantAgencies);
  }

  private Consultant givenAPersistedDeletedConsultantWithTenAgencies() {
    var consultant = new EasyRandom(baseConsultantParameters()).nextObject(Consultant.class);
    consultantRepository.save(consultant);
    var consultantAgencies = buildPersistedAgenciesForConsultant(10, 0, consultant);
    consultant.setConsultantAgencies(consultantAgencies);
    mockAgencyServiceResponse(consultantAgencies);

    return consultant;
  }

}
