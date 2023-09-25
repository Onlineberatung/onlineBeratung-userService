package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageResponseDTO;
import de.caritas.cob.userservice.api.admin.facade.AdminUserFacade;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.config.VideoChatConfig;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.TopicServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@TestPropertySource(properties = "feature.topics.enabled=true")
class UserControllerConsultantE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ChatRepository chatRepository;

  @Autowired private ChatAgencyRepository chatAgencyRepository;

  @Autowired private UserAgencyRepository userAgencyRepository;

  @Autowired private VideoChatConfig videoChatConfig;

  @Autowired private IdentityConfig identityConfig;

  @Autowired private UsernameTranscoder usernameTranscoder;

  @MockBean
  @Qualifier("rocketChatRestTemplate")
  private RestTemplate rocketChatRestTemplate;

  @MockBean private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @SpyBean private AgencyService agencyService;

  @MockBean
  @Qualifier("topicControllerApiPrimary")
  private TopicControllerApi topicControllerApi;

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean private AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @MockBean private TopicServiceApiControllerFactory topicServiceApiControllerFactory;

  @MockBean private AgencyAdminService agencyAdminService;

  @MockBean private AdminUserFacade adminUserFacade;

  @MockBean private TenantService tenantService;

  private User user;
  private Consultant consultant;
  private Set<de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode> allLanguages =
      new HashSet<>();
  private Set<Consultant> consultantsToReset = new HashSet<>();
  private List<String> consultantIdsToDelete = new ArrayList<>();
  private List<ConsultantAgency> consultantAgencies = new ArrayList<>();
  private Chat chat;
  private ChatAgency chatAgency;
  private UserAgency userAgency;
  private String infix;

  @BeforeEach
  void setUp() {
    when(agencyServiceApiControllerFactory.createControllerApi())
        .thenReturn(
            new TestAgencyControllerApi(
                new de.caritas.cob.userservice.agencyserivce.generated.ApiClient()));

    when(topicServiceApiControllerFactory.createControllerApi()).thenReturn(topicControllerApi);
  }

  @AfterEach
  void reset() {
    if (nonNull(user)) {
      user.setDeleteDate(null);
      userRepository.save(user);
      user = null;
    }
    consultant = null;
    allLanguages = new HashSet<>();
    consultantsToReset.forEach(
        consultantToReset -> {
          consultantToReset.setLanguages(null);
          consultantToReset.setNotifyEnquiriesRepeating(true);
          consultantToReset.setNotifyNewChatMessageFromAdviceSeeker(true);
          consultantToReset.setNotifyNewFeedbackMessageFromAdviceSeeker(true);
          consultantRepository.save(consultantToReset);
        });
    consultantsToReset = new HashSet<>();
    consultantAgencyRepository.deleteAll(consultantAgencies);
    consultantIdsToDelete.forEach(id -> consultantRepository.deleteById(id));
    consultantIdsToDelete = new ArrayList<>();
    consultantAgencies = new ArrayList<>();
    if (nonNull(chat) && chatRepository.existsById(chat.getId())) {
      chatRepository.deleteById(chat.getId());
    }
    chat = null;
    if (nonNull(chatAgency) && chatAgencyRepository.existsById(chatAgency.getId())) {
      chatAgencyRepository.deleteById(chatAgency.getId());
    }
    chatAgency = null;
    if (nonNull(userAgency) && userAgencyRepository.existsById(userAgency.getId())) {
      userAgencyRepository.deleteById(userAgency.getId());
    }
    userAgency = null;
    videoChatConfig.setE2eEncryptionEnabled(false);
    identityConfig.setDisplayNameAllowedForConsultants(false);
    infix = null;
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfQueryIsNotGiven() throws Exception {
    mockMvc
        .perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfPageTooSmall() throws Exception {
    int pageNumber = -easyRandom.nextInt(3);

    mockMvc
        .perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", RandomStringUtils.randomAlphabetic(1))
                .param("page", String.valueOf(pageNumber)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfPerPageTooSmall() throws Exception {
    int perPage = -easyRandom.nextInt(3);

    mockMvc
        .perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", RandomStringUtils.randomAlphabetic(1))
                .param("perPage", String.valueOf(perPage)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfFieldIsNotInEnum() throws Exception {
    mockMvc
        .perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", RandomStringUtils.randomAlphabetic(1))
                .param("field", RandomStringUtils.randomAlphabetic(16)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfOrderIsNotInEnum() throws Exception {
    mockMvc
        .perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", RandomStringUtils.randomAlphabetic(1))
                .param("order", RandomStringUtils.randomAlphabetic(16)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondOkAndPayloadIfQueryIsGiven() throws Exception {
    givenAnInfix();
    var numMatching = easyRandom.nextInt(20) + 11;
    givenConsultantsMatching(numMatching, infix);
    givenAgencyServiceReturningAgencies();

    var pageUrlPrefix = "http://localhost/users/consultants/search?";
    var consultantUrlPrefix = "http://localhost/useradmin/consultants/";
    var response =
        mockMvc
            .perform(
                get("/users/consultants/search")
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .accept("application/hal+json")
                    .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("total", is(numMatching)))
            .andExpect(jsonPath("_embedded", hasSize(10)))
            .andExpect(jsonPath("_embedded[*]._embedded.id", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.firstname", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.lastname", containsString(infix)))
            .andExpect(jsonPath("_embedded[9]._embedded.lastname", containsString(infix)))
            .andExpect(jsonPath("_embedded[*]._embedded.username", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[9]._embedded.status", is("CREATED")))
            .andExpect(
                jsonPath("_embedded[0]._embedded.absenceMessage", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.absent", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.formalLanguage", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.teamConsultant", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.createDate", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.updateDate", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.email", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].city", not(contains(nullValue()))))
            .andExpect(
                jsonPath(
                    "_embedded[0]._embedded.agencies[0].description", not(contains(nullValue()))))
            .andExpect(
                jsonPath(
                    "_embedded[0]._embedded.agencies[0].teamAgency", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].offline", not(contains(nullValue()))))
            .andExpect(
                jsonPath(
                    "_embedded[0]._embedded.agencies[0].consultingType",
                    not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].city", not(contains(nullValue()))))
            .andExpect(
                jsonPath(
                    "_embedded[9]._embedded.agencies[0].description", not(contains(nullValue()))))
            .andExpect(
                jsonPath(
                    "_embedded[9]._embedded.agencies[0].teamAgency", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].offline", not(contains(nullValue()))))
            .andExpect(
                jsonPath(
                    "_embedded[9]._embedded.agencies[0].consultingType",
                    not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._links.self.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.self.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.self.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.update.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.update.method", is("PUT")))
            .andExpect(jsonPath("_embedded[0]._links.update.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.delete.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.delete.method", is("DELETE")))
            .andExpect(jsonPath("_embedded[0]._links.delete.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
            .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.self.method", is("GET")))
            .andExpect(jsonPath("_links.self.templated", is(false)))
            .andExpect(jsonPath("_links.next.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.next.method", is("GET")))
            .andExpect(jsonPath("_links.next.templated", is(false)))
            .andExpect(jsonPath("_links.previous", is(nullValue())))
            .andReturn()
            .getResponse();

    var searchResult =
        objectMapper.readValue(response.getContentAsString(), ConsultantSearchResultDTO.class);
    var foundConsultants = searchResult.getEmbedded();
    var previousFirstName = foundConsultants.get(0).getEmbedded().getFirstname();
    for (var foundConsultant : foundConsultants) {
      var currentFirstname = foundConsultant.getEmbedded().getFirstname();
      assertTrue(previousFirstName.compareTo(currentFirstname) <= 0);
      previousFirstName = currentFirstname;
    }
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondOkIfAllIsGiven() throws Exception {
    givenAnInfix();
    var numMatching = 26;
    givenConsultantsMatching(numMatching, infix);
    givenAgencyServiceReturningAgencies();

    var pageUrlPrefix = "http://localhost/users/consultants/search?";
    var consultantUrlPrefix = "http://localhost/useradmin/consultants/";
    var response =
        mockMvc
            .perform(
                get("/users/consultants/search")
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .accept("application/hal+json")
                    .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8))
                    .param("page", "3")
                    .param("perPage", "11")
                    .param("field", "LASTNAME")
                    .param("order", "DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("total", is(numMatching)))
            .andExpect(jsonPath("_embedded", hasSize(4)))
            .andExpect(jsonPath("_embedded[*]._embedded.id", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.firstname", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.lastname", containsString(infix)))
            .andExpect(jsonPath("_embedded[*]._embedded.username", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.email", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.agencies", hasSize(1)))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[1]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[1]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[1]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[2]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[2]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[2]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[3]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[3]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[3]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[1]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[2]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[3]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[0]._links.self.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.self.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.self.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.update.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.update.method", is("PUT")))
            .andExpect(jsonPath("_embedded[0]._links.update.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.delete.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.delete.method", is("DELETE")))
            .andExpect(jsonPath("_embedded[0]._links.delete.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
            .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.self.method", is("GET")))
            .andExpect(jsonPath("_links.self.templated", is(false)))
            .andExpect(jsonPath("_links.previous.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.previous.method", is("GET")))
            .andExpect(jsonPath("_links.previous.templated", is(false)))
            .andExpect(jsonPath("_links.next", is(nullValue())))
            .andReturn()
            .getResponse();

    var searchResult =
        objectMapper.readValue(response.getContentAsString(), ConsultantSearchResultDTO.class);
    var foundConsultants = searchResult.getEmbedded();

    var previousLastName = foundConsultants.get(0).getEmbedded().getLastname();
    for (var foundConsultant : foundConsultants) {
      var currentLastname = foundConsultant.getEmbedded().getLastname();
      assertTrue(previousLastName.compareTo(currentLastname) >= 0);
      previousLastName = currentLastname;
    }

    var agencyIdConsultantMap =
        consultantAgencies.stream()
            .collect(
                Collectors.toMap(ConsultantAgency::getAgencyId, ConsultantAgency::getConsultant));
    for (var foundConsultant : foundConsultants) {
      var embedded = foundConsultant.getEmbedded();
      var foundAgencyId = embedded.getAgencies().get(0).getId();
      assertEquals(agencyIdConsultantMap.get(foundAgencyId).getId(), embedded.getId());
    }
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN, AuthorityValue.RESTRICTED_AGENCY_ADMIN})
  void searchConsultantsShouldRespondOkAndFilterConsultantsByRestrictedAdminAgencies()
      throws Exception {
    when(authenticatedUser.hasRestrictedAgencyPriviliges()).thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn("d42c2e5e-143c-4db1-a90f-7cccf82fbb15");
    long agencyIdToSearchFor = 2L;
    when(adminUserFacade.findAdminUserAgencyIds(authenticatedUser.getUserId()))
        .thenReturn(Lists.newArrayList(agencyIdToSearchFor));
    givenAnInfix();

    var numMatching = 24;

    givenConsultantsMatching(numMatching, infix, Lists.newArrayList(agencyIdToSearchFor));

    var pageUrlPrefix = "http://localhost/users/consultants/search?";
    var consultantUrlPrefix = "http://localhost/useradmin/consultants/";
    var response =
        mockMvc
            .perform(
                get("/users/consultants/search")
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .accept("application/hal+json")
                    .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8))
                    .param("page", "3")
                    .param("perPage", "11")
                    .param("field", "LASTNAME")
                    .param("order", "DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("total", is(numMatching)))
            .andExpect(jsonPath("_embedded", hasSize(2)))
            .andExpect(jsonPath("_embedded[*]._embedded.id", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.firstname", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.lastname", containsString(infix)))
            .andExpect(jsonPath("_embedded[*]._embedded.username", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.email", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.isGroupchatConsultant", is(true)))
            .andExpect(jsonPath("_embedded[1]._embedded.isGroupchatConsultant", is(true)))
            .andExpect(jsonPath("_embedded[0]._embedded.agencies", hasSize(1)))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[1]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[1]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[1]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[1]._embedded.status", is("CREATED")))
            .andExpect(jsonPath("_embedded[0]._links.self.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.self.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.self.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.update.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.update.method", is("PUT")))
            .andExpect(jsonPath("_embedded[0]._links.update.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.delete.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.delete.method", is("DELETE")))
            .andExpect(jsonPath("_embedded[0]._links.delete.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
            .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.self.method", is("GET")))
            .andExpect(jsonPath("_links.self.templated", is(false)))
            .andExpect(jsonPath("_links.previous.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.previous.method", is("GET")))
            .andExpect(jsonPath("_links.previous.templated", is(false)))
            .andExpect(jsonPath("_links.next", is(nullValue())))
            .andReturn()
            .getResponse();

    var searchResult =
        objectMapper.readValue(response.getContentAsString(), ConsultantSearchResultDTO.class);
    var foundConsultants = searchResult.getEmbedded();

    for (var foundConsultant : foundConsultants) {
      var embedded = foundConsultant.getEmbedded();
      var agencies =
          embedded.getAgencies().stream()
              .map(agency -> agency.getId())
              .collect(Collectors.toList());
      assertTrue(agencies.contains(agencyIdToSearchFor));
    }
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondOkAndPayloadIfStarQueryIsGiven() throws Exception {
    givenAnInfix();
    givenConsultantsMatching(easyRandom.nextInt(20) + 11, infix);
    givenAgencyServiceReturningDummyAgencies();
    var numAll = (int) consultantRepository.countByDeleteDateIsNull();

    var pageUrlPrefix = "http://localhost/users/consultants/search?";
    var consultantUrlPrefix = "http://localhost/useradmin/consultants/";
    var response =
        mockMvc
            .perform(
                get("/users/consultants/search")
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .accept("application/hal+json")
                    .param("query", "*")
                    .param("perPage", String.valueOf(numAll) + 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("total", is(numAll)))
            .andExpect(jsonPath("_embedded", hasSize(numAll)))
            .andExpect(jsonPath("_embedded[*]._embedded.id", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.firstname", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.lastname", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[9]._embedded.lastname", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.username", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._embedded.status", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[9]._embedded.status", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[*]._embedded.email", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].id", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].name", not(contains(nullValue()))))
            .andExpect(
                jsonPath("_embedded[9]._embedded.agencies[0].postcode", not(contains(nullValue()))))
            .andExpect(jsonPath("_embedded[0]._links.self.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.self.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.self.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.update.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.update.method", is("PUT")))
            .andExpect(jsonPath("_embedded[0]._links.update.templated", is(false)))
            .andExpect(jsonPath("_embedded[0]._links.delete.href", startsWith(consultantUrlPrefix)))
            .andExpect(jsonPath("_embedded[0]._links.delete.method", is("DELETE")))
            .andExpect(jsonPath("_embedded[0]._links.delete.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
            .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
            .andExpect(
                jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
            .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
            .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
            .andExpect(jsonPath("_links.self.href", containsString("query=*")))
            .andExpect(jsonPath("_links.self.method", is("GET")))
            .andExpect(jsonPath("_links.self.templated", is(false)))
            .andExpect(jsonPath("_links.next", is(nullValue())))
            .andExpect(jsonPath("_links.previous", is(nullValue())))
            .andReturn()
            .getResponse();

    var searchResult =
        objectMapper.readValue(response.getContentAsString(), ConsultantSearchResultDTO.class);
    var foundConsultants = searchResult.getEmbedded();
    var previousFirstName = foundConsultants.get(0).getEmbedded().getFirstname();
    for (var foundConsultant : foundConsultants) {
      var currentFirstname = foundConsultant.getEmbedded().getFirstname();
      assertTrue(previousFirstName.compareTo(currentFirstname) <= 0);
      previousFirstName = currentFirstname;
    }
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldContainAgenciesMarkedForDeletionIfConsultantDeleted()
      throws Exception {
    givenAnInfix();
    givenConsultantsMatching(1, infix, true, true, Lists.newArrayList());
    givenAgencyServiceReturningDummyAgencies();
    var consultantsMarkedAsDeleted = consultantRepository.findAllByDeleteDateNotNull();
    assertEquals(1, consultantsMarkedAsDeleted.size());
    var onlyConsultant = consultantsMarkedAsDeleted.get(0);

    mockMvc
        .perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", infix)
                .param("perPage", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("_embedded", hasSize(1)))
        .andExpect(jsonPath("_embedded[0]._embedded.id", is(onlyConsultant.getId())))
        .andExpect(
            jsonPath("_embedded[0]._embedded.status", is(onlyConsultant.getStatus().toString())))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies", hasSize(1)))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.deleteDate", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.email", is(onlyConsultant.getEmail())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldContainOnlyAgenciesNotMarkedForDeletionIfConsultantNotDeleted()
      throws Exception {
    givenAnInfix();
    var numMatching = easyRandom.nextInt(20) + 1;
    givenConsultantsMatching(numMatching, infix, true, false, Lists.newArrayList());
    givenAgencyServiceReturningDummyAgencies();

    var response =
        mockMvc
            .perform(
                get("/users/consultants/search")
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .accept("application/hal+json")
                    .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("total", is(numMatching)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    var searchResult = objectMapper.readValue(response, ConsultantSearchResultDTO.class);
    var consultantAgenciesMarkedForDeletion =
        consultantAgencies.stream()
            .filter(consultantAgency -> nonNull(consultantAgency.getDeleteDate()))
            .map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toSet());
    var consultantAgenciesNotMarkedForDeletion =
        consultantAgencies.stream()
            .filter(consultantAgency -> isNull(consultantAgency.getDeleteDate()))
            .map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toSet());

    for (var foundConsultant : searchResult.getEmbedded()) {
      foundConsultant
          .getEmbedded()
          .getAgencies()
          .forEach(
              agency -> {
                var agencyId = agency.getId();
                assertFalse(consultantAgenciesMarkedForDeletion.contains(agencyId));
                assertTrue(consultantAgenciesNotMarkedForDeletion.contains(agencyId));
              });
    }
  }

  @Test
  @WithMockUser
  void getLanguagesShouldRespondWithBadRequestIfAgencyIdIsNotGiven() throws Exception {
    mockMvc
        .perform(
            get("/users/consultants/languages")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void getLanguagesShouldRespondWithDefaultLanguageAndOkWhenOnlyDefaultInDatabase()
      throws Exception {
    var agencyId = givenAnAgencyIdWithDefaultLanguageOnly();

    mockMvc
        .perform(
            get("/users/consultants/languages")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .param("agencyId", String.valueOf(agencyId))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("languages", hasSize(1)))
        .andExpect(jsonPath("languages[0]", is("de")));
  }

  @Test
  @WithMockUser
  void getLanguagesShouldRespondWithMultipleLanguageAndOkWhenMultipleLanguagesInDatabase()
      throws Exception {
    var agencyId = givenAnAgencyWithMultipleLanguages();

    var response =
        mockMvc
            .perform(
                get("/users/consultants/languages")
                    .cookie(CSRF_COOKIE)
                    .header(CSRF_HEADER, CSRF_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("agencyId", String.valueOf(agencyId))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("languages", hasSize(allLanguages.size())))
            .andReturn()
            .getResponse();

    var dto = objectMapper.readValue(response.getContentAsByteArray(), LanguageResponseDTO.class);
    assertEquals(allLanguages, new HashSet<>(dto.getLanguages()));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.VIEW_AGENCY_CONSULTANTS)
  void getConsultantsShouldRespondWithDisplayNameAndUsername() throws Exception {
    var agencyId = givenAnAgencyIdWithDefaultLanguageOnly();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse("user1", "user2", "user3");

    mockMvc
        .perform(
            get("/users/consultants")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .param("agencyId", String.valueOf(agencyId))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("[0].displayName", is("user1")))
        .andExpect(jsonPath("[0].username", startsWith("enc.")))
        .andExpect(jsonPath("[1].displayName", is("user2")))
        .andExpect(jsonPath("[1].username", startsWith("enc.")))
        .andExpect(jsonPath("[2].displayName", is("user3")))
        .andExpect(jsonPath("[2].username", startsWith("enc.")));
  }

  @Test
  @WithMockUser
  void getConsultantPublicDataShouldRespondWithOk() throws Exception {
    givenAConsultantWithMultipleAgencies();

    mockMvc
        .perform(
            get("/users/consultants/{consultantId}", consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("consultantId", is(consultant.getId())))
        .andExpect(jsonPath("firstName").doesNotExist())
        .andExpect(jsonPath("lastName").doesNotExist())
        .andExpect(jsonPath("agencies", hasSize(greaterThan(0))))
        .andExpect(jsonPath("agencies[0].id", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].name", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].postcode", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].city", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].description", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].teamAgency", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].offline", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].consultingType", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].topicIds", is(notNullValue())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void fetchSessionForConsultantShouldRespondWithConsultantSessionData() throws Exception {
    givenAConsultantWithAAdvisedSession(true);
    givenAValidTopicServiceResponse();

    mockMvc
        .perform(
            get("/users/consultants/sessions/{sessionId}", 1216L)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", is(1216)))
        .andExpect(jsonPath("agencyId", is(121)))
        .andExpect(jsonPath("consultingType", is(1)))
        .andExpect(jsonPath("status", is(1)))
        .andExpect(jsonPath("groupId", is("ix7E7HzXKTgGeQMyb")))
        .andExpect(jsonPath("feedbackGroupId", is("EQBcSwxn4eCAPYQ2J")))
        .andExpect(jsonPath("consultantId", is("473f7c4b-f011-4fc2-847c-ceb636a5b399")))
        .andExpect(jsonPath("consultantRcId", is("CztX9SWF4SJPvgknZ")))
        .andExpect(jsonPath("askerId", is("06c6601f-a5b4-4812-9260-20065390b1f5")))
        .andExpect(jsonPath("askerUserName", is("enc.OUZDK5DFON2DGNJVGU2Q....")))
        .andExpect(jsonPath("isTeamSession", is(true)))
        .andExpect(jsonPath("postcode", is("12345")))
        .andExpect(jsonPath("age", is(15)))
        .andExpect(jsonPath("gender", is("FEMALE")))
        .andExpect(jsonPath("counsellingRelation", is("SELF_COUNSELLING")))
        .andExpect(jsonPath("mainTopic").isMap())
        .andExpect(jsonPath("mainTopic.id", is(1)))
        .andExpect(jsonPath("mainTopic.name", is("topic name")))
        .andExpect(jsonPath("mainTopic.description", is("topic desc")))
        .andExpect(jsonPath("topics").isArray())
        .andExpect(jsonPath("topics", hasSize(2)))
        .andExpect(jsonPath("topics[0].id", is(1)))
        .andExpect(jsonPath("topics[0].name", is("topic name")))
        .andExpect(jsonPath("topics[0].description", is("topic desc")))
        .andExpect(jsonPath("topics[1].id", is(2)))
        .andExpect(jsonPath("topics[1].name", is("topic name 2")))
        .andExpect(jsonPath("topics[1].description", is("topic desc 2")));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void fetchSessionForConsultantShouldRespondNotFoundWhenSessionIsNotValid() throws Exception {
    givenAConsultantWithAAdvisedSession(true);

    mockMvc
        .perform(
            get("/users/consultants/sessions/{sessionId}", 9999)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void
      fetchSessionForConsultantShouldRespondForbiddenWhenSessionIsInAdviceAndTeamSessionNotInAgency()
          throws Exception {
    givenAConsultantWithAAdvisedSession(true);

    mockMvc
        .perform(
            get("/users/consultants/sessions/{sessionId}", 2)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @SuppressWarnings("unchecked,SameParameterValue")
  private void givenAValidRocketChatInfoUserResponse(
      String username1, String username2, String username3) {
    var urlInfix = "/api/v1/users.info?userId=";
    when(rocketChatRestTemplate.exchange(
            contains(urlInfix), eq(HttpMethod.GET),
            any(HttpEntity.class), eq(UserInfoResponseDTO.class)))
        .thenReturn(
            ResponseEntity.ok(userInfoResponseDTO(username1)),
            ResponseEntity.ok(userInfoResponseDTO(username2)),
            ResponseEntity.ok(userInfoResponseDTO(username3)));
  }

  @NonNull
  private UserInfoResponseDTO userInfoResponseDTO(String clearTextUsername) {
    var userInfoResponse = new UserInfoResponseDTO();
    userInfoResponse.setSuccess(true);

    var chatUser = easyRandom.nextObject(RocketChatUserDTO.class);
    chatUser.setId(RandomStringUtils.randomAlphanumeric(17));
    chatUser.setUsername(RandomStringUtils.randomAlphabetic(16));
    chatUser.setName(usernameTranscoder.encodeUsername(clearTextUsername));
    userInfoResponse.setUser(chatUser);
    return userInfoResponse;
  }

  private void givenAValidRocketChatSystemUser() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getSystemUserSneaky()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rocketChatCredentialsProvider.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
  }

  private long aPositiveLong() {
    return Math.abs(easyRandom.nextLong());
  }

  private void givenAnInfix() {
    infix =
        RandomStringUtils.randomAlphanumeric(7)
            + (easyRandom.nextBoolean() ? "ä" : "Ö")
            + RandomStringUtils.randomAlphanumeric(7);
  }

  private void givenConsultantsMatching(@PositiveOrZero int count, @NotBlank String infix) {
    givenConsultantsMatching(count, infix, false, false, Lists.newArrayList());
  }

  private void givenConsultantsMatching(
      @PositiveOrZero int count, @NotBlank String infix, List<Long> agencyIds) {
    givenConsultantsMatching(count, infix, false, false, agencyIds);
  }

  private void givenConsultantsMatching(
      @PositiveOrZero int count,
      @NotBlank String infix,
      boolean includingAgenciesMarkedAsDeleted,
      boolean markedAsDeleted,
      List<Long> agenciesIdsToAssign) {
    List<Consultant> savedConsultants = Lists.newArrayList();
    while (count-- > 0) {
      var dbConsultant = consultantRepository.findAll().iterator().next();
      var consultant = new Consultant();
      BeanUtils.copyProperties(dbConsultant, consultant);
      consultant.setId(UUID.randomUUID().toString());
      consultant.setUsername(RandomStringUtils.randomAlphabetic(8));
      consultant.setRocketChatId(RandomStringUtils.randomAlphabetic(8));
      consultant.setFirstName(aStringWithoutInfix(infix));
      consultant.setLastName(aStringWithInfix(infix));
      consultant.setEmail(aValidEmailWithoutInfix(infix));
      if (markedAsDeleted) {
        consultant.setStatus(ConsultantStatus.IN_DELETION);
        consultant.setDeleteDate(LocalDateTime.now());
      } else {
        consultant.setStatus(ConsultantStatus.CREATED);
      }
      consultant.setAbsenceMessage(RandomStringUtils.randomAlphabetic(8));
      consultant.setAbsent(easyRandom.nextBoolean());
      consultant.setLanguageFormal(easyRandom.nextBoolean());
      consultant.setTeamConsultant(easyRandom.nextBoolean());

      consultantRepository.save(consultant);
      consultantIdsToDelete.add(consultant.getId());

      ConsultantAgency consultantAgency;

      if (!agenciesIdsToAssign.isEmpty()) {
        var index = easyRandom.nextInt(agenciesIdsToAssign.size());
        consultantAgency =
            ConsultantAgency.builder()
                .consultant(consultant)
                .agencyId(agenciesIdsToAssign.get(index))
                .build();
      } else {
        consultantAgency =
            ConsultantAgency.builder().consultant(consultant).agencyId(aPositiveLong()).build();
      }

      if (includingAgenciesMarkedAsDeleted) {
        var deleteDate = easyRandom.nextBoolean() ? null : LocalDateTime.now();
        consultantAgency.setDeleteDate(deleteDate);
      }
      consultantAgencyRepository.save(consultantAgency);
      consultantAgencies.add(consultantAgency);
      consultant.setConsultantAgencies(Set.of(consultantAgency));
      var saved = consultantRepository.save(consultant);
      savedConsultants.add(saved);
    }
  }

  private String aValidEmail() {
    return RandomStringUtils.randomAlphabetic(8)
        + "@"
        + RandomStringUtils.randomAlphabetic(8)
        + "."
        + (easyRandom.nextBoolean() ? "de" : "com");
  }

  private String aValidEmailWithoutInfix(String infix) {
    var email = infix;
    while (email.contains(infix)) {
      email = aValidEmail();
    }

    return email;
  }

  private String aStringWithoutInfix(String infix) {
    var str = infix;
    while (str.contains(infix)) {
      str = RandomStringUtils.randomAlphanumeric(8);
    }

    return str;
  }

  private String aStringWithInfix(String infix) {
    return RandomStringUtils.randomAlphabetic(4) + infix + RandomStringUtils.randomAlphabetic(4);
  }

  private void givenAgencyServiceReturningAgencies() {
    var agencies = new ArrayList<AgencyDTO>();
    consultantAgencies.forEach(
        consultantAgency -> {
          var agency = new AgencyDTO();
          agency.setId(consultantAgency.getAgencyId());
          agency.setName(RandomStringUtils.randomAlphabetic(16));
          agency.setPostcode(RandomStringUtils.randomNumeric(5));
          agency.setCity(RandomStringUtils.randomNumeric(8));
          agency.setDescription(RandomStringUtils.randomNumeric(8));
          agency.setTeamAgency(easyRandom.nextBoolean());
          agency.setOffline(easyRandom.nextBoolean());
          agency.setConsultingType(easyRandom.nextInt());
          agencies.add(agency);
        });

    when(agencyService.getAgenciesWithoutCaching(anyList())).thenReturn(agencies);
  }

  private void givenAgencyServiceReturningDummyAgencies() {
    var agencies = new ArrayList<AgencyDTO>();

    when(agencyService.getAgenciesWithoutCaching(anyList()))
        .thenAnswer(
            i -> {
              List<Long> agencyIds = i.getArgument(0);
              agencyIds.forEach(
                  agencyId -> {
                    var agency = new AgencyDTO();
                    agency.setId(agencyId);
                    agency.setName(RandomStringUtils.randomAlphabetic(16));
                    agency.setPostcode(RandomStringUtils.randomNumeric(5));
                    agencies.add(agency);
                  });
              return agencies;
            });
  }

  private void givenAConsultantWithMultipleAgencies() {
    consultant =
        consultantRepository.findById("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe").orElseThrow();
  }

  private void givenAConsultantWithAAdvisedSession(boolean isAuthUser) {
    consultant =
        consultantRepository.findById("473f7c4b-f011-4fc2-847c-ceb636a5b399").orElseThrow();
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
      when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
      when(authenticatedUser.isConsultant()).thenReturn(true);
      when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
      when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.CONSULTANT.getValue()));
      when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
    }
  }

  private long givenAnAgencyIdWithDefaultLanguageOnly() {
    return 121;
  }

  private long givenAnAgencyWithMultipleLanguages() {
    var agencyId = 0L;

    consultantAgencyRepository
        .findByAgencyIdAndDeleteDateIsNull(agencyId)
        .forEach(
            consultantAgency -> {
              var consultant = consultantAgency.getConsultant();
              var language1 = new Language(consultant, aLanguageCode());
              var language2 = new Language(consultant, aLanguageCode());
              allLanguages.add(mapLanguageCode(language1));
              allLanguages.add(mapLanguageCode(language2));
              var languages = Set.of(language1, language2);
              consultant.setLanguages(languages);
              consultantRepository.save(consultant);

              consultantsToReset.add(consultant);
            });

    return agencyId;
  }

  private LanguageCode aLanguageCode() {
    LanguageCode languageCode = null;
    while (isNull(languageCode) || languageCode.equals(LanguageCode.undefined)) {
      languageCode = easyRandom.nextObject(LanguageCode.class);
    }
    return languageCode;
  }

  private de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode mapLanguageCode(
      Language language) {
    return de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode.fromValue(
        language.getLanguageCode().name());
  }

  private void givenAValidTopicServiceResponse() {
    var firstTopic =
        new de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO()
            .id(1L)
            .name("topic name")
            .description("topic desc")
            .status("INACTIVE")
            .internalIdentifier("internal identifier 1");
    var secondTopic =
        new de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO()
            .id(2L)
            .name("topic name 2")
            .description("topic desc 2")
            .status("ACTIVE")
            .internalIdentifier("internal identifier 2");

    when(topicControllerApi.getApiClient())
        .thenReturn(new de.caritas.cob.userservice.topicservice.generated.ApiClient());
    when(topicControllerApi.getAllTopics()).thenReturn(Lists.newArrayList(firstTopic, secondTopic));
    when(topicControllerApi.getAllActiveTopics())
        .thenReturn(Lists.newArrayList(firstTopic, secondTopic));
  }
}
