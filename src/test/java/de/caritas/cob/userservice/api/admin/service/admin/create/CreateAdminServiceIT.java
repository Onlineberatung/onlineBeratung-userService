package de.caritas.cob.userservice.api.admin.service.admin.create;

import static de.caritas.cob.userservice.api.config.auth.UserRole.RESTRICTED_AGENCY_ADMIN;
import static de.caritas.cob.userservice.api.config.auth.UserRole.TOPIC_ADMIN;
import static de.caritas.cob.userservice.api.config.auth.UserRole.USER_ADMIN;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminType;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class CreateAdminServiceIT {

  private static final String VALID_USERNAME = "validUsername";
  private static final String VALID_EMAIL_ADDRESS = "valid@emailaddress.de";

  @Autowired private CreateAdminService createAdminService;
  @MockBean private IdentityClient identityClient;
  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void createNewAgencyAdmin_Should_returnExpectedCreatedAdmin_When_inputDataIsCorrect() {
    // given
    when(identityClient.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    CreateAdminDTO createAdminDTO = this.easyRandom.nextObject(CreateAdminDTO.class);
    createAdminDTO.setUsername(VALID_USERNAME);
    createAdminDTO.setEmail(VALID_EMAIL_ADDRESS);

    // when
    Admin admin = this.createAdminService.createNewAdmin(createAdminDTO, AdminType.AGENCY);

    // then
    verify(identityClient).updatePassword(anyString(), anyString());
    verify(identityClient).updateRole(anyString(), eq(RESTRICTED_AGENCY_ADMIN));
    verify(identityClient).updateRole(anyString(), eq(USER_ADMIN));

    assertThat(admin).isNotNull();
    assertThat(admin.getId()).isNotNull();
    assertThat(admin.getType()).isEqualTo(AdminType.AGENCY);
    assertThat(admin.getUsername()).isNotNull();
    ;
    assertThat(admin.getFirstName()).isNotNull();
    assertThat(admin.getLastName()).isNotNull();
    assertThat(admin.getEmail()).isNotNull();
    assertThat(admin.getCreateDate()).isNotNull();
    assertThat(admin.getUpdateDate()).isNotNull();
    assertThat(admin.getTenantId()).isNotNull();
  }

  @Test
  public void getUserRolesForTenantAdmin_ShouldGetProperDefaultRoles_ForSingleDomainMultitenancy() {
    Whitebox.setInternalState(createAdminService, "multitenancyWithSingleDomain", true);
    List<UserRole> defaultRoles = this.createAdminService.getDefaultRoles(AdminType.TENANT);

    assertThat(defaultRoles)
        .containsOnly(UserRole.AGENCY_ADMIN, UserRole.SINGLE_TENANT_ADMIN, USER_ADMIN);
  }

  @Test
  public void getUserRolesForTenantAdmin_ShouldGetProperDefaultRoles_ForMultidomainMultitenancy() {
    Whitebox.setInternalState(createAdminService, "multitenancyWithSingleDomain", false);
    List<UserRole> defaultRoles = this.createAdminService.getDefaultRoles(AdminType.TENANT);

    assertThat(defaultRoles)
        .containsOnly(UserRole.AGENCY_ADMIN, UserRole.SINGLE_TENANT_ADMIN, USER_ADMIN, TOPIC_ADMIN);
  }

  @Test(expected = CustomValidationHttpStatusException.class)
  public void
      createNewAdminAgency_Should_throwCustomValidationHttpStatusException_When_keycloakIdIsMissing() {
    // given
    KeycloakCreateUserResponseDTO keycloakResponse =
        easyRandom.nextObject(KeycloakCreateUserResponseDTO.class);
    keycloakResponse.setUserId(null);
    when(identityClient.createKeycloakUser(any(), anyString(), any())).thenReturn(keycloakResponse);
    CreateAdminDTO createAgencyAdminDTO = this.easyRandom.nextObject(CreateAdminDTO.class);

    // when
    this.createAdminService.createNewAdmin(createAgencyAdminDTO, AdminType.AGENCY);
  }

  @Test
  public void createNewAdminAgency_Should_throwExpectedException_When_emailIsInvalid() {
    // given
    CreateAdminDTO createAgencyAdminDTO = this.easyRandom.nextObject(CreateAdminDTO.class);
    createAgencyAdminDTO.setEmail("invalid");

    try {

      // when
      this.createAdminService.createNewAdmin(createAgencyAdminDTO, AdminType.AGENCY);
      fail("Exception should be thrown");

      // then
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader()).isNotNull();
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0)).isEqualTo(EMAIL_NOT_VALID.name());
    }
  }
}
