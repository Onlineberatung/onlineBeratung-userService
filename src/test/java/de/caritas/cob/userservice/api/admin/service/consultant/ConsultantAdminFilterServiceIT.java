package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.ConsultantDTO;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import de.caritas.cob.userservice.api.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
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
public class ConsultantAdminFilterServiceIT {

  @Autowired
  private ConsultantAdminFilterService consultantAdminFilterService;

  @MockBean
  private UsernameTranscoder usernameTranscoder;

  @Test
  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsGiven() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, new ConsultantFilter());

    assertThat(consultants.getEmbedded(), hasSize(37));
  }

  @Test
  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsNull() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, null);

    assertThat(consultants.getEmbedded(), hasSize(37));
  }

  @Test
  public void findFilteredConsultants_Should_returnOneConsultant_When_perPageIsNegativeValue() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, -100, new ConsultantFilter());

    assertThat(consultants.getEmbedded(), hasSize(1));
  }

  @Test
  public void findFilteredConsultants_Should_returnFullMappedConsultantSearchResultDTO() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 1, new ConsultantFilter());

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

  @Test
  public void findFilteredConsultants_Should_haveCorrectPagedResults_When_noFilterIsGiven() {
    ConsultantSearchResultDTO firstPage = this.consultantAdminFilterService
        .findFilteredConsultants(1, 30, new ConsultantFilter());
    ConsultantSearchResultDTO secondPage = this.consultantAdminFilterService
        .findFilteredConsultants(2, 30, new ConsultantFilter());

    assertThat(firstPage.getEmbedded(), hasSize(30));
    assertThat(secondPage.getEmbedded(), hasSize(7));
  }

  @Test
  public void findFilteredConsultants_Should_returnExpectedConsultant_When_allFiltersAreSet() {
    ConsultantFilter consultantFilter = new ConsultantFilter()
        .agencyId(1L)
        .absent(false)
        .email("addiction@consultant.de")
        .lastname("Consultant")
        .username("enc.MFSGI2LDORUW63RNMRSWMYLVNR2A....");

    ConsultantSearchResultDTO result = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, consultantFilter);

    assertThat(result.getEmbedded(), hasSize(1));
    ConsultantDTO filteredConsultant = result.getEmbedded().iterator().next().getEmbedded();
    assertThat(filteredConsultant.getAbsent(), is(false));
    assertThat(filteredConsultant.getEmail(), is("addiction@consultant.de"));
    assertThat(filteredConsultant.getLastname(), is("Consultant"));
    assertThat(filteredConsultant.getUsername(), is("enc.MFSGI2LDORUW63RNMRSWMYLVNR2A...."));
  }

  @Test
  public void findFilteredConsultants_Should_returnNonAbsentConsultants_When_filterAbsentIsFalse() {
    ConsultantFilter consultantFilter = new ConsultantFilter().absent(false);

    ConsultantSearchResultDTO result = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, consultantFilter);

    result.getEmbedded()
        .forEach(consultant -> assertThat(consultant.getEmbedded().getAbsent(), is(false)));
  }

  @Test
  public void findFilteredConsultants_Should_returnAllLastnameConsultants_When_filterLastname() {
    ConsultantFilter consultantFilter = new ConsultantFilter().lastname("Consultant");

    ConsultantSearchResultDTO result = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, consultantFilter);

    result.getEmbedded().forEach(consultant -> assertThat(consultant.getEmbedded().getLastname(),
        startsWith("Consultant")));
  }

  @Test
  public void findFilteredConsultants_Should_returnAllEmailConsultants_When_filterEmail() {
    ConsultantFilter consultantFilter = new ConsultantFilter().email("addiction@caritas.de");

    ConsultantSearchResultDTO result = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, consultantFilter);

    result.getEmbedded().forEach(consultant -> assertThat(consultant.getEmbedded().getEmail(),
        is("addiction@caritas.de")));
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithSelfLink() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, new ConsultantFilter());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getSelf(), notNullValue());
    assertThat(consultants.getLinks().getSelf().getMethod(), is(MethodEnum.GET));
    assertThat(consultants.getLinks().getSelf().getHref(),
        endsWith("/useradmin/consultants?page=1&perPage=100"));
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithoutNextLink_When_pageIsTheLast() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, new ConsultantFilter());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getNext(), nullValue());
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithoutPreviousLink_When_pageIsTheFirst() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 100, new ConsultantFilter());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getPrevious(), nullValue());
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithoutExpectedNextLink_When_pageIsNotTheLast() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(1, 10, new ConsultantFilter());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getNext(), notNullValue());
    assertThat(consultants.getLinks().getNext().getHref(),
        endsWith("/useradmin/consultants?page=2&perPage=10"));
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithoutExpectedPreviousLink_When_pageIsNotTheFirst() {
    ConsultantSearchResultDTO consultants = this.consultantAdminFilterService
        .findFilteredConsultants(3, 10, new ConsultantFilter());

    assertThat(consultants.getLinks(), notNullValue());
    assertThat(consultants.getLinks().getPrevious(), notNullValue());
    assertThat(consultants.getLinks().getPrevious().getHref(),
        endsWith("/useradmin/consultants?page=2&perPage=10"));
  }

}
