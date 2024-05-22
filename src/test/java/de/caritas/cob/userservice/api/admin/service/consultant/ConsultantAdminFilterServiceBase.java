package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.Organizer;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.FieldEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.OrderEnum;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ConsultantAdminFilterServiceBase {

  @Autowired private ConsultantAdminFilterService consultantAdminFilterService;

  @MockBean
  @SuppressWarnings("unused")
  private Organizer organizer;

  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsGiven() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, new ConsultantFilter(), new Sort());

    assertThat(consultants.getEmbedded(), hasSize(39));
  }

  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsNull() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(1, 100, null, new Sort());

    assertThat(consultants.getEmbedded(), hasSize(39));
  }

  public void findFilteredConsultants_Should_returnOneConsultant_When_perPageIsNegativeValue() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, -100, new ConsultantFilter(), new Sort());

    assertThat(consultants.getEmbedded(), hasSize(1));
  }

  public void findFilteredConsultants_Should_returnFullMappedConsultantSearchResultDTO() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 1, new ConsultantFilter(), new Sort());

    ConsultantDTO consultantDTO = consultants.getEmbedded().iterator().next().getEmbedded();
    assertThat(consultantDTO.getId(), notNullValue());
    assertThat(consultantDTO.getUsername(), notNullValue());
    assertThat(consultantDTO.getFirstname(), notNullValue());
    assertThat(consultantDTO.getLastname(), notNullValue());
    assertThat(consultantDTO.getEmail(), notNullValue());
    assertThat(consultantDTO.getFormalLanguage(), notNullValue());
    assertThat(consultantDTO.getTeamConsultant(), notNullValue());
    assertThat(consultantDTO.getAbsent(), notNullValue());
    assertThat(consultantDTO.getCreateDate(), notNullValue());
    assertThat(consultantDTO.getUpdateDate(), notNullValue());
  }

  public void findFilteredConsultants_Should_haveCorrectPagedResults_When_noFilterIsGiven() {
    ConsultantSearchResultDTO firstPage =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 30, new ConsultantFilter(), null);

    ConsultantSearchResultDTO secondPage =
        this.consultantAdminFilterService.findFilteredConsultants(
            2, 30, new ConsultantFilter(), null);

    assertThat(firstPage.getEmbedded(), hasSize(30));
    assertThat(secondPage.getEmbedded(), hasSize(9));
  }

  public void findFilteredConsultants_Should_returnExpectedConsultant_When_allFiltersAreSet() {
    ConsultantFilter consultantFilter =
        new ConsultantFilter()
            .agencyId(1L)
            .absent(false)
            .email("addiction@consultant.de")
            .lastname("Consultant")
            .username("enc.MFSGI2LDORUW63RNMRSWMYLVNR2A....");

    ConsultantSearchResultDTO result =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, consultantFilter, new Sort());

    assertThat(result.getEmbedded(), hasSize(1));
    ConsultantDTO filteredConsultant = result.getEmbedded().iterator().next().getEmbedded();
    assertThat(filteredConsultant.getAbsent(), is(false));
    assertThat(filteredConsultant.getEmail(), is("addiction@consultant.de"));
    assertThat(filteredConsultant.getLastname(), is("Consultant"));
    assertThat(filteredConsultant.getUsername(), is("enc.MFSGI2LDORUW63RNMRSWMYLVNR2A...."));
  }

  public void findFilteredConsultants_Should_returnNonAbsentConsultants_When_filterAbsentIsFalse() {
    ConsultantFilter consultantFilter = new ConsultantFilter().absent(false);

    ConsultantSearchResultDTO result =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, consultantFilter, new Sort());

    result
        .getEmbedded()
        .forEach(consultant -> assertThat(consultant.getEmbedded().getAbsent(), is(false)));
  }

  public void findFilteredConsultants_Should_returnAllLastnameConsultants_When_filterLastname() {
    ConsultantFilter consultantFilter = new ConsultantFilter().lastname("Consultant");

    ConsultantSearchResultDTO result =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, consultantFilter, new Sort());

    result
        .getEmbedded()
        .forEach(
            consultant ->
                assertThat(consultant.getEmbedded().getLastname(), startsWith("Consultant")));
  }

  public void findFilteredConsultants_Should_returnAllEmailConsultants_When_filterEmail() {
    ConsultantFilter consultantFilter = new ConsultantFilter().email("addiction@caritas.de");

    ConsultantSearchResultDTO result =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, consultantFilter, new Sort());

    result
        .getEmbedded()
        .forEach(
            consultant ->
                assertThat(consultant.getEmbedded().getEmail(), is("addiction@caritas.de")));
  }

  public void findFilteredConsultants_Should_returnResultWithSelfLink() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, new ConsultantFilter(), new Sort());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getSelf(), notNullValue());
    assertThat(
        consultants.getLinks().getSelf().getMethod().getValue(), is(MethodEnum.GET.getValue()));
    assertThat(
        consultants.getLinks().getSelf().getHref(),
        endsWith("/useradmin/consultants?page=1&perPage=100"));
  }

  public void findFilteredConsultants_Should_returnResultWithoutNextLink_When_pageIsTheLast() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, new ConsultantFilter(), new Sort());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getNext(), nullValue());
  }

  public void findFilteredConsultants_Should_returnResultWithoutPreviousLink_When_pageIsTheFirst() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 100, new ConsultantFilter(), new Sort());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getPrevious(), nullValue());
  }

  public void
      findFilteredConsultants_Should_returnResultWithoutExpectedNextLink_When_pageIsNotTheLast() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 10, new ConsultantFilter(), new Sort());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getNext(), notNullValue());
    assertThat(
        consultants.getLinks().getNext().getHref(),
        endsWith("/useradmin/consultants?page=2&perPage=10"));
  }

  public void
      findFilteredConsultants_Should_returnResultWithoutExpectedPreviousLink_When_pageIsNotTheFirst() {
    ConsultantSearchResultDTO consultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            3, 10, new ConsultantFilter(), new Sort());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getPrevious(), notNullValue());
    assertThat(
        consultants.getLinks().getPrevious().getHref(),
        endsWith("/useradmin/consultants?page=2&perPage=10"));
  }

  public void
      findFilteredConsultants_Should_orderResultByFirstNameDESC_When_sortParameterIsGiven() {
    var sort = new Sort().field(FieldEnum.FIRSTNAME).order(OrderEnum.DESC);

    var result =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 30, new ConsultantFilter(), sort);

    var firstNames =
        result.getEmbedded().stream()
            .map(ConsultantAdminResponseDTO::getEmbedded)
            .map(ConsultantDTO::getFirstname)
            .collect(Collectors.toList());

    var iterator = firstNames.iterator();
    var beforeElement = iterator.next();
    while (iterator.hasNext()) {
      var currentElement = iterator.next();
      assertThat(beforeElement.compareTo(currentElement), greaterThanOrEqualTo(0));
      beforeElement = currentElement;
    }
  }

  public void findFilteredConsultants_Should_orderResultByEmailASC_When_sortParameterIsGiven() {
    var sort = new Sort().field(FieldEnum.EMAIL).order(OrderEnum.ASC);

    var result =
        this.consultantAdminFilterService.findFilteredConsultants(
            1, 30, new ConsultantFilter(), sort);

    var firstNames =
        result.getEmbedded().stream()
            .map(ConsultantAdminResponseDTO::getEmbedded)
            .map(ConsultantDTO::getEmail)
            .collect(Collectors.toList());

    var iterator = firstNames.iterator();
    var beforeElement = iterator.next();
    while (iterator.hasNext()) {
      var currentElement = iterator.next();
      assertThat(currentElement.compareTo(beforeElement), greaterThanOrEqualTo(0));
      beforeElement = currentElement;
    }
  }
}
