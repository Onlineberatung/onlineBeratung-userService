package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.UserDtoMapper;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ConsultantAgencyServiceTest {

  private final String CONSULTANT_ID = "1b71cc46-650d-42bb-8299-f8e3f6d7249a";
  private final String CONSULTANT_ROCKETCHAT_ID = "xN3Mobksn3xdp7gEk";
  private final Long AGENCY_ID = 1L;
  private final Consultant CONSULTANT =
      new Consultant(
          CONSULTANT_ID,
          CONSULTANT_ROCKETCHAT_ID,
          "consultant",
          "first name",
          "last name",
          "consultant@cob.de",
          false,
          false,
          null,
          false,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(
          AGENCY_ID, CONSULTANT, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final List<ConsultantAgency> CONSULTANT_AGENCY_LIST = Arrays.asList(CONSULTANT_AGENCY);
  private final ConsultantAgency NULL_CONSULTANT_AGENCY = null;
  private final List<ConsultantAgency> CONSULTANT_AGENCY_NULL_LIST =
      Arrays.asList(NULL_CONSULTANT_AGENCY);
  private final ConsultantAgency CONSULTANT_NULL_AGENCY =
      new ConsultantAgency(AGENCY_ID, null, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final List<ConsultantAgency> CONSULTANT_NULL_AGENCY_LIST =
      Arrays.asList(CONSULTANT_NULL_AGENCY);
  private final String ERROR = "error";

  @InjectMocks private ConsultantAgencyService consultantAgencyService;
  @Mock private ConsultantAgencyRepository consultantAgencyRepository;
  @Mock private Logger logger;
  @Mock private AgencyService agencyService;

  @Mock
  @SuppressWarnings("unused")
  private AccountManaging accountManager;

  @Mock
  @SuppressWarnings("unused")
  private UserDtoMapper userDtoMapper;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void saveConsultantAgency_Should_SaveConsultantAgency() {

    consultantAgencyService.saveConsultantAgency(CONSULTANT_AGENCY);
    verify(consultantAgencyRepository, times(1)).save(Mockito.any());
  }

  /** Method: findConsultantsByAgencyId */
  @Test
  public void findConsultantsByAgencyId_Should_ReturnListOfConsultantAgency_WhenAgencyFound() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(Mockito.anyLong()))
        .thenReturn(CONSULTANT_AGENCY_LIST);

    assertThat(
        consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID),
        everyItem(instanceOf(ConsultantAgency.class)));
  }

  /** Method: getConsultantsOfAgency */
  @Test
  public void
      getConsultantsOfAgency_Should_ThrowInternalServerErrorException_WhenDatabaseAgencyIsNull() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(
            Mockito.anyLong()))
        .thenReturn(CONSULTANT_AGENCY_NULL_LIST);

    try {
      consultantAgencyService.getConsultantsOfAgency(AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void
      getConsultantsOfAgency_Should_ThrowInternalServerErrorException_WhenDatabaseAgencyConsultantIsNull() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(
            Mockito.anyLong()))
        .thenReturn(CONSULTANT_NULL_AGENCY_LIST);

    try {
      consultantAgencyService.getConsultantsOfAgency(AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getConsultantsOfAgency_Should_ReturnListOfConsultantAgency_WhenAgencyFound() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(
            Mockito.anyLong()))
        .thenReturn(CONSULTANT_AGENCY_LIST);

    assertThat(
        consultantAgencyService.getConsultantsOfAgency(AGENCY_ID),
        everyItem(instanceOf(ConsultantResponseDTO.class)));
  }

  @Test
  public void getConsultantsOfAgency_Should_ReturnOnlyConsultantsNotMarkedAsDeleted() {
    var consultantAgencies =
        new EasyRandom().objects(ConsultantAgency.class, 10).collect(Collectors.toList());
    removeDeletionFlagForConsultantAtIndex(consultantAgencies, 0, 2, 4, 6, 8, 9);
    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(
            any()))
        .thenReturn(consultantAgencies);

    var consultants = consultantAgencyService.getConsultantsOfAgency(0L);

    assertThat(consultants, hasSize(6));
  }

  private void removeDeletionFlagForConsultantAtIndex(
      List<ConsultantAgency> consultantAgencies, int... indexRange) {
    Arrays.stream(indexRange)
        .mapToObj(consultantAgencies::get)
        .map(ConsultantAgency::getConsultant)
        .forEach(consultant -> consultant.setDeleteDate(null));
  }

  @Test
  public void getOnlineAgenciesOfConsultant_Should_returnEmptyList_When_consultantDoesNotExist() {
    when(consultantAgencyRepository.findByConsultantId(any())).thenReturn(emptyList());

    var agencies = consultantAgencyService.getOnlineAgenciesOfConsultant("invalid");

    assertThat(agencies, hasSize(0));
  }

  @Test
  public void
      getOnlineAgenciesOfConsultant_Should_returnEmptyList_When_agencyForConsultantDoesNotExist() {
    var consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    when(consultantAgencyRepository.findByConsultantId(any()))
        .thenReturn(singletonList(consultantAgency));

    var agencies = consultantAgencyService.getOnlineAgenciesOfConsultant("valid");

    assertThat(agencies, hasSize(0));
  }

  @Test
  public void
      getOnlineAgenciesOfConsultant_Should_returnExpectedAgenciesAndFilterOutOfflineAgencies_When_consultantAgenciesExists() {
    List<ConsultantAgency> consultantAgencies = givenConsultantAgenciesWithDeletionDateNull();

    when(consultantAgencyRepository.findByConsultantId(any())).thenReturn(consultantAgencies);
    var agencyIds =
        consultantAgencies.stream().map(ConsultantAgency::getAgencyId).collect(Collectors.toList());
    List<AgencyDTO> agencies = mockAgenciesForIds(agencyIds);
    agencies.get(0).setOffline(true);
    when(agencyService.getAgenciesNotCached(agencyIds)).thenReturn(agencies);

    var resultAgencies = consultantAgencyService.getOnlineAgenciesOfConsultant("valid");

    assertThat(resultAgencies, hasSize(9));
    verify(agencyService, Mockito.never()).getAgencies(agencyIds);
    resultAgencies.forEach(
        agency -> {
          assertTrue(agencyIds.contains(agency.getId()));
          assertNotNull(agency.getConsultingType());
          assertNotNull(agency.getName());
          assertNotNull(agency.getCity());
          assertNotNull(agency.getDescription());
          assertNotNull(agency.getPostcode());
        });
  }

  private static List<ConsultantAgency> givenConsultantAgenciesWithDeletionDateNull() {
    var consultantAgencies =
        new EasyRandom().objects(ConsultantAgency.class, 10).collect(Collectors.toList());
    consultantAgencies.stream().forEach(ca -> ca.setDeleteDate(null));
    return consultantAgencies;
  }

  private List<AgencyDTO> mockAgenciesForIds(List<Long> agencyIds) {
    return agencyIds.stream()
        .map(
            agencyId -> {
              var agencyDTO = new EasyRandom().nextObject(AgencyDTO.class);
              agencyDTO.setId(agencyId);
              return agencyDTO;
            })
        .collect(Collectors.toList());
  }
}
