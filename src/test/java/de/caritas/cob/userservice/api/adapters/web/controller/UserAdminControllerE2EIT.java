package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.AGENCY_ADMIN_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.TENANT_ADMIN_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.TENANT_ADMIN_PATH_WITHOUT_SLASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.ConsultingTypeServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.MailServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Admin.AdminType;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import java.util.LinkedHashMap;
import javax.servlet.http.Cookie;
import net.minidev.json.JSONArray;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
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
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@TestPropertySource(properties = {"feature.topics.enabled=true", "multitenancy.enabled=false"})
@Transactional
class UserAdminControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);
  public static final int PAGE_SIZE = 10;
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ConsultingTypeControllerApi consultingTypeControllerApi;

  @Autowired private IdentityConfig identityConfig;

  @Autowired private AdminRepository adminRepository;

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @MockBean
  private ConsultingTypeServiceApiControllerFactory consultingTypeServiceApiControllerFactory;

  @MockBean private MailServiceApiControllerFactory mailServiceApiControllerFactory;

  @MockBean
  @Qualifier("restTemplate")
  private RestTemplate restTemplate;

  @MockBean
  @Qualifier("keycloakRestTemplate")
  private RestTemplate keycloakRestTemplate;

  @MockBean
  @Qualifier("rocketChatRestTemplate")
  private RestTemplate rocketChatRestTemplate;

  @MockBean
  @Qualifier("topicControllerApiPrimary")
  private TopicControllerApi topicControllerApi;

  @MockBean
  @Qualifier("mailsControllerApi")
  private MailsControllerApi mailsControllerApi;

  @MockBean AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @MockBean private Keycloak keycloak;

  @MockBean IdentityClient identityClient;

  @MockBean TenantService tenantService;

  private User user;

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
  public void createNewAgencyAdmin_Should_returnOk_When_requiredCreateAgencyAdminIsGiven()
      throws Exception {
    givenNewAgencyAdminIsCreated();
  }

  private String givenNewAgencyAdminIsCreated() throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("agencyadmin@email.com");

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
            .andReturn();
    String content = mvcResult.getResponse().getContentAsString();
    return JsonPath.read(content, "_embedded.id");
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.SINGLE_TENANT_ADMIN})
  public void createNewAgencyAdmin_Should_returnForbidden_When_calledNotAsUserAdmin()
      throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("agencyadmin@email.com");

    // when

    this.mockMvc
        .perform(
            post(AGENCY_ADMIN_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void createNewTenantAdmin_Should_returnOk_When_requiredCreateTenantAdminIsGiven()
      throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("valid@email.com");

    // when

    this.mockMvc
        .perform(
            post(TENANT_ADMIN_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.id", notNullValue()))
        .andExpect(jsonPath("_embedded.username", notNullValue()))
        .andExpect(jsonPath("_embedded.lastname", notNullValue()))
        .andExpect(jsonPath("_embedded.email", is("valid@email.com")));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void
      createNewTenantAdmin_Should_returnBadRequest_When_requiredCreateTenantAdminIsGivenButTenantIdIsNull()
          throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("valid@email.com");
    createAdminDTO.setTenantId(null);

    // when

    this.mockMvc
        .perform(
            post(TENANT_ADMIN_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      createNewTenantAdmin_Should_returnForbidden_When_attemptedToCreateTenantAdminWithoutTenantAdminAuthority()
          throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("valid@email.com");

    // when
    this.mockMvc
        .perform(
            post(TENANT_ADMIN_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void updateAgencyAdmin_Should_returnOk_When_updateAttemptAsUserAdmin() throws Exception {
    // given
    String adminId = givenNewAgencyAdminIsCreated();

    UpdateTenantAdminDTO updateAdminDTO = new EasyRandom().nextObject(UpdateTenantAdminDTO.class);

    updateAdminDTO.setFirstname("changedFirstname");
    updateAdminDTO.setLastname("changedLastname");
    updateAdminDTO.setEmail("changed@email.com");
    updateAdminDTO.setTenantId(1);

    when(tenantService.getRestrictedTenantData(Mockito.anyLong()))
        .thenReturn(new RestrictedTenantDTO().subdomain("subdomain"));

    // when, then
    this.mockMvc
        .perform(
            put(AGENCY_ADMIN_PATH + adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAdminDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.id", is(adminId)))
        .andExpect(jsonPath("_embedded.firstname", is("changedFirstname")))
        .andExpect(jsonPath("_embedded.lastname", is("changedLastname")))
        .andExpect(jsonPath("_embedded.email", is("changed@email.com")));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.SINGLE_TENANT_ADMIN})
  public void updateAgencyAdmin_Should_returnForbidden_When_UpdateAttemptAsNonUserAdmin()
      throws Exception {
    // given
    String adminId = "5606179b-77e7-4056-aedc-68ddc769890c";

    UpdateAgencyAdminDTO updateAdminDTO = new UpdateAgencyAdminDTO();
    updateAdminDTO.setFirstname("changedFirstname");
    updateAdminDTO.setLastname("changedLastname");
    updateAdminDTO.setEmail("changed@email.com");

    when(tenantService.getRestrictedTenantData(Mockito.anyLong()))
        .thenReturn(new RestrictedTenantDTO().subdomain("subdomain"));

    // when, then
    this.mockMvc
        .perform(
            put(AGENCY_ADMIN_PATH + adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAdminDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void updateTenantAdmin_Should_returnOk_When_updateAttemptAsTenantAdmin() throws Exception {
    // given
    String adminId = givenNewTenantAdminIsCreated();

    UpdateTenantAdminDTO updateAdminDTO = new EasyRandom().nextObject(UpdateTenantAdminDTO.class);

    updateAdminDTO.setFirstname("changedFirstname");
    updateAdminDTO.setLastname("changedLastname");
    updateAdminDTO.setEmail("changed@email.com");
    updateAdminDTO.setTenantId(1);

    when(tenantService.getRestrictedTenantData(Mockito.anyLong()))
        .thenReturn(new RestrictedTenantDTO().subdomain("subdomain"));

    // when, then
    this.mockMvc
        .perform(
            put(TENANT_ADMIN_PATH + adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAdminDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.id", is(adminId)))
        .andExpect(jsonPath("_embedded.firstname", is("changedFirstname")))
        .andExpect(jsonPath("_embedded.lastname", is("changedLastname")))
        .andExpect(jsonPath("_embedded.email", is("changed@email.com")));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      updateTenantAdmin_Should_returnForbidden_When_attemptedToUpdatedTenantAdminWithoutTenantAdminAuthority()
          throws Exception {
    // given
    UpdateTenantAdminDTO updateAdminDTO = new EasyRandom().nextObject(UpdateTenantAdminDTO.class);
    var existingAdminId = "6584f4a9-a7f0-42f0-b929-ab5c99c0802d";
    updateAdminDTO.setFirstname("changedFirstname");
    updateAdminDTO.setLastname("changedLastname");
    updateAdminDTO.setEmail("changed@email.com");
    updateAdminDTO.setTenantId(1);

    // when, then
    this.mockMvc
        .perform(
            put(TENANT_ADMIN_PATH + existingAdminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAdminDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void getAgencydmin_Should_returnOk_When_attemptedToGetAgencyAdminWithTenantAdminAuthority()
      throws Exception {
    // given
    var existingAdminId = "5606179b-77e7-4056-aedc-68ddc769890c";

    // when, then
    this.mockMvc
        .perform(get(AGENCY_ADMIN_PATH + existingAdminId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.id", is(existingAdminId)))
        .andExpect(jsonPath("_embedded.username", is("bmachin1j")))
        .andExpect(jsonPath("_embedded.firstname", is("Barn")))
        .andExpect(jsonPath("_embedded.lastname", is("Machin")))
        .andExpect(jsonPath("_embedded.email", is("bmachin1j@senate.gov")));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.SINGLE_TENANT_ADMIN})
  public void
      getAgencydmin_Should_returnForbidden_When_attemptedToGetAgencyAdminWithoutUserAdminAuthority()
          throws Exception {
    // given
    var existingAdminId = "5606179b-77e7-4056-aedc-68ddc769890c";

    // when, then
    this.mockMvc
        .perform(get(AGENCY_ADMIN_PATH + existingAdminId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void
      getTenantAdmin_Should_returnOk_When_attemptedToGetTenantAdminWithTenantAdminAuthority()
          throws Exception {
    // given
    var existingAdminId = "6584f4a9-a7f0-42f0-b929-ab5c99c0802d";

    // when, then
    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH + existingAdminId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.id", is(existingAdminId)))
        .andExpect(jsonPath("_embedded.username", is("cgenney5")))
        .andExpect(jsonPath("_embedded.firstname", is("Ceil")))
        .andExpect(jsonPath("_embedded.lastname", is("Genney")))
        .andExpect(jsonPath("_embedded.email", is("cgenney5@imageshack.us")));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void
      getTenantAdmins_Should_returnOkAndFilterByTenantId_When_attemptedToGetTenantWithTenantAdminAuthority()
          throws Exception {

    // when, then
    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH_WITHOUT_SLASH + "?tenantId=2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(1)))
        .andExpect(jsonPath("$.[0]._embedded.tenantId", is("2")))
        .andReturn();

    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH_WITHOUT_SLASH + "?tenantId=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(0)))
        .andReturn();
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.SINGLE_TENANT_ADMIN,
        AuthorityValue.USER_ADMIN,
        AuthorityValue.RESTRICTED_AGENCY_ADMIN
      })
  public void
      getTenantAdmins_Should_returnForbidden_When_attemptedToGetTenantAdminsWithNonSuperTenantAdminAuthority()
          throws Exception {

    // when, then
    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH_WITHOUT_SLASH + "?tenantId=2"))
        .andExpect(status().isForbidden());

    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH_WITHOUT_SLASH + "?tenantId=1"))
        .andExpect(status().isForbidden());

    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH_WITHOUT_SLASH + "?tenantId=0"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void
      searchTenantAdmin_Should_returnOk_When_attemptedToSearchTenantsWithTenantAdminAuthority()
          throws Exception {
    // when, then
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                get(
                    "/useradmin/tenantadmins/search?query=*&page=1&perPage=10&order=ASC&field=FIRSTNAME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_embedded", hasSize(PAGE_SIZE)))
            .andExpect(jsonPath("_embedded[0]._embedded.id").exists())
            .andExpect(jsonPath("_embedded[0]._embedded.username").exists())
            .andExpect(jsonPath("_embedded[0]._embedded.firstname").exists())
            .andExpect(jsonPath("_embedded[0]._embedded.lastname").exists())
            .andExpect(jsonPath("_embedded[0]._embedded.email").exists())
            .andReturn();

    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONArray embedded = JsonPath.read(contentAsString, "_embedded");

    assertAllElementsAreOfAdminType(embedded, AdminType.TENANT);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      searchAgencyAdmins_Should_returnOk_When_attemptedToSearchTenantsWithUserAdminAuthority()
          throws Exception {
    // when, then
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                get(
                    "/useradmin/agencyadmins/search?query=*&page=1&perPage=10&order=ASC&field=FIRSTNAME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_embedded", hasSize(PAGE_SIZE)))
            .andReturn();

    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONArray embedded = JsonPath.read(contentAsString, "_embedded");

    assertAllElementsAreOfAdminType(embedded, AdminType.AGENCY);
  }

  private void assertAllElementsAreOfAdminType(JSONArray embedded, AdminType adminType) {
    for (int i = 0; i < PAGE_SIZE; i++) {
      assertAllElementsAreOfAdminType(embedded, PAGE_SIZE, adminType);
    }
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void searchTenantAdmin_Should_returnCorrectResult_When_tenantIdIsProvided()
      throws Exception {
    final String tenantId = "102";
    final String expectedAdminId = "6584f4a9-a7f0-42f0-b929-ab5c99c0802d";
    final String expectedUsername = "cgenney5";
    final String expectedFirstname = "Ceil";
    final String expectedLastname = "Genney";
    final String expectedEmail = "cgenney5@imageshack.us";
    // when, then
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                get(
                    "/useradmin/tenantadmins/search?query="
                        + tenantId
                        + "&page=1&perPage=10&order=ASC&field=FIRSTNAME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_embedded", hasSize(1)))
            .andExpect(jsonPath("_embedded[0]._embedded.id").value(expectedAdminId))
            .andExpect(jsonPath("_embedded[0]._embedded.username").value(expectedUsername))
            .andExpect(jsonPath("_embedded[0]._embedded.firstname").value(expectedFirstname))
            .andExpect(jsonPath("_embedded[0]._embedded.lastname").value(expectedLastname))
            .andExpect(jsonPath("_embedded[0]._embedded.email").value(expectedEmail))
            .andReturn();

    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONArray embedded = JsonPath.read(contentAsString, "_embedded");

    assertAllElementsAreOfAdminType(embedded, 1, AdminType.TENANT);
  }

  private void assertAllElementsAreOfAdminType(
      JSONArray embedded, int pageSize, AdminType adminType) {
    for (int i = 0; i < pageSize; i++) {
      String tenantId = extractTenantWithOrderInList(embedded, i).get("id");
      assertThat(adminRepository.findByIdAndType(tenantId, adminType).isPresent()).isTrue();
    }
  }

  private static LinkedHashMap<String, String> extractTenantWithOrderInList(
      JSONArray embedded, int i) {
    return (LinkedHashMap<String, String>) ((LinkedHashMap) embedded.get(i)).get("_embedded");
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      getTenantAdmin_Should_returnForbidden_When_attemptedToGetTenantAdminWithoutTenantAdminAuthority()
          throws Exception {
    // given
    var existingAdminId = "6584f4a9-a7f0-42f0-b929-ab5c99c0802d";

    // when, then
    this.mockMvc
        .perform(get(TENANT_ADMIN_PATH + existingAdminId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      deleteTenantAdmin_Should_returnForbidden_When_attemptedToDeleteTenantAdminWithoutTenantAdminAuthority()
          throws Exception {
    // given
    var existingAdminId = "6584f4a9-a7f0-42f0-b929-ab5c99c0802d";

    // when, then
    this.mockMvc
        .perform(delete(TENANT_ADMIN_PATH + existingAdminId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void
      deleteTenantAdmin_Should_delete_When_attemptedToDeleteTenantAdminWithTenantAdminAuthority()
          throws Exception {
    // given
    var adminId = givenNewTenantAdminIsCreated();

    // when
    this.mockMvc.perform(delete(TENANT_ADMIN_PATH + adminId)).andExpect(status().isOk());

    // then
    this.mockMvc.perform(get(TENANT_ADMIN_PATH + adminId)).andExpect(status().isNoContent());
  }

  private String givenNewTenantAdminIsCreated() throws Exception {
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);
    createAdminDTO.setEmail("valid@email.com");

    MvcResult result =
        this.mockMvc
            .perform(
                post(TENANT_ADMIN_PATH)
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createAdminDTO)))
            .andExpect(status().isOk())
            .andReturn();
    String content = result.getResponse().getContentAsString();
    return JsonPath.read(content, "_embedded.id");
  }
}
