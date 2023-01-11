package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.TENANT_ADMIN_PATH;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.ConsultingTypeServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.MailServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.TopicServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
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
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@TestPropertySource(properties = "feature.topics.enabled=true")
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

  @MockBean private TopicServiceApiControllerFactory topicServiceApiControllerFactory;

  @MockBean
  @Qualifier("mailsControllerApi")
  private MailsControllerApi mailsControllerApi;

  @MockBean AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @MockBean private Keycloak keycloak;

  @MockBean IdentityClient identityClient;

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
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void updateTenantAdmin_Should_returnOk_When_updateAttemptAsTenantAdmin() throws Exception {
    // given
    String adminId = givenNewTenantAdminIsCreated();

    UpdateTenantAdminDTO updateAdminDTO = new EasyRandom().nextObject(UpdateTenantAdminDTO.class);

    updateAdminDTO.setFirstname("changedFirstname");
    updateAdminDTO.setLastname("changedLastname");
    updateAdminDTO.setEmail("changed@email.com");
    updateAdminDTO.setTenantId(1);

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
  @WithMockUser(authorities = {AuthorityValue.TENANT_ADMIN})
  public void getTenantAdmin_Should_returnOk_When_attemptedToGetTenantWithTenantAdminAuthority()
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
  public void searchTenantAdmin_Should_returnOk_When_attemptedToGetTenantWithTenantAdminAuthority()
      throws Exception {
    // when, then
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                get(
                    "/useradmin/tenantadmins/search?query=*&page=1&perPage=10&order=ASC&field=FIRSTNAME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_embedded", hasSize(PAGE_SIZE)))
            .andReturn();

    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONArray embedded = JsonPath.read(contentAsString, "_embedded");

    assertAllElementsAreOfAdminTypeTenant(embedded);
  }

  private void assertAllElementsAreOfAdminTypeTenant(JSONArray embedded) {
    for (int i = 0; i < PAGE_SIZE; i++) {
      String tenantId = extractTenantWithOrderInList(embedded, i).get("id");
      assertThat(adminRepository.findByIdAndType(tenantId, Admin.AdminType.TENANT).isPresent())
          .isTrue();
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
