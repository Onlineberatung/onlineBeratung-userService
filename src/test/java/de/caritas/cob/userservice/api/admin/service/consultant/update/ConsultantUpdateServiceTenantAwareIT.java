package de.caritas.cob.userservice.api.admin.service.consultant.update;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class ConsultantUpdateServiceTenantAwareIT extends ConsultantUpdateServiceBase {

  protected String VALID_CONSULTANT_ID = "0b3b1cc6-be98-4787-aa56-212259d811b8";

  @Before
  public void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @After
  public void afterTests() {
    TenantContext.clear();
  }

  @Test
  public void updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid() {
    super.updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid();
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid() {
    super.updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid();
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid() {
    super.updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid();
  }

  protected String getValidConsultantId() {
    return VALID_CONSULTANT_ID;
  }


}
