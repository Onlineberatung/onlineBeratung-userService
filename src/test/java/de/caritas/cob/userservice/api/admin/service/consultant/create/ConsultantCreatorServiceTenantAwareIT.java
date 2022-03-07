package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.Licensing;
import de.caritas.cob.userservice.tenantservice.generated.web.model.TenantDTO;
import java.time.LocalDateTime;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantCreatorServiceTenantAwareIT {

  @Autowired
  private ConsultantCreatorService consultantCreatorService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Mock
  private TenantControllerApi tenantControllerApi;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test(expected = CustomValidationHttpStatusException.class)
  public void createNewConsultant_Should_throwCustomValidationHttpStatusException_When_LicensesAreExceeded() {
    //given
    givenTenantApiCall();
    ReflectionTestUtils.setField(consultantCreatorService, "multiTenancyEnabled", true);
    givenDatabaseState_2_Consultants_Exist();
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    this.consultantCreatorService.createNewConsultant(createConsultantDTO);
    rollbackDBState();
  }

  private void givenDatabaseState_2_Consultants_Exist() {
    Iterable<Consultant> all = consultantRepository.findAll();
    for (Consultant c : all) {
      c.setDeleteDate(LocalDateTime.now());
    }
    var iterator = all.iterator();
    Consultant next1 = iterator.next();
    Consultant next2 = iterator.next();

    next1.setDeleteDate(null);
    next2.setDeleteDate(null);
    consultantRepository.saveAll(all);
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
    var currentTenant = new TenantData(11L, "testdomain");
    TenantContext.setCurrentTenantData(currentTenant);
    var dummyTenant = new TenantDTO();
    var licensing = new Licensing();
    licensing.setAllowedNumberOfUsers(2);
    dummyTenant.setLicensing(licensing);
    ReflectionTestUtils
        .setField(consultantCreatorService, "tenantControllerApi", tenantControllerApi);
    when(tenantControllerApi.getTenantById(currentTenant.getTenantId())).thenReturn(dummyTenant);
  }

}
