package de.caritas.cob.userservice.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.model.ConsultingTypeResultDTO;
import de.caritas.cob.userservice.api.model.PaginationLinks;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
public class ConsultingTypeAdminServiceTest {

  @Autowired
  private ConsultingTypeAdminService consultingTypeAdminService;

  @Test
  public void findConsultingTypes_Should_returnOneResult_When_perPageIsSetToOneAndPageIsSetToOne() {
    EasyRandom easyRandom = new EasyRandom();
    List<ConsultingTypeResultDTO> randomConsultingTypeResultDTOList = easyRandom
        .objects(ConsultingTypeResultDTO.class, ConsultingType.values().length).collect(
            Collectors.toList());

    List<ConsultingTypeResultDTO> consultingTypes = this.consultingTypeAdminService
        .findConsultingTypes(1, 1)
        .getEmbedded();

    assertThat(consultingTypes, hasSize(1));
  }

  @Test
  public void findConsultingTypes_Should_returnOneResult_When_paginationParamsAreZero() {
    List<ConsultingTypeResultDTO> consultingTypes = this.consultingTypeAdminService
        .findConsultingTypes(0, 0)
        .getEmbedded();

    assertThat(consultingTypes, hasSize(1));
  }

  @Test
  public void findConsultingTypes_Should_returnOneResult_When_paginationParamsAreNegative() {
    List<ConsultingTypeResultDTO> consultingTypes = this.consultingTypeAdminService
        .findConsultingTypes(-100, -1000)
        .getEmbedded();

    assertThat(consultingTypes, hasSize(1));
  }

  @Test
  public void findConsultingTypes_Should_returnPaginatedEntities_When_paginationParamsAreSplitted() {
    List<ConsultingTypeResultDTO> firstPage = this.consultingTypeAdminService
        .findConsultingTypes(0, 20)
        .getEmbedded();
    List<ConsultingTypeResultDTO> secondPage = this.consultingTypeAdminService
        .findConsultingTypes(2, 9)
        .getEmbedded();

    assertThat(firstPage, hasSize(20));
    assertThat(secondPage, hasSize(9));
  }

  @Test
  public void buildAgencyAdminSearchResult_Should_haveExpectedLinks_When_AllParamsAreProvided() {
    PaginationLinks paginationLinks = this.consultingTypeAdminService
        .findConsultingTypes(1, 19).getLinks();

    assertThat(paginationLinks.getSelf(), notNullValue());
    assertThat(paginationLinks.getSelf().getHref(),
        endsWith("/useradmin/consultingtypes?page=1&perPage=19"));
    assertThat(paginationLinks.getPrevious(), nullValue());
    assertThat(paginationLinks.getNext(), notNullValue());
    assertThat(paginationLinks.getNext().getHref(),
        endsWith("/useradmin/consultingtypes?page=2&perPage=19"));
  }

  @Test
  public void findConsultingTypes_Should_returnAllConsultingTypes_When_ProvidedWithMaxPerPagesParam() {
    List<ConsultingTypeResultDTO> page = this.consultingTypeAdminService
        .findConsultingTypes(0, Integer.MAX_VALUE)
        .getEmbedded();

    assertThat(page, hasSize(20));
  }
}