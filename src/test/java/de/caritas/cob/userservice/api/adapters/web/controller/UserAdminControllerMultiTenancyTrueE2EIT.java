package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.AGENCY_ADMIN_PATH;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.ConsultingTypeServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.MailServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.tenant.TenantResolverService;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@TestPropertySource(properties = {"multitenancy.enabled=true"})
@Transactional
class UserAdminControllerMultiTenancyTrueE2EIT {

  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ConsultingTypeControllerApi consultingTypeControllerApi;

  @Autowired private IdentityConfig identityConfig;

  @MockBean
  private ConsultingTypeServiceApiControllerFactory consultingTypeServiceApiControllerFactory;

  @MockBean private MailServiceApiControllerFactory mailServiceApiControllerFactory;

  @MockBean
  @Qualifier("mailsControllerApi")
  private MailsControllerApi mailsControllerApi;

  @MockBean AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @MockBean IdentityClient identityClient;

  @MockBean TenantService tenantService;

  @MockBean TenantResolverService tenantResolverService;

  @MockBean AuthenticatedUser authenticatedUser;

  @AfterEach
  void reset() {
    identityConfig.setDisplayNameAllowedForConsultants(false);
  }

  @BeforeEach
  public void setUp() {
    when(agencyServiceApiControllerFactory.createControllerApi())
        .thenReturn(
            new TestAgencyControllerApi(
                new de.caritas.cob.userservice.agencyserivce.generated.ApiClient()));

    when(consultingTypeServiceApiControllerFactory.createControllerApi())
        .thenReturn(consultingTypeControllerApi);
    when(mailServiceApiControllerFactory.createControllerApi()).thenReturn(mailsControllerApi);

    KeycloakCreateUserResponseDTO keycloakResponse = new KeycloakCreateUserResponseDTO();
    keycloakResponse.setUserId(new EasyRandom().nextObject(String.class));
    when(identityClient.createKeycloakUser(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(keycloakResponse);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  void createNewAgencyAdmin_Should_returnOk_When_requiredCreateAgencyAdminIsGiven()
      throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("agencyadmin@email.com");
    createAdminDTO.setTenantId(95);
    givenTenant();
    givenTenantSuperAdmin();

    // when

    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post(AGENCY_ADMIN_PATH)
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createAdminDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_embedded.id", notNullValue()))
            .andExpect(jsonPath("_embedded.username", notNullValue()))
            .andExpect(jsonPath("_embedded.lastname", notNullValue()))
            .andExpect(jsonPath("_embedded.email", is("agencyadmin@email.com")))
            .andExpect(jsonPath("_embedded.tenantId", is("95")))
            .andReturn();
    String content = mvcResult.getResponse().getContentAsString();
    JsonPath.read(content, "_embedded.id");
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  void createNewAgencyAdmin_Should_return500_When_superAdminHasNullTenantID() throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("agencyadmin@email.com");
    createAdminDTO.setTenantId(null);
    givenTenant();
    givenTenantSuperAdmin();

    // when

    this.mockMvc
        .perform(
            post(AGENCY_ADMIN_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
        .andExpect(status().isInternalServerError())
        .andReturn();
  }

  private void givenTenantSuperAdmin() {
    when(authenticatedUser.isTenantSuperAdmin()).thenReturn(true);
  }

  private void givenTenant() {
    when(tenantResolverService.resolve(any())).thenReturn(95L);
    when(tenantService.getRestrictedTenantData(anyLong()))
        .thenReturn(new RestrictedTenantDTO().subdomain("subdomain"));
  }
}
