package de.caritas.cob.userservice.api.admin.service.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = "multitenancy.enabled=true")
@TestPropertySource(
    properties =
        "spring.datasource.data=classpath*:database/UserServiceDatabase.sql,classpath*:database/transformDataForTenants.sql")
@Transactional
public class SessionAdminServiceTenantAwareIT {

  @Autowired private SessionAdminService sessionAdminService;

  @Before
  public void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @After
  public void afterTests() {
    TenantContext.clear();
  }

  @Test
  public void findSessions_Should_haveCorrectPagedResults_When_noFilterIsGiven() {
    SessionAdminResultDTO firstPage =
        this.sessionAdminService.findSessions(1, 100, new SessionFilter());
    SessionAdminResultDTO secondPage =
        this.sessionAdminService.findSessions(2, 100, new SessionFilter());

    assertThat(firstPage.getEmbedded(), hasSize(100));
    assertThat(secondPage.getEmbedded(), hasSize(55));
  }
}
