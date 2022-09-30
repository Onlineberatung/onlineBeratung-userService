package de.caritas.cob.userservice.api.admin.facade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.FieldEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.OrderEnum;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.config.apiclient.AgencyAdminServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.RolesDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

  @Autowired private ConsultantAdminFacade consultantAdminFacade;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @MockBean private AgencyAdminService agencyAdminService;

  @MockBean private ConsultingTypeManager consultingTypeManager;

  @MockBean private AgencyAdminServiceApiControllerFactory agencyAdminServiceApiControllerFactory;

  @MockBean private AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @Test
  public void
      findFilteredConsultants_Should_retrieveDeletedAgencyRelations_When_consultantIsDeleted() {
    var consultant = givenAPersistedDeletedConsultantWithTenAgencies();

    var searchResult =
        this.consultantAdminFacade.findFilteredConsultants(
            1,
            100,
            new ConsultantFilter(),
            new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.ASC));
    var resultConsultant =
        searchResult.getEmbedded().stream()
            .filter(
                consultantResponse ->
                    consultantResponse.getEmbedded().getId().equals(consultant.getId()))
            .collect(Collectors.toSet())
            .iterator()
            .next();

    assertThat(resultConsultant.getEmbedded().getDeleteDate(), notNullValue());
    assertThat(resultConsultant.getEmbedded().getAgencies(), hasSize(10));
  }

  @Test
  public void
      findFilteredConsultants_Should_retrieveOnlyNonDeletedAgencyRelations_When_consultantIsNotDeleted() {
    var consultant = givenAPersistedNonDeletedConsultantWithDeletedAndNotDeletedAgencies();

    var searchResult =
        this.consultantAdminFacade.findFilteredConsultants(
            1,
            100,
            new ConsultantFilter(),
            new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.ASC));
    var resultConsultant =
        searchResult.getEmbedded().stream()
            .filter(
                consultantResponse ->
                    consultantResponse.getEmbedded().getId().equals(consultant.getId()))
            .collect(Collectors.toSet())
            .iterator()
            .next();

    assertThat(resultConsultant.getEmbedded().getDeleteDate(), is("null"));
    assertThat(resultConsultant.getEmbedded().getAgencies(), hasSize(5));
    resultConsultant
        .getEmbedded()
        .getAgencies()
        .forEach(agency -> assertThat(agency.getDeleteDate(), is("null")));
  }

  private Consultant givenAPersistedNonDeletedConsultantWithDeletedAndNotDeletedAgencies() {
    var parameters = baseConsultantParameters().excludeField(FieldPredicates.named("deleteDate"));
    var consultant = new EasyRandom(parameters).nextObject(Consultant.class);
    consultantRepository.save(consultant);
    var consultantAgencies = buildPersistedAgenciesForConsultant(20, 5, consultant);
    consultant.setConsultantAgencies(consultantAgencies);
    mockAgencyServiceResponse(consultantAgencies);

    return consultant;
  }

  private void mockAgencyServiceResponse(Set<ConsultantAgency> consultantAgencies) {
    var mockedAgencies =
        consultantAgencies.stream()
            .map(
                consultantAgency ->
                    new AgencyAdminResponseDTO()
                        .id(consultantAgency.getAgencyId())
                        .deleteDate(String.valueOf(consultantAgency.getDeleteDate())))
            .collect(Collectors.toList());
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

  private Set<ConsultantAgency> buildPersistedAgenciesForConsultant(
      int amount, int notDeletedAmount, Consultant consultant) {
    var consultantAgencies =
        new EasyRandom()
            .objects(ConsultantAgency.class, amount)
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

  @Test
  public void findFilteredConsultants_Should_retrieveConsultantAfterAddingRelationToAgency() {

    var consultantId = "id";
    givenConsultantWithoutAgency(consultantId);
    when(consultingTypeManager.isConsultantBoundedToAgency(anyInt())).thenReturn(false);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(getExtendedConsultingTypeResponse());

    var agencyId = 999L;
    CreateConsultantAgencyDTO consultantAgencyDto = new CreateConsultantAgencyDTO();
    consultantAgencyDto.agencyId(agencyId);

    ConsultantFilter consultantFilter = new ConsultantFilter();
    consultantFilter.setAgencyId(agencyId);
    when(agencyServiceApiControllerFactory.createControllerApi())
        .thenReturn(new TestAgencyControllerApi(new ApiClient()));

    var searchResult =
        this.consultantAdminFacade.findFilteredConsultants(
            1, 100, consultantFilter, new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.ASC));

    assertThat(searchResult.getEmbedded(), hasSize(0));

    consultantAdminFacade.createNewConsultantAgency(consultantId, consultantAgencyDto);

    searchResult =
        this.consultantAdminFacade.findFilteredConsultants(
            1, 100, consultantFilter, new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.ASC));
    assertThat(searchResult.getEmbedded(), hasSize(1));
  }

  private ExtendedConsultingTypeResponseDTO getExtendedConsultingTypeResponse() {
    ExtendedConsultingTypeResponseDTO e = new ExtendedConsultingTypeResponseDTO();
    e.setRoles(new RolesDTO());
    return e;
  }

  private void givenConsultantWithoutAgency(String id) {
    Consultant newConsultant = new Consultant();
    newConsultant.setStatus(ConsultantStatus.CREATED);
    newConsultant.setLastName("lastName");
    newConsultant.setWalkThroughEnabled(false);
    newConsultant.setFirstName("firstName");
    newConsultant.setEmail("email@email.com");
    newConsultant.setRocketChatId("rocketChatId");
    newConsultant.setEncourage2fa(false);
    newConsultant.setUsername("username");
    newConsultant.setId(id);
    newConsultant.setNotifyEnquiriesRepeating(false);
    newConsultant.setNotifyNewChatMessageFromAdviceSeeker(false);
    newConsultant.setNotifyNewFeedbackMessageFromAdviceSeeker(false);
    newConsultant.setLanguageCode(LanguageCode.de);

    consultantRepository.save(newConsultant);
  }

  @Test
  public void testConsultantAgencyForDeletionFiltering() {
    List<AgencyAdminResponseDTO> result = new ArrayList<AgencyAdminResponseDTO>();
    AgencyAdminResponseDTO agency1 = new AgencyAdminResponseDTO();
    agency1.setId(110L);
    result.add(agency1);
    AgencyAdminResponseDTO agency2 = new AgencyAdminResponseDTO();
    agency2.setId(121L);
    result.add(agency2);
    when(this.agencyAdminService.retrieveAllAgencies()).thenReturn(result);

    List<CreateConsultantAgencyDTO> newList = new ArrayList<CreateConsultantAgencyDTO>();
    CreateConsultantAgencyDTO consultantAgency1 = new CreateConsultantAgencyDTO();
    consultantAgency1.setAgencyId(110L);
    newList.add(consultantAgency1);

    String consultanId = "45816eb6-984b-411f-a818-996cd16e1f2a";
    List<Long> filteredList =
        consultantAdminFacade.filterAgencyListForDeletion(consultanId, newList);
    assertThat(filteredList.size(), is(1));

    CreateConsultantAgencyDTO consultantAgency2 = new CreateConsultantAgencyDTO();
    consultantAgency2.setAgencyId(121L);
    newList.add(consultantAgency2);

    filteredList = consultantAdminFacade.filterAgencyListForDeletion(consultanId, newList);
    assertThat(filteredList.size(), is(0));
  }

  @Test
  public void testConsultantAgencyForCreationFiltering() {
    List<AgencyAdminResponseDTO> result = new ArrayList<AgencyAdminResponseDTO>();
    AgencyAdminResponseDTO agency1 = new AgencyAdminResponseDTO();
    agency1.setId(110L);
    result.add(agency1);
    AgencyAdminResponseDTO agency2 = new AgencyAdminResponseDTO();
    agency2.setId(121L);
    result.add(agency2);
    when(this.agencyAdminService.retrieveAllAgencies()).thenReturn(result);

    List<CreateConsultantAgencyDTO> newList = new ArrayList<CreateConsultantAgencyDTO>();
    CreateConsultantAgencyDTO consultantAgency1 = new CreateConsultantAgencyDTO();
    consultantAgency1.setAgencyId(110L);
    newList.add(consultantAgency1);

    String consultantId = "45816eb6-984b-411f-a818-996cd16e1f2a";
    consultantAdminFacade.filterAgencyListForCreation(consultantId, newList);
    assertThat(newList.size(), is(0));

    CreateConsultantAgencyDTO consultantAgency2 = new CreateConsultantAgencyDTO();
    consultantAgency2.setAgencyId(122L);
    newList.add(consultantAgency2);

    consultantAdminFacade.filterAgencyListForCreation(consultantId, newList);
    assertThat(newList.size(), is(1));
  }
}
