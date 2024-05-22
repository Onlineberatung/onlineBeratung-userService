package de.caritas.cob.userservice.api.admin.service.consultant;

import de.caritas.cob.userservice.api.UserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantAdminFilterServiceIT extends ConsultantAdminFilterServiceBase {

  @Test
  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsGiven() {
    super.findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsGiven();
  }

  @Test
  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsNull() {
    super.findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsNull();
  }

  @Test
  public void findFilteredConsultants_Should_returnOneConsultant_When_perPageIsNegativeValue() {
    super.findFilteredConsultants_Should_returnOneConsultant_When_perPageIsNegativeValue();
  }

  @Test
  public void findFilteredConsultants_Should_returnFullMappedConsultantSearchResultDTO() {
    super.findFilteredConsultants_Should_returnFullMappedConsultantSearchResultDTO();
  }

  @Test
  public void findFilteredConsultants_Should_haveCorrectPagedResults_When_noFilterIsGiven() {
    super.findFilteredConsultants_Should_haveCorrectPagedResults_When_noFilterIsGiven();
  }

  @Test
  public void findFilteredConsultants_Should_returnExpectedConsultant_When_allFiltersAreSet() {
    super.findFilteredConsultants_Should_returnExpectedConsultant_When_allFiltersAreSet();
  }

  @Test
  public void findFilteredConsultants_Should_returnNonAbsentConsultants_When_filterAbsentIsFalse() {
    super.findFilteredConsultants_Should_returnNonAbsentConsultants_When_filterAbsentIsFalse();
  }

  @Test
  public void findFilteredConsultants_Should_returnAllLastnameConsultants_When_filterLastname() {
    super.findFilteredConsultants_Should_returnAllLastnameConsultants_When_filterLastname();
  }

  @Test
  public void findFilteredConsultants_Should_returnAllEmailConsultants_When_filterEmail() {
    super.findFilteredConsultants_Should_returnAllEmailConsultants_When_filterEmail();
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithSelfLink() {
    super.findFilteredConsultants_Should_returnResultWithSelfLink();
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithoutNextLink_When_pageIsTheLast() {
    super.findFilteredConsultants_Should_returnResultWithoutNextLink_When_pageIsTheLast();
  }

  @Test
  public void findFilteredConsultants_Should_returnResultWithoutPreviousLink_When_pageIsTheFirst() {
    super.findFilteredConsultants_Should_returnResultWithoutPreviousLink_When_pageIsTheFirst();
  }

  @Test
  public void
      findFilteredConsultants_Should_returnResultWithoutExpectedNextLink_When_pageIsNotTheLast() {
    super
        .findFilteredConsultants_Should_returnResultWithoutExpectedNextLink_When_pageIsNotTheLast();
  }

  @Test
  public void
      findFilteredConsultants_Should_returnResultWithoutExpectedPreviousLink_When_pageIsNotTheFirst() {
    super
        .findFilteredConsultants_Should_returnResultWithoutExpectedPreviousLink_When_pageIsNotTheFirst();
  }

  @Test
  public void
      findFilteredConsultants_Should_orderResultByFirstNameDESC_When_sortParameterIsGiven() {
    super.findFilteredConsultants_Should_orderResultByFirstNameDESC_When_sortParameterIsGiven();
  }

  @Test
  public void findFilteredConsultants_Should_orderResultByEmailASC_When_sortParameterIsGiven() {
    super.findFilteredConsultants_Should_orderResultByEmailASC_When_sortParameterIsGiven();
  }
}
