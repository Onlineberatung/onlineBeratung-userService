package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.config.auth.UserRole.CONSULTANT;
import static de.caritas.cob.userservice.api.config.auth.UserRole.GROUP_CHAT_CONSULTANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.tenantadminservice.generated.web.model.Licensing;
import de.caritas.cob.userservice.tenantadminservice.generated.web.model.TenantDTO;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
public class CreateConsultantSagaTenantAwareIT {

  private static final String DUMMY_RC_ID = "rcUserId";
  private static final String VALID_USERNAME = "validUsername";
  private static final String VALID_EMAILADDRESS = "valid@emailaddress.de";
  private static final long TENANT_ID = 1;

  @Autowired private CreateConsultantSaga createConsultantSaga;

  @Autowired private ConsultantRepository consultantRepository;

  @MockBean private TenantAdminService tenantAdminService;

  @MockBean private RocketChatService rocketChatService;

  @MockBean private KeycloakService keycloakService;

  private final EasyRandom easyRandom = new EasyRandom();

  @AfterEach
  public void tearDown() {
    TenantContext.clear();
  }

  @Test(expected = CustomValidationHttpStatusException.class)
  public void
      createNewConsultant_Should_throwCustomValidationHttpStatusException_When_LicensesAreExceeded() {
    // given
    givenTenantApiCall();
    createConsultant("username1");
    createConsultant("username2");
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    this.createConsultantSaga.createNewConsultant(createConsultantDTO);
    rollbackDBState();
  }

  @Test
  public void
      createNewConsultant_Should_addConsultantAndGroupChatConsultantRole_When_isGroupChatConsultantFlagIsEnabled()
          throws RocketChatLoginException {
    // given
    TenantContext.setCurrentTenant(1L);
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    var tenant =
        new TenantDTO()
            .licensing(new Licensing().allowedNumberOfUsers(1))
            .settings(
                new de.caritas.cob.userservice.tenantadminservice.generated.web.model.Settings()
                    .featureGroupChatV2Enabled(false));
    when(tenantAdminService.getTenantById(Mockito.anyLong())).thenReturn(tenant);

    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setTenantId(TENANT_ID);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(true);
    createConsultantDTO.setTenantId(1L);

    // when
    ConsultantAdminResponseDTO consultant =
        createConsultantSaga.createNewConsultant(createConsultantDTO);

    // then
    verify(keycloakService, times(2)).updateRole(anyString(), anyString());
    verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
    verify(keycloakService).updateRole(anyString(), eq(GROUP_CHAT_CONSULTANT.getValue()));

    assertThat(consultant.getEmbedded(), notNullValue());
    assertThat(consultant.getEmbedded().getId(), notNullValue());
  }

  private void createConsultant(String username) {
    Consultant consultant = new Consultant();
    consultant.setAppointments(null);
    consultant.setTenantId(1L);
    consultant.setId(username);
    consultant.setRocketChatId(username);
    consultant.setUsername(username);
    consultant.setFirstName(username);
    consultant.setLastName(username);
    consultant.setEmail(username + "@email.com");
    consultant.setEncourage2fa(true);
    consultant.setNotifyEnquiriesRepeating(true);
    consultant.setNotifyNewChatMessageFromAdviceSeeker(true);
    consultant.setNotifyNewFeedbackMessageFromAdviceSeeker(true);
    consultant.setWalkThroughEnabled(true);
    consultant.setLanguageCode(LanguageCode.de);

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
    ReflectionTestUtils.setField(createConsultantSaga, "tenantAdminService", tenantAdminService);
    when(tenantAdminService.getTenantById(TenantContext.getCurrentTenant()))
        .thenReturn(dummyTenant);
  }
}
