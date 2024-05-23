package de.caritas.cob.userservice.api.admin.service.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = "multitenancy.enabled=true")
@Sql(scripts = {"classpath:database/transformDataForTenants.sql"})
@ActiveProfiles("testing")
@Transactional
class SessionAdminServiceTenantAwareIT {

  @Autowired private SessionAdminService sessionAdminService;

  @Autowired private SessionRepository sessionRepository;

  @BeforeEach
  void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @AfterEach
  void afterTests() {
    TenantContext.clear();
  }

  @Test
  void findSessions_Should_haveCorrectPagedResults_When_noFilterIsGiven() {

    SessionAdminResultDTO firstPage =
        this.sessionAdminService.findSessions(1, 100, new SessionFilter());
    SessionAdminResultDTO secondPage =
        this.sessionAdminService.findSessions(2, 100, new SessionFilter());

    assertThat(firstPage.getEmbedded(), hasSize(100));
    assertThat(secondPage.getEmbedded(), hasSize(55));
  }
}
