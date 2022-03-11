package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.tenantservice.generated.web.model.Licensing;
import de.caritas.cob.userservice.tenantservice.generated.web.model.TenantDTO;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = "multitenancy.enabled=true")
@Transactional
public class ConsultantCreatorServiceTenantAwareIT {

  @Autowired
  private ConsultantCreatorService consultantCreatorService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @MockBean
  private TenantService tenantService;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test(expected = CustomValidationHttpStatusException.class)
  public void createNewConsultant_Should_throwCustomValidationHttpStatusException_When_LicensesAreExceeded() {
    //given
    givenTenantApiCall();
    createConsultant("username1");
    createConsultant("username2");
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    this.consultantCreatorService.createNewConsultant(createConsultantDTO);
    rollbackDBState();
  }

  private void createConsultant(String username) {
    Consultant consultant = new Consultant();
    consultant.setTenantId(1L);
    consultant.setId(username);
    consultant.setRocketChatId(username);
    consultant.setUsername(username);
    consultant.setFirstName(username);
    consultant.setLastName(username);
    consultant.setEmail(username + "@email.com");
    consultantRepository.save(consultant);
  }

  private void rollbackDBState() {
    Iterable<Consultant> all = consultantRepository.findAll();
    for (Consultant c : all) {
      c.setDeleteDate(null);
    }
    consultantRepository.saveAll(all);
    TenantContext.clear();
  }

  private void givenTenantApiCall() {
    var currentTenant = new TenantData(1L, "testdomain");
    TenantContext.setCurrentTenantData(currentTenant);
    var dummyTenant = new TenantDTO();
    var licensing = new Licensing();
    licensing.setAllowedNumberOfUsers(2);
    dummyTenant.setLicensing(licensing);
    ReflectionTestUtils
        .setField(consultantCreatorService, "tenantService", tenantService);
    when(tenantService.getTenantById()).thenReturn(dummyTenant);
  }

}
