package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantmobiletoken.ConsultantMobileTokenRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
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
class ConsultantServiceTenantAwareIT extends ConsultantServiceITBase {

  @Autowired
  private ConsultantService consultantService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private ConsultantMobileTokenRepository consultantMobileTokenRepository;

  @BeforeEach
  void clearTokens() {
    TenantContext.setCurrentTenant(1L);
    this.consultantMobileTokenRepository.deleteAll();
  }

  @After
  void clear() {
    TenantContext.clear();
  }

  @Test
  void addMobileTokensToConsultant_Should_persistMobileTokens_When_tokensAreUnique() {
    super.addMobileTokensToConsultant_Should_persistMobileTokens_When_tokensAreUnique();
  }

  @Test
  void addMobileTokensToConsultant_Should_throwConflictException_When_tokenAlreadyExists() {
    TenantContext.setCurrentTenant(1L);
    super.addMobileTokensToConsultant_Should_throwConflictException_When_tokenAlreadyExists();
  }

}


