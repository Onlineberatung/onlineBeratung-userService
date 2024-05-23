package de.caritas.cob.userservice.api.admin.service.consultant;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = "multitenancy.enabled=true")
@Transactional
@TestPropertySource(
    properties =
        "spring.sql.init.data-locations=classpath:database/UserServiceDatabase.sql,classpath:database/transformDataForTenants.sql")
@Sql(scripts = {"classpath:database/transformDataForTenants.sql"})
public class ConsultantAdminFilterServiceTenantAwareIT extends ConsultantAdminFilterServiceBase {

  @Autowired ConsultantRepository consultantRepository;

  @BeforeEach
  public void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @AfterEach
  public void afterTests() {
    TenantContext.clear();
  }

  @Test
  public void findFilteredConsultants_Should_returnAllConsultants_When_noFilterIsGiven() {
    consultantRepository.findAll().forEach(consultant -> System.out.println(consultant));
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
}
