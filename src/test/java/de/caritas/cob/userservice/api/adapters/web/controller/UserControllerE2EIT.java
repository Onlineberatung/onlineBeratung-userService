package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID_HEADER_PARAMETER_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.actions.chat.StopChatActionCommand;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.StandardResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.MessageResponse;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomResponse;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UpdateUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.E2eKeyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.OneTimePasswordDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PasswordDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.config.VideoChatConfig;
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
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.OtpType;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Success;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
public class UserControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);
  private static final Cookie RC_TOKEN_COOKIE = new Cookie(
      "rc_token", RandomStringUtils.randomAlphanumeric(43)
  );

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UsernameTranscoder usernameTranscoder;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private ChatRepository chatRepository;

  @Autowired
  private ChatAgencyRepository chatAgencyRepository;

  @Autowired
  private UserAgencyRepository userAgencyRepository;

  @Autowired
  private de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi consultingTypeControllerApi;

  @Autowired
  private VideoChatConfig videoChatConfig;

  @Autowired
  private IdentityConfig identityConfig;

  @MockBean
  private AuthenticatedUser authenticatedUser;

  @MockBean
  private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @SpyBean
  private AgencyService agencyService;

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
  private Keycloak keycloak;

  @MockBean
  @SuppressWarnings("unused")
  private StopChatActionCommand stopChatActionCommand;

  @Captor
  private ArgumentCaptor<HttpEntity<OtpSetupDTO>> otpSetupCaptor;

  @Captor
  private ArgumentCaptor<HttpEntity<UpdateUser>> updateUserCaptor;

  private User user;

  private Consultant consultant;

  private Session session;

  private UpdateConsultantDTO updateConsultantDTO;

  private EnquiryMessageDTO enquiryMessageDTO;

  private Set<de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode> allLanguages = new HashSet<>();

  private Set<Consultant> consultantsToReset = new HashSet<>();

  private List<String> consultantIdsToDelete = new ArrayList<>();

  private List<ConsultantAgency> consultantAgencies = new ArrayList<>();

  private OneTimePasswordDTO oneTimePasswordDTO;

  private E2eKeyDTO e2eKeyDTO;

  private EmailDTO emailDTO;

  private String tan;

  private String email;

  private PatchUserDTO patchUserDTO;

  private UserDTO userDTO;

  private Chat chat;

  private PasswordDTO passwordDto;

  private DeleteUserAccountDTO deleteUserAccountDto;

  private UserInfoResponseDTO userInfoResponse;

  @SuppressWarnings("FieldCanBeLocal")
  private SubscriptionsGetDTO subscriptionsGetResponse;

  private String infix;

  @AfterEach
  public void reset() {
    if (nonNull(user)) {
      user.setDeleteDate(null);
      userRepository.save(user);
      user = null;
    }
    session = null;
    consultant = null;
    updateConsultantDTO = null;
    enquiryMessageDTO = null;
    allLanguages = new HashSet<>();
    consultantsToReset.forEach(consultantToReset -> {
      consultantToReset.setLanguages(null);
      consultantRepository.save(consultantToReset);
    });
    consultantsToReset = new HashSet<>();
    consultantAgencyRepository.deleteAll(consultantAgencies);
    consultantIdsToDelete.forEach(id -> consultantRepository.deleteById(id));
    consultantIdsToDelete = new ArrayList<>();
    consultantAgencies = new ArrayList<>();
    oneTimePasswordDTO = null;
    emailDTO = null;
    tan = null;
    email = null;
    patchUserDTO = null;
    userDTO = null;
    if (nonNull(chat)) {
      chatRepository.deleteById(chat.getId());
      chat = null;
    }
    videoChatConfig.setE2eEncryptionEnabled(false);
    passwordDto = null;
    deleteUserAccountDto = null;
    userInfoResponse = null;
    subscriptionsGetResponse = null;
    identityConfig.setDisplayNameAllowedForConsultants(false);
    infix = null;
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void createEnquiryMessageWithLanguageShouldSaveLanguageAndRespondWithCreated()
      throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatTechUserResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(true);

    mockMvc.perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")));

    var savedSession = sessionRepository.findById(session.getId());
    assertTrue(savedSession.isPresent());
    assertEquals(
        LanguageCode.getByCode(enquiryMessageDTO.getLanguage().getValue()),
        savedSession.get().getLanguageCode()
    );

    restoreSession();
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void createEnquiryMessageWithoutLanguageShouldSaveDefaultLanguageAndRespondWithCreated()
      throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatTechUserResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(false);

    mockMvc.perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")));

    var savedSession = sessionRepository.findById(session.getId());
    assertTrue(savedSession.isPresent());
    assertEquals(LanguageCode.de, savedSession.get().getLanguageCode());

    restoreSession();
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getSessionsForGroupOrFeedbackGroupIdsShouldFindSessionsByGroupOrFeedbackGroup()
      throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc.perform(
            get("/users/sessions/room?rcGroupIds=mzAdWzQEobJ2PkoxP,9faSTWZ5gurHLXy4R")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.feedbackGroupId", is("9faSTWZ5gurHLXy4R")))
        .andExpect(jsonPath("sessions[1].session.groupId", is("mzAdWzQEobJ2PkoxP")))
        .andExpect(jsonPath("sessions", hasSize(2)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getSessionsForGroupOrFeedbackGroupIdsShouldFindSessionsByGroupOrFeedbackGroupForConsultant()
      throws Exception {
    givenAConsultantWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc.perform(
            get("/users/sessions/room?rcGroupIds=YWKxhFX5K2HPpsFbs,4SPkApB8So88c7tQ3")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("YWKxhFX5K2HPpsFbs")))
        .andExpect(jsonPath("sessions[1].session.feedbackGroupId", is("4SPkApB8So88c7tQ3")))
        .andExpect(jsonPath("sessions", hasSize(2)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getSessionsForGroupOrFeedbackGroupIdsShouldFindSessionByGroupId()
      throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc.perform(
            get("/users/sessions/room?rcGroupIds=mzAdWzQEobJ2PkoxP")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("mzAdWzQEobJ2PkoxP")))
        .andExpect(jsonPath("sessions", hasSize(1)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getSessionsForGroupOrFeedbackGroupIdsShouldBeForbiddenIfUserDoesNotParticipateInSession()
      throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc.perform(
            get("/users/sessions/room?rcGroupIds=4SPkApB8So88c7tQ3")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getSessionsForGroupOrFeedbackGroupIdsShouldBeForbiddenIfConsultantDoesNotParticipateInSession()
      throws Exception {
    givenAConsultantWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc.perform(
            get("/users/sessions/room?rcGroupIds=QBv2xym9qQ2DoAxkR")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getSessionsForGroupOrFeedbackGroupIdsShouldBeNoContentIfNoSessionsFoundFourIds()
      throws Exception {
    givenAConsultantWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc.perform(
            get("/users/sessions/room?rcGroupIds=doesNotExist")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfQueryIsNotGiven() throws Exception {
    mockMvc.perform(
        get("/users/consultants/search")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept("application/hal+json")
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfPageTooSmall() throws Exception {
    int pageNumber = -easyRandom.nextInt(3);

    mockMvc.perform(
        get("/users/consultants/search")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept("application/hal+json")
            .param("query", RandomStringUtils.randomAlphabetic(1))
            .param("page", String.valueOf(pageNumber))
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfPerPageTooSmall() throws Exception {
    int perPage = -easyRandom.nextInt(3);

    mockMvc.perform(
        get("/users/consultants/search")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept("application/hal+json")
            .param("query", RandomStringUtils.randomAlphabetic(1))
            .param("perPage", String.valueOf(perPage))
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfFieldIsNotInEnum() throws Exception {
    mockMvc.perform(
        get("/users/consultants/search")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept("application/hal+json")
            .param("query", RandomStringUtils.randomAlphabetic(1))
            .param("field", RandomStringUtils.randomAlphabetic(16))
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_ADMIN)
  void searchConsultantsShouldRespondWithBadRequestIfOrderIsNotInEnum() throws Exception {
    mockMvc.perform(
        get("/users/consultants/search")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept("application/hal+json")
            .param("query", RandomStringUtils.randomAlphabetic(1))
            .param("order", RandomStringUtils.randomAlphabetic(16))
    ).andExpect(status().isBadRequest());
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
    var response = mockMvc.perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8))
        )
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
        .andExpect(jsonPath("_embedded[0]._embedded.absenceMessage", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.absent", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.formalLanguage", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.teamConsultant", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.createDate", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.updateDate", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[*]._embedded.email", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].city", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[0]._embedded.agencies[0].description", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[0]._embedded.agencies[0].teamAgency", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[0]._embedded.agencies[0].offline", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].consultingType",
            not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[9]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[9]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[9]._embedded.agencies[0].postcode", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[9]._embedded.agencies[0].city", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[9]._embedded.agencies[0].description", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[9]._embedded.agencies[0].teamAgency", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[9]._embedded.agencies[0].offline", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[9]._embedded.agencies[0].consultingType",
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
        .andExpect(jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
        .andExpect(jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
        .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
        .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
        .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
        .andExpect(jsonPath("_links.self.method", is("GET")))
        .andExpect(jsonPath("_links.self.templated", is(false)))
        .andExpect(jsonPath("_links.next.href", startsWith(pageUrlPrefix)))
        .andExpect(jsonPath("_links.next.method", is("GET")))
        .andExpect(jsonPath("_links.next.templated", is(false)))
        .andExpect(jsonPath("_links.previous", is(nullValue())))
        .andReturn().getResponse();

    var searchResult = objectMapper.readValue(response.getContentAsString(),
        ConsultantSearchResultDTO.class);
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
    var response = mockMvc.perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8))
                .param("page", "3")
                .param("perPage", "11")
                .param("field", "LASTNAME")
                .param("order", "DESC")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("total", is(numMatching)))
        .andExpect(jsonPath("_embedded", hasSize(4)))
        .andExpect(jsonPath("_embedded[*]._embedded.id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[*]._embedded.firstname", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.lastname", containsString(infix)))
        .andExpect(jsonPath("_embedded[*]._embedded.username", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[*]._embedded.email", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies", hasSize(1)))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[1]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[1]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[1]._embedded.agencies[0].postcode", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[2]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[2]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[2]._embedded.agencies[0].postcode", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[3]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[3]._embedded.agencies[0].name", not(contains(nullValue()))))
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
        .andExpect(jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
        .andExpect(jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
        .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
        .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
        .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
        .andExpect(jsonPath("_links.self.method", is("GET")))
        .andExpect(jsonPath("_links.self.templated", is(false)))
        .andExpect(jsonPath("_links.previous.href", startsWith(pageUrlPrefix)))
        .andExpect(jsonPath("_links.previous.method", is("GET")))
        .andExpect(jsonPath("_links.previous.templated", is(false)))
        .andExpect(jsonPath("_links.next", is(nullValue())))
        .andReturn().getResponse();

    var searchResult = objectMapper.readValue(response.getContentAsString(),
        ConsultantSearchResultDTO.class);
    var foundConsultants = searchResult.getEmbedded();

    var previousLastName = foundConsultants.get(0).getEmbedded().getLastname();
    for (var foundConsultant : foundConsultants) {
      var currentLastname = foundConsultant.getEmbedded().getLastname();
      assertTrue(previousLastName.compareTo(currentLastname) >= 0);
      previousLastName = currentLastname;
    }

    var agencyIdConsultantMap = consultantAgencies.stream()
        .collect(Collectors.toMap(ConsultantAgency::getAgencyId, ConsultantAgency::getConsultant));
    for (var foundConsultant : foundConsultants) {
      var embedded = foundConsultant.getEmbedded();
      var foundAgencyId = embedded.getAgencies().get(0).getId();
      assertEquals(agencyIdConsultantMap.get(foundAgencyId).getId(), embedded.getId());
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
    var response = mockMvc.perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", "*")
                .param("perPage", String.valueOf(numAll) + 1)
        )
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
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[0]._embedded.agencies[0].name", not(contains(nullValue()))))
        .andExpect(
            jsonPath("_embedded[0]._embedded.agencies[0].postcode", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[9]._embedded.agencies[0].id", not(contains(nullValue()))))
        .andExpect(jsonPath("_embedded[9]._embedded.agencies[0].name", not(contains(nullValue()))))
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
        .andExpect(jsonPath("_embedded[0]._links.agencies.href", startsWith(consultantUrlPrefix)))
        .andExpect(jsonPath("_embedded[0]._links.agencies.href", Matchers.endsWith("/agencies")))
        .andExpect(jsonPath("_embedded[0]._links.agencies.method", is("GET")))
        .andExpect(jsonPath("_embedded[0]._links.agencies.templated", is(false)))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.href", startsWith(consultantUrlPrefix)))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.href", Matchers.endsWith("/agencies")))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.method", is("POST")))
        .andExpect(jsonPath("_embedded[0]._links.addAgency.templated", is(false)))
        .andExpect(jsonPath("_links.self.href", startsWith(pageUrlPrefix)))
        .andExpect(jsonPath("_links.self.href", containsString("query=*")))
        .andExpect(jsonPath("_links.self.method", is("GET")))
        .andExpect(jsonPath("_links.self.templated", is(false)))
        .andExpect(jsonPath("_links.next", is(nullValue())))
        .andExpect(jsonPath("_links.previous", is(nullValue())))
        .andReturn().getResponse();

    var searchResult = objectMapper.readValue(response.getContentAsString(),
        ConsultantSearchResultDTO.class);
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
    givenConsultantsMatching(1, infix, true, true);
    givenAgencyServiceReturningDummyAgencies();
    var consultantsMarkedAsDeleted = consultantRepository.findAllByDeleteDateNotNull();
    assertEquals(1, consultantsMarkedAsDeleted.size());
    var onlyConsultant = consultantsMarkedAsDeleted.get(0);

    mockMvc.perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", infix)
                .param("perPage", "1")
        )
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
    givenConsultantsMatching(numMatching, infix, true, false);
    givenAgencyServiceReturningDummyAgencies();

    var response = mockMvc.perform(
            get("/users/consultants/search")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept("application/hal+json")
                .param("query", URLEncoder.encode(infix, StandardCharsets.UTF_8))
        ).andExpect(status().isOk())
        .andExpect(jsonPath("total", is(numMatching)))
        .andReturn().getResponse().getContentAsString();

    var searchResult = objectMapper.readValue(response, ConsultantSearchResultDTO.class);
    var consultantAgenciesMarkedForDeletion = consultantAgencies.stream()
        .filter(consultantAgency -> nonNull(consultantAgency.getDeleteDate()))
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toSet());
    var consultantAgenciesNotMarkedForDeletion = consultantAgencies.stream()
        .filter(consultantAgency -> isNull(consultantAgency.getDeleteDate()))
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toSet());

    for (var foundConsultant : searchResult.getEmbedded()) {
      foundConsultant.getEmbedded().getAgencies().forEach(agency -> {
        var agencyId = agency.getId();
        assertFalse(consultantAgenciesMarkedForDeletion.contains(agencyId));
        assertTrue(consultantAgenciesNotMarkedForDeletion.contains(agencyId));
      });
    }
  }

  @Test
  @WithMockUser
  public void getLanguagesShouldRespondWithBadRequestIfAgencyIdIsNotGiven() throws Exception {
    mockMvc.perform(
            get("/users/consultants/languages")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void getLanguagesShouldRespondWithDefaultLanguageAndOkWhenOnlyDefaultInDatabase()
      throws Exception {
    var agencyId = givenAnAgencyIdWithDefaultLanguageOnly();

    mockMvc.perform(
            get("/users/consultants/languages")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .param("agencyId", String.valueOf(agencyId))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("languages", hasSize(1)))
        .andExpect(jsonPath("languages[0]", is("de")));
  }

  @Test
  @WithMockUser
  public void getLanguagesShouldRespondWithMultipleLanguageAndOkWhenMultipleLanguagesInDatabase()
      throws Exception {
    var agencyId = givenAnAgencyWithMultipleLanguages();

    var response = mockMvc.perform(
            get("/users/consultants/languages")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .param("agencyId", String.valueOf(agencyId))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("languages", hasSize(allLanguages.size())))
        .andReturn().getResponse();

    var dto = objectMapper.readValue(response.getContentAsByteArray(), LanguageResponseDTO.class);
    assertEquals(allLanguages, new HashSet<>(dto.getLanguages()));
  }

  @Test
  @WithMockUser
  public void getConsultantPublicDataShouldRespondWithOk() throws Exception {
    givenAConsultantWithMultipleAgencies();

    mockMvc.perform(
            get("/users/consultants/{consultantId}", consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("consultantId", is(consultant.getId())))
        .andExpect(jsonPath("firstName").doesNotExist())
        .andExpect(jsonPath("lastName").doesNotExist())
        .andExpect(jsonPath("agencies", hasSize(24)))
        .andExpect(jsonPath("agencies[0].id", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].name", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].postcode", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].city", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].description", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].teamAgency", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].offline", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].consultingType", is(notNullValue())));

    assertEquals(24, consultant.getConsultantAgencies().size());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faByAppIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant(true);
    givenKeycloakRespondsOtpByAppHasBeenSetup(consultant.getUsername());
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();

    var consultantAgency = consultant.getConsultantAgencies().iterator().next();
    var displayName = usernameTranscoder.decodeUsername(userInfoResponse.getUser().getName());
    var username = usernameTranscoder.decodeUsername(consultant.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(consultant.getId())))
        .andExpect(jsonPath("userName", is(username)))
        .andExpect(jsonPath("displayName", is(displayName)))
        .andExpect(jsonPath("isDisplayNameEditable", is(false)))
        .andExpect(jsonPath("firstName", is(consultant.getFirstName())))
        .andExpect(jsonPath("lastName", is(consultant.getLastName())))
        .andExpect(jsonPath("email", is(consultant.getEmail())))
        .andExpect(jsonPath("languages", is(notNullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", hasSize(1)))
        .andExpect(jsonPath("agencies[0].id", is(consultantAgency.getAgencyId().intValue())))
        .andExpect(jsonPath("agencies[0].name", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].postcode", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].city", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].description", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].teamAgency", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].offline", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].consultingType", is(notNullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("consultant")))
        .andExpect(jsonPath("grantedAuthorities", hasSize(1)))
        .andExpect(jsonPath("grantedAuthorities[0]", is("anAuthority")))
        .andExpect(jsonPath("consultingTypes", is(nullValue())))
        .andExpect(jsonPath("hasAnonymousConversations", is(true)))
        .andExpect(jsonPath("hasArchive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isEnabled", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isActive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.secret", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.qrCode", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.type", is("APP")))
        .andExpect(jsonPath("twoFactorAuth.isToEncourage", is(consultant.getEncourage2fa())))
        .andExpect(jsonPath("absent", is(consultant.isAbsent())))
        .andExpect(jsonPath("formalLanguage", is(consultant.isLanguageFormal())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faByAppIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidUser(true);
    givenConsultingTypeServiceResponse();
    givenKeycloakRespondsOtpByAppHasBeenSetup(user.getUsername());

    var username = usernameTranscoder.decodeUsername(user.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(user.getUserId())))
        .andExpect(jsonPath("userName", is(username)))
        .andExpect(jsonPath("displayName", is(nullValue())))
        .andExpect(jsonPath("isDisplayNameEditable", is(false)))
        .andExpect(jsonPath("firstName", is(nullValue())))
        .andExpect(jsonPath("lastName", is(nullValue())))
        .andExpect(jsonPath("email", is(nullValue())))
        .andExpect(jsonPath("languages", is(nullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", is(nullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("user")))
        .andExpect(jsonPath("grantedAuthorities", hasSize(1)))
        .andExpect(jsonPath("grantedAuthorities[0]", is("anotherAuthority")))
        .andExpect(jsonPath("consultingTypes", is(notNullValue())))
        .andExpect(jsonPath("hasAnonymousConversations", is(false)))
        .andExpect(jsonPath("hasArchive", is(false)))
        .andExpect(jsonPath("twoFactorAuth.isEnabled", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isActive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.secret", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.qrCode", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.type", is("APP")))
        .andExpect(jsonPath("twoFactorAuth.isToEncourage", is(user.getEncourage2fa())))
        .andExpect(jsonPath("absent", is(false)))
        .andExpect(jsonPath("formalLanguage", is(user.isLanguageFormal())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faByEmailIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant(true);
    givenKeycloakRespondsOtpByEmailHasBeenSetup(consultant.getUsername());
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();

    var consultantAgency = consultant.getConsultantAgencies().iterator().next();
    var displayName = usernameTranscoder.decodeUsername(userInfoResponse.getUser().getName());
    var username = usernameTranscoder.decodeUsername(consultant.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(consultant.getId())))
        .andExpect(jsonPath("userName", is(username)))
        .andExpect(jsonPath("displayName", is(displayName)))
        .andExpect(jsonPath("isDisplayNameEditable", is(false)))
        .andExpect(jsonPath("firstName", is(consultant.getFirstName())))
        .andExpect(jsonPath("lastName", is(consultant.getLastName())))
        .andExpect(jsonPath("email", is(consultant.getEmail())))
        .andExpect(jsonPath("languages", is(notNullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", hasSize(1)))
        .andExpect(jsonPath("agencies[0].id", is(consultantAgency.getAgencyId().intValue())))
        .andExpect(jsonPath("agencies[0].name", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].postcode", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].city", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].description", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].teamAgency", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].offline", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].consultingType", is(notNullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("consultant")))
        .andExpect(jsonPath("grantedAuthorities", hasSize(1)))
        .andExpect(jsonPath("grantedAuthorities[0]", is("anAuthority")))
        .andExpect(jsonPath("consultingTypes", is(nullValue())))
        .andExpect(jsonPath("hasAnonymousConversations", is(true)))
        .andExpect(jsonPath("hasArchive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isEnabled", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isActive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.secret", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.qrCode", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.type", is("EMAIL")))
        .andExpect(jsonPath("twoFactorAuth.isToEncourage", is(consultant.getEncourage2fa())))
        .andExpect(jsonPath("absent", is(consultant.isAbsent())))
        .andExpect(jsonPath("formalLanguage", is(consultant.isLanguageFormal())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faByEmailIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidUser(true);
    givenConsultingTypeServiceResponse();
    givenKeycloakRespondsOtpByEmailHasBeenSetup(user.getUsername());

    var username = usernameTranscoder.decodeUsername(user.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(user.getUserId())))
        .andExpect(jsonPath("userName", is(username)))
        .andExpect(jsonPath("displayName", is(nullValue())))
        .andExpect(jsonPath("isDisplayNameEditable", is(false)))
        .andExpect(jsonPath("firstName", is(nullValue())))
        .andExpect(jsonPath("lastName", is(nullValue())))
        .andExpect(jsonPath("email", is(nullValue())))
        .andExpect(jsonPath("languages", is(nullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", is(nullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("user")))
        .andExpect(jsonPath("grantedAuthorities", hasSize(1)))
        .andExpect(jsonPath("grantedAuthorities[0]", is("anotherAuthority")))
        .andExpect(jsonPath("consultingTypes", is(notNullValue())))
        .andExpect(jsonPath("hasAnonymousConversations", is(false)))
        .andExpect(jsonPath("hasArchive", is(false)))
        .andExpect(jsonPath("twoFactorAuth.isEnabled", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isActive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.secret", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.qrCode", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.type", is("EMAIL")))
        .andExpect(jsonPath("twoFactorAuth.isToEncourage", is(user.getEncourage2fa())))
        .andExpect(jsonPath("absent", is(false)))
        .andExpect(jsonPath("formalLanguage", is(user.isLanguageFormal())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faIsNotActivated()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant(true);
    givenKeycloakRespondsOtpHasNotBeenSetup(consultant.getUsername());
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();

    var consultantAgency = consultant.getConsultantAgencies().iterator().next();
    var displayName = usernameTranscoder.decodeUsername(userInfoResponse.getUser().getName());
    var username = usernameTranscoder.decodeUsername(consultant.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(consultant.getId())))
        .andExpect(jsonPath("userName", is(username)))
        .andExpect(jsonPath("displayName", is(displayName)))
        .andExpect(jsonPath("isDisplayNameEditable", is(false)))
        .andExpect(jsonPath("firstName", is(consultant.getFirstName())))
        .andExpect(jsonPath("lastName", is(consultant.getLastName())))
        .andExpect(jsonPath("email", is(consultant.getEmail())))
        .andExpect(jsonPath("languages", is(notNullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", hasSize(1)))
        .andExpect(jsonPath("agencies[0].id", is(consultantAgency.getAgencyId().intValue())))
        .andExpect(jsonPath("agencies[0].name", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].postcode", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].city", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].description", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].teamAgency", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].offline", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].consultingType", is(notNullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("consultant")))
        .andExpect(jsonPath("grantedAuthorities", hasSize(1)))
        .andExpect(jsonPath("grantedAuthorities[0]", is("anAuthority")))
        .andExpect(jsonPath("consultingTypes", is(nullValue())))
        .andExpect(jsonPath("hasAnonymousConversations", is(true)))
        .andExpect(jsonPath("hasArchive", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isEnabled", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isActive", is(false)))
        .andExpect(jsonPath("twoFactorAuth.secret", is(notNullValue())))
        .andExpect(jsonPath("twoFactorAuth.qrCode", is(notNullValue())))
        .andExpect(jsonPath("twoFactorAuth.type", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.isToEncourage", is(consultant.getEncourage2fa())))
        .andExpect(jsonPath("absent", is(consultant.isAbsent())))
        .andExpect(jsonPath("formalLanguage", is(consultant.isLanguageFormal())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faIsNotActivated()
      throws Exception {
    givenABearerToken();
    givenAValidUser(true);
    givenConsultingTypeServiceResponse();
    givenKeycloakRespondsOtpHasNotBeenSetup(user.getUsername());

    var username = usernameTranscoder.decodeUsername(user.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(user.getUserId())))
        .andExpect(jsonPath("userName", is(username)))
        .andExpect(jsonPath("displayName", is(nullValue())))
        .andExpect(jsonPath("isDisplayNameEditable", is(false)))
        .andExpect(jsonPath("firstName", is(nullValue())))
        .andExpect(jsonPath("lastName", is(nullValue())))
        .andExpect(jsonPath("email", is(nullValue())))
        .andExpect(jsonPath("languages", is(nullValue())))
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", is(nullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("user")))
        .andExpect(jsonPath("grantedAuthorities", hasSize(1)))
        .andExpect(jsonPath("grantedAuthorities[0]", is("anotherAuthority")))
        .andExpect(jsonPath("consultingTypes", is(notNullValue())))
        .andExpect(jsonPath("hasAnonymousConversations", is(false)))
        .andExpect(jsonPath("hasArchive", is(false)))
        .andExpect(jsonPath("twoFactorAuth.isEnabled", is(true)))
        .andExpect(jsonPath("twoFactorAuth.isActive", is(false)))
        .andExpect(jsonPath("twoFactorAuth.secret", is(notNullValue())))
        .andExpect(jsonPath("twoFactorAuth.qrCode", is(notNullValue())))
        .andExpect(jsonPath("twoFactorAuth.type", is(nullValue())))
        .andExpect(jsonPath("twoFactorAuth.isToEncourage", is(user.getEncourage2fa())))
        .andExpect(jsonPath("absent", is(false)))
        .andExpect(jsonPath("formalLanguage", is(user.isLanguageFormal())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void getUserDataShouldContainEnabledFlags() throws Exception {
    givenABearerToken();
    givenAValidConsultant(true);
    givenConsultingTypeServiceResponse();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenKeycloakRespondsOtpHasNotBeenSetup(consultant.getUsername());
    givenEnabledE2EEncryption();
    givenDisplayNameAllowedForConsultants();

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("e2eEncryptionEnabled", is(true)))
        .andExpect(jsonPath("isDisplayNameEditable", is(true)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void patchUserDataShouldSaveAdviceSeekerAndRespondWithNoContent() throws Exception {
    givenAValidUser(true);
    givenAFullPatchDto();

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .cookie(RC_TOKEN_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var savedUser = userRepository.findById(user.getUserId());
    assertTrue(savedUser.isPresent());
    assertEquals(patchUserDTO.getEncourage2fa(), savedUser.get().getEncourage2fa());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void patchUserDataShouldSaveConsultantAndRespondWithNoContent() throws Exception {
    givenAValidConsultant(true);
    givenAFullPatchDto();
    givenAValidRocketChatUpdateUserResponse();

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .cookie(RC_TOKEN_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(patchUserDTO.getEncourage2fa(), savedConsultant.get().getEncourage2fa());

    var urlSuffix = "/api/v1/users.update";
    verify(rocketChatRestTemplate).postForEntity(
        endsWith(urlSuffix), updateUserCaptor.capture(), eq(Void.class)
    );

    var updateUser = updateUserCaptor.getValue().getBody();
    assertNotNull(updateUser);
    var user = updateUser.getData();
    assertTrue(user.getName().startsWith("enc."));
    assertTrue(user.getName().length() > 4);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void patchUserDataShouldOverrideDefaultAndRespondWithNoContent() throws Exception {
    givenAValidConsultant(true);
    givenAFullPatchDto(false);
    givenAValidRocketChatUpdateUserResponse();

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .cookie(RC_TOKEN_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(false, savedConsultant.get().getEncourage2fa());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void patchUserDataShouldOverridePreviousValueAndRespondWithNoContentEachTime()
      throws Exception {
    givenAValidConsultant(true);

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(true, savedConsultant.get().getEncourage2fa());

    givenAFullPatchDto(false);
    givenAValidRocketChatUpdateUserResponse();
    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .cookie(RC_TOKEN_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(false, savedConsultant.get().getEncourage2fa());

    givenAFullPatchDto(true);
    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .cookie(RC_TOKEN_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(true, savedConsultant.get().getEncourage2fa());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void patchUserDataShouldRespondWithBadRequestOnNullInMandatoryDtoFields()
      throws Exception {
    givenAValidConsultant(true);
    var patchDto = givenAnInvalidPatchDto();

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDto))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void patchUserDataShouldRespondWithBadRequestOnEmptyPayload() throws Exception {
    givenAValidUser(true);
    var patchDto = givenAnEmptyPatchDto();

    mockMvc.perform(
        patch("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patchDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void patchUserDataShouldRespondWithNoContentOnPartialPayload() throws Exception {
    givenAValidUser(true);
    var patchDto = givenAPartialPatchDto();

    mockMvc.perform(
        patch("/users/data")
            .cookie(CSRF_COOKIE)
            .cookie(RC_TOKEN_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patchDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void deactivateAndFlagUserAccountForDeletionShouldDeactivateAndRespondWithOkIf2faIsOff()
      throws Exception {
    givenAValidUser(true);
    givenADeleteUserAccountDto();
    givenAValidKeycloakLoginResponse();

    mockMvc.perform(
        delete("/users/account")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deleteUserAccountDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk());

    var savedUser = userRepository.findById(user.getUserId());
    assertTrue(savedUser.isPresent());
    assertNotNull(savedUser.get().getDeleteDate());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void deactivateAndFlagUserAccountForDeletionShouldDeactivateAndRespondWithOkIf2faIsOn()
      throws Exception {
    givenAValidUser(true);
    givenADeleteUserAccountDto();
    givenAnInvalidKeycloakLoginResponseMissingOtp();

    mockMvc.perform(
        delete("/users/account")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deleteUserAccountDto)
            ).accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk());

    var savedUser = userRepository.findById(user.getUserId());
    assertTrue(savedUser.isPresent());
    assertNotNull(savedUser.get().getDeleteDate());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void deactivateAndFlagUserAccountForDeletionShouldRespondWithBadRequestIfPasswordIsFalse()
      throws Exception {
    givenAValidUser(true);
    givenADeleteUserAccountDto();
    givenAnInvalidKeycloakLoginResponseFailingPassword();

    mockMvc.perform(
        delete("/users/account")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deleteUserAccountDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isBadRequest());

    var savedUser = userRepository.findById(user.getUserId());
    assertTrue(savedUser.isPresent());
    assertNull(savedUser.get().getDeleteDate());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void updatePasswordShouldUpdatePasswordAndRespondWithOkIf2faIsOff() throws Exception {
    givenAValidUser(true);
    givenAPasswordDto();
    givenAValidKeycloakLoginResponse();

    mockMvc.perform(
        put("/users/password/change")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void updatePasswordShouldUpdatePasswordAndRespondWithOkIf2faIsOn() throws Exception {
    givenAValidUser(true);
    givenAPasswordDto();
    givenAnInvalidKeycloakLoginResponseMissingOtp();

    mockMvc.perform(
        put("/users/password/change")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordDto)
            ).accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void updatePasswordShouldRespondWithBadRequestIfPasswordIsFalse() throws Exception {
    givenAValidUser(true);
    givenAPasswordDto();
    givenAnInvalidKeycloakLoginResponseFailingPassword();

    mockMvc.perform(
        put("/users/password/change")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldSaveDefaultLanguageAndRespondWithOk() throws Exception {
    givenAValidConsultant(true);
    givenAMinimalUpdateConsultantDto(consultant.getEmail());
    givenValidRocketChatTechUserResponse();

    mockMvc.perform(put("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    var savedLanguages = savedConsultant.get().getLanguages();
    assertEquals(1, savedLanguages.size());
    assertEquals(LanguageCode.de, savedLanguages.iterator().next().getLanguageCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldSaveGivenLanguagesAndRespondWithOk() throws Exception {
    givenAValidConsultant(true);
    givenAnUpdateConsultantDtoWithLanguages(consultant.getEmail());
    givenValidRocketChatTechUserResponse();

    mockMvc.perform(put("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    var savedLanguages = savedConsultant.get().getLanguages();
    assertEquals(3, savedLanguages.size());
    savedLanguages.forEach(language -> assertTrue(updateConsultantDTO.getLanguages().contains(
        de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode.fromValue(
            language.getLanguageCode().toString()
        )
    )));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldCascadeLanguageDeletionAndRespondWithOk() throws Exception {
    givenAValidConsultantSpeaking(easyRandom.nextObject(LanguageCode.class));
    givenAnUpdateConsultantDtoWithLanguages(consultant.getEmail());
    givenValidRocketChatTechUserResponse();

    mockMvc.perform(put("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    var savedLanguages = savedConsultant.get().getLanguages();
    assertEquals(3, savedLanguages.size());
    savedLanguages.forEach(language -> assertTrue(updateConsultantDTO.getLanguages().contains(
        de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode.fromValue(
            language.getLanguageCode().toString())
    )));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  public void removeFromSessionShouldReturnForbiddenIfSessionIdFormatIsInvalid() throws Exception {
    givenAValidConsultant(true);
    var sessionId = RandomStringUtils.randomAlphabetic(8);

    mockMvc.perform(
            delete("/users/sessions/{sessionId}/consultant/{consultantId}", sessionId,
                consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  public void removeFromSessionShouldReturnBadRequestIfConsultantIdFormatIsInvalid()
      throws Exception {
    var consultantId = RandomStringUtils.randomAlphanumeric(8);

    mockMvc.perform(
            delete("/users/sessions/1/consultant/{consultantId}", consultantId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  public void removeFromSessionShouldReturnNotFoundIfConsultantDoesNotExist() throws Exception {
    givenAValidSession();

    mockMvc.perform(
            delete("/users/sessions/{sessionId}/consultant/{consultantId}",
                session.getId(), UUID.randomUUID().toString())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  public void removeFromSessionShouldReturnNotFoundIfSessionDoesNotExist() throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();

    mockMvc.perform(
            delete("/users/sessions/{sessionId}/consultant/{consultantId}",
                RandomStringUtils.randomNumeric(5, 6), consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  public void removeFromSessionShouldReturnNoContent(CapturedOutput logOutput) throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenAValidSession();
    givenKeycloakUserRoles(consultant.getId(), "consultant");

    mockMvc.perform(
            delete("/users/sessions/{sessionId}/consultant/{consultantId}", session.getId(),
                consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var output = logOutput.getOut();
    int occurrencesOfAddTech = StringUtils.countOccurrencesOf(output,
        "RocketChatTestConfig.addTechnicalUserToGroup(" + session.getGroupId() + ") called");
    assertEquals(1, occurrencesOfAddTech);
    int occurrencesOfRemoval = StringUtils.countOccurrencesOf(output,
        "RocketChatTestConfig.removeUserFromGroup("
            + consultant.getRocketChatId() + "," + session.getGroupId()
            + ") called");
    assertEquals(1, occurrencesOfRemoval);
    int occurrencesOfRemoveTech = StringUtils.countOccurrencesOf(output,
        "RocketChatTestConfig.removeTechnicalUserFromGroup(" + session.getGroupId() + ") called");
    assertEquals(1, occurrencesOfRemoveTech);
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnClientErrorIfUserIdHasInvalidFormat() throws Exception {
    var invalidUserId = RandomStringUtils.randomAlphabetic(16);

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", invalidUserId, aPositiveLong())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnClientErrorIfChatIdHasInvalidFormat() throws Exception {
    givenAValidUser();
    var invalidChatId = RandomStringUtils.randomAlphabetic(16);

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", user.getRcUserId(), invalidChatId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnBadRequestIfRcTokenIsNotGiven() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", user.getRcUserId(), chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNotFoundIfUserDoesNotExist() throws Exception {
    var nonExistingUserId = RandomStringUtils.randomAlphanumeric(17);
    givenAValidConsultant(true);
    givenAValidChat();

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", nonExistingUserId, chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNotFoundIfChatDoesNotExist() throws Exception {
    givenAValidUser();

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", user.getRcUserId(), aPositiveLong())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNoContentIfBanWentWell() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatMuteUserInRoomResponse();

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", user.getRcUserId(), chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix = "/api/v1/method.call/muteUserInRoom";
    verify(rocketChatRestTemplate).postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(MessageResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNotFoundIfRocketChatReturnsAnInvalidResponse()
      throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAnInvalidRocketChatMuteUserInRoomResponse();

    mockMvc.perform(
            post("/users/{chatUserId}/chat/{chatId}/ban", user.getRcUserId(), chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    var urlSuffix = "/api/v1/method.call/muteUserInRoom";
    verify(rocketChatRestTemplate).postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(MessageResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void getChatShouldReturnOkIfUsersAreBannedAndAUserRequested() throws Exception {
    givenAValidUser(true);
    givenAValidConsultant();
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatRoomResponse(chat.getGroupId(), true);

    mockMvc.perform(
            get("/users/chat/{chatId}", chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("groupId", is(chat.getGroupId())))
        .andExpect(jsonPath("bannedUsers[0]", isA(String.class)));

    var urlSuffix = "/api/v1/rooms.info?roomId=" + chat.getGroupId();
    verify(rocketChatRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  public void getChatShouldReturnOkIfUsersAreNotBannedAndAUserRequested() throws Exception {
    givenAValidUser(true);
    givenAValidConsultant();
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatRoomResponse(chat.getGroupId(), false);

    mockMvc.perform(
            get("/users/chat/{chatId}", chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("groupId", is(chat.getGroupId())))
        .andExpect(jsonPath("bannedUsers", is(empty())));

    var urlSuffix = "/api/v1/rooms.info?roomId=" + chat.getGroupId();
    verify(rocketChatRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void getChatShouldReturnOkIfUsersAreBannedAndAConsultantRequested() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatRoomResponse(chat.getGroupId(), true);

    mockMvc.perform(
            get("/users/chat/{chatId}", chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("groupId", is(chat.getGroupId())))
        .andExpect(jsonPath("bannedUsers[0]", isA(String.class)));

    var urlSuffix = "/api/v1/rooms.info?roomId=" + chat.getGroupId();
    verify(rocketChatRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void getChatShouldReturnOkIfUsersAreNotBannedAndAConsultantRequested() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatRoomResponse(chat.getGroupId(), false);

    mockMvc.perform(
            get("/users/chat/{chatId}", chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("groupId", is(chat.getGroupId())))
        .andExpect(jsonPath("bannedUsers", is(empty())));

    var urlSuffix = "/api/v1/rooms.info?roomId=" + chat.getGroupId();
    verify(rocketChatRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.STOP_CHAT)
  public void stopChatShouldReturnOkIfUsersAreNotBanned() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatRoomResponse(chat.getGroupId(), false);

    mockMvc.perform(
            put("/users/chat/{chatId}/stop", chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/api/v1/rooms.info?roomId=" + chat.getGroupId();
    verify(rocketChatRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.STOP_CHAT)
  public void stopChatShouldReturnOkIfUsersAreBanned() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatRoomResponse(chat.getGroupId(), true);
    givenAValidRocketChatUnmuteResponse();

    mockMvc.perform(
            put("/users/chat/{chatId}/stop", chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/api/v1/rooms.info?roomId=" + chat.getGroupId();
    verify(rocketChatRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    );
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithNoContent() throws Exception {
    givenAValidConsultant(true);
    givenAValidEmailDTO();
    givenKeycloakFoundNoEmailInUse();
    givenABearerToken();
    givenAValidKeycloakVerifyEmailResponse();

    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix = "/auth/realms/test/otp-config/send-verification-mail/"
        + consultant.getUsername();
    verify(keycloakRestTemplate)
        .exchange(endsWith(urlSuffix), eq(HttpMethod.PUT), otpSetupCaptor.capture(),
            eq(Success.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(emailDTO.getEmail(), otpSetupDTO.getEmail());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithNoContentIfEmailIsOwnedByUser()
      throws Exception {
    givenAValidConsultant(true);
    givenAValidEmailDTO();
    givenKeycloakFoundOwnEmailInUse();
    givenABearerToken();
    givenAValidKeycloakVerifyEmailResponse();

    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfEmailIsNotAvailable()
      throws Exception {
    givenAValidConsultant(true);
    givenAValidEmailDTO();
    givenKeycloakFoundAnEmailInUse();
    givenABearerToken();

    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isPreconditionFailed());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTheEmailFormatIsInvalid()
      throws Exception {
    givenAnInvalidEmailDTO();

    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfNoPayloadIsGiven()
      throws Exception {
    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithInvalidParameterError()
      throws Exception {
    givenAValidConsultant(true);
    givenAValidEmailDTO();
    givenKeycloakFoundNoEmailInUse();
    givenABearerToken();
    givenAKeycloakVerifyEmailInvalidParameterErrorResponse();

    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithAlreadyConfiguredError()
      throws Exception {
    givenAValidConsultant(true);
    givenAValidEmailDTO();
    givenKeycloakFoundNoEmailInUse();
    givenABearerToken();
    givenAKeycloakVerifyEmailIAlreadyConfiguredErrorResponse();

    mockMvc.perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupForAConsultantShouldRespondWithNoContent()
      throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAValidKeycloakSetupEmailResponse(consultant.getUsername());
    givenAValidKeycloakEmailChangeByUsernameResponse(consultant.getUsername());

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(
            endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());

    var c = consultantRepository.findById(consultant.getId()).orElseThrow();
    assertEquals(email, c.getEmail());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupForAUserShouldRespondWithNoContent() throws Exception {
    givenAValidUser(true);
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAValidKeycloakSetupEmailResponse(user.getUsername());
    givenAValidKeycloakEmailChangeByUsernameResponse(user.getUsername());

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + user.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(
            endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());

    var u = userRepository.findById(user.getUserId()).orElseThrow();
    assertEquals(email, u.getEmail());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTanLengthIsWrong()
      throws Exception {
    givenAWronglyFormattedTan();

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTanHasLetters()
      throws Exception {
    givenATanWithLetters();

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithNotFoundIfTanIsEmpty()
      throws Exception {
    mockMvc.perform(
            post("/users/2fa/email/validate/")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTheTanIsInvalid()
      throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakSetupEmailInvalidCodeResponse();

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(
            endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
      throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakSetupEmailOtpAnotherOtpConfigActiveErrorResponse();

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(
            endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithTooManyRequestsIfTooManyTanAttempts()
      throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakSetupEmailTooManyRequestsResponse();

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isTooManyRequests());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(
            endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithPreconditionFailedIfOtpByEmailHasBeenSetupBefore()
      throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakAlreadySetupEmailResponse();

    mockMvc.perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isPreconditionFailed());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(
            endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateE2eInChatsShouldRespondWithNoContent() throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedE2eKeyDTO();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    var subscriptionSize = easyRandom.nextInt(4) + 1;
    givenAValidRocketChatGetSubscriptionsResponse(subscriptionSize);
    givenValidRocketChatGroupKeyUpdateResponses();

    mockMvc.perform(
        put("/users/chat/e2e")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("rcToken", RandomStringUtils.randomAlphabetic(16))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(e2eKeyDTO))
    ).andExpect(status().isNoContent());

    var urlSuffix = "/api/v1/users.info?userId=" + consultant.getRocketChatId();
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(UserInfoResponseDTO.class));

    urlSuffix = "/api/v1/subscriptions.get";
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class));

    urlSuffix = "/api/v1/e2e.updateGroupKey";
    verify(rocketChatRestTemplate, times(subscriptionSize))
        .postForEntity(endsWith(urlSuffix), any(HttpEntity.class), eq(StandardResponseDTO.class));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateE2eInChatsShouldRespondWithNoContentOnEmptySubscriptions() throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedE2eKeyDTO();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenAValidRocketChatGetSubscriptionsResponse(0);

    mockMvc.perform(
        put("/users/chat/e2e")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("rcToken", RandomStringUtils.randomAlphabetic(16))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(e2eKeyDTO))
    ).andExpect(status().isNoContent());

    var urlSuffix = "/api/v1/users.info?userId=" + consultant.getRocketChatId();
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(UserInfoResponseDTO.class));

    urlSuffix = "/api/v1/subscriptions.get";
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class));

    urlSuffix = "/api/v1/e2e.updateGroupKey";
    verify(rocketChatRestTemplate, never())
        .postForEntity(endsWith(urlSuffix), any(HttpEntity.class), eq(StandardResponseDTO.class));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateE2eInChatsShouldRespondWithInternalServerErrorIfNotTemporarilyEncrypted()
      throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedE2eKeyDTO();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenARocketChatGetSubscriptionsResponseIncludingNoneTemporary();

    mockMvc.perform(
            put("/users/chat/e2e")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e2eKeyDTO))
        )
        .andExpect(status().isInternalServerError());

    var urlSuffix = "/api/v1/users.info?userId=" + consultant.getRocketChatId();
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(UserInfoResponseDTO.class));

    urlSuffix = "/api/v1/subscriptions.get";
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class));

    urlSuffix = "/api/v1/e2e.updateGroupKey";
    verify(rocketChatRestTemplate, never())
        .postForEntity(endsWith(urlSuffix), any(HttpEntity.class), eq(StandardResponseDTO.class));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateE2eInChatsShouldRespondWithInternalServerErrorIfSubscriptionUpdateFailed()
      throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedE2eKeyDTO();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenAValidRocketChatGetSubscriptionsResponse(easyRandom.nextInt(4) + 1);
    givenFailedRocketChatGroupKeyUpdateResponses();

    mockMvc.perform(
            put("/users/chat/e2e")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header("rcToken", RandomStringUtils.randomAlphabetic(16))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e2eKeyDTO))
        )
        .andExpect(status().isInternalServerError());

    var urlSuffix = "/api/v1/users.info?userId=" + consultant.getRocketChatId();
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(UserInfoResponseDTO.class));

    urlSuffix = "/api/v1/subscriptions.get";
    verify(rocketChatRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class));

    urlSuffix = "/api/v1/e2e.updateGroupKey";
    verify(rocketChatRestTemplate)
        .postForEntity(endsWith(urlSuffix), any(HttpEntity.class), eq(StandardResponseDTO.class));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateE2eInChatsShouldRespondWithBadRequestIfE2eKeyHasWrongFormat() throws Exception {
    givenAValidConsultant(true);
    givenAWronglyFormattedE2eKeyDTO();

    mockMvc.perform(
        put("/users/chat/e2e")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(e2eKeyDTO))
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateE2eInChatsShouldRespondWithBadRequestIfPayloadIsEmpty() throws Exception {
    givenAValidConsultant(true);

    mockMvc.perform(
        put("/users/chat/e2e")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithOK() throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();

    mockMvc.perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    verify(keycloakRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.PUT),
        otpSetupCaptor.capture(),
        eq(OtpInfoDTO.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(oneTimePasswordDTO.getOtp(), otpSetupDTO.getInitialCode());
    assertEquals(oneTimePasswordDTO.getSecret(), otpSetupDTO.getSecret());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenOtpHasWrongLength()
      throws Exception {
    givenAValidConsultant(true);
    givenAnInvalidOneTimePasswordDTO();

    mockMvc.perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenSecretHasWrongLength()
      throws Exception {
    givenAValidConsultant(true);
    givenAWronglyFormattedSecret();

    mockMvc.perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfTheOtpIsInvalid()
      throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpValidationErrorResponse();

    mockMvc.perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfParameterInvalid()
      throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpInvalidParameterErrorResponse();

    mockMvc.perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }


  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
      throws Exception {
    givenAValidConsultant(true);
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpAnotherOtpConfigActiveErrorResponse();

    mockMvc.perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void deactivateTwoFactorAuthByAppShouldRespondWithOK() throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();

    mockMvc.perform(
            delete("/users/2fa")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/auth/realms/test/otp-config/delete-otp/" + consultant.getUsername();
    verify(keycloakRestTemplate).exchange(
        endsWith(urlSuffix), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)
    );
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void deactivateTwoFactorAuthByAppShouldRespondWithInternalServerErrorWhenKeycloakIsDown()
      throws Exception {
    givenAValidConsultant(true);
    givenABearerToken();
    givenKeycloakIsDown();

    mockMvc.perform(
            delete("/users/2fa")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void registerUserWithoutConsultingIdShouldSaveMonitoring() throws Exception {
    givenConsultingTypeServiceResponse();
    givenARealmResource();
    givenAUserDTO();

    mockMvc.perform(
            post("/users/askers/new")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
        .andExpect(status().isCreated());
  }

  private void givenARealmResource() {
    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(mock(UsersResource.class));
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenKeycloakUserRoles(String userId, String... roles) {
    var realmResource = mock(RealmResource.class);
    var usersResource = mock(UsersResource.class);
    var userResource = mock(UserResource.class);
    var roleMappingResource = mock(RoleMappingResource.class);
    var roleScopeResource = mock(RoleScopeResource.class);
    var roleRepresentationList = Arrays.stream(roles)
        .map(role -> new RoleRepresentation(role, "", false))
        .collect(Collectors.toList());
    when(roleScopeResource.listAll()).thenReturn(roleRepresentationList);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(usersResource.get(userId)).thenReturn(userResource);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenAValidKeycloakEmailChangeByUsernameResponse(String username) {
    var usernameTranscoder = new UsernameTranscoder();
    var userRepresentation = new UserRepresentation();
    var encodedUsername = usernameTranscoder.encodeUsername(username);
    var keycloakId = UUID.randomUUID().toString();
    userRepresentation.setId(keycloakId);
    userRepresentation.setUsername(encodedUsername);
    userRepresentation.setEmail(givenAValidEmail());
    var userRepresentationList = new ArrayList<UserRepresentation>(1);
    userRepresentationList.add(userRepresentation);
    var usersResource = mock(UsersResource.class);
    var userResource = mock(UserResource.class);

    when(usersResource.search(eq(encodedUsername))).thenReturn(userRepresentationList);
    when(usersResource.get(keycloakId)).thenReturn(userResource);

    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenAUserDTO() {
    userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setUsername(RandomStringUtils.randomAlphabetic(5, 30));
    userDTO.setAge("17");
    userDTO.setState("8");
    userDTO.setPostcode(RandomStringUtils.randomNumeric(5));
    userDTO.setTermsAccepted("true");
    userDTO.setConsultingType("1");
    userDTO.setConsultantId(null);
    userDTO.setAgencyId(aPositiveLong());
    userDTO.setEmail(givenAValidEmail());
  }

  private long aPositiveLong() {
    return Math.abs(easyRandom.nextLong());
  }

  private void givenKeycloakIsDown() {
    var urlSuffix = "/auth/realms/test/otp-config/delete-otp/" + consultant.getUsername();
    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class))
    ).thenThrow(new RestClientException("Keycloak down"));
  }

  private void givenAnInvalidOneTimePasswordDTO() {
    givenACorrectlyFormattedOneTimePasswordDTO();
    while (oneTimePasswordDTO.getOtp().length() == 6) {
      oneTimePasswordDTO.setOtp(RandomStringUtils.randomNumeric(1, 32));
    }
  }

  private void givenAWronglyFormattedSecret() {
    givenACorrectlyFormattedOneTimePasswordDTO();
    while (oneTimePasswordDTO.getSecret().length() == 32) {
      oneTimePasswordDTO.setSecret(RandomStringUtils.randomNumeric(1, 64));
    }
  }

  private void givenACorrectlyFormattedOneTimePasswordDTO() {
    oneTimePasswordDTO = new OneTimePasswordDTO();
    oneTimePasswordDTO.setOtp(RandomStringUtils.randomNumeric(6));
    oneTimePasswordDTO.setSecret(RandomStringUtils.randomAlphanumeric(32));
  }

  private void givenACorrectlyFormattedE2eKeyDTO() {
    var n = "w5j-hUYZRT-ZSBJsk3J1gEtZG5fuP66dWMxs2I4PxgIC7TH8JU_zEDSjgjR6mCsIARVhyzZnBsNVoJYIg2TDF"
        + "18TAcYhaDsFEhxntg9RktrLGIs_nod0cafLCVQYWfp27SrpBeHdO9ewuezJzSzvNPZnx-8iWIDqp_nQt2xSPdh2"
        + "8AUm8f3KJ0P0AGFL6HiQ24GcLlsi-xqit3_M-MMr0kYJenaxJX1IdXCd1Io_pWBcgykSxhGo0fDWpfhkS1jmU4_"
        + "_9RNfoR1uroa10g3YVWYXvpZ5T9Qw96ynhwqdLMsGwbo1Y2AyG8NckOR3fE4ARC3OSUv0LFqmdq2xf5quZw";

    e2eKeyDTO = new E2eKeyDTO();
    e2eKeyDTO.setPublicKey(n);
  }

  private void givenAWronglyFormattedE2eKeyDTO() {
    e2eKeyDTO = new E2eKeyDTO();
    e2eKeyDTO.setPublicKey(RandomStringUtils.randomAlphanumeric(8));
  }

  private void givenAValidKeycloakSetupEmailResponse(String username) {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + username;
    var successWithEmail = new SuccessWithEmail();
    email = givenAValidEmail();
    successWithEmail.setEmail(email);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenReturn(new ResponseEntity<>(successWithEmail, HttpStatus.CREATED));
  }

  private void givenAKeycloakSetupEmailInvalidCodeResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var codeInvalid = new HttpClientErrorException(HttpStatus.UNAUTHORIZED,
        "the code was not valid", null, null);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenThrow(codeInvalid);
  }

  private void givenKeycloakFoundAnEmailInUse() {
    var usernameTranscoder = new UsernameTranscoder();
    var userRepresentation = new UserRepresentation();
    var username = usernameTranscoder.encodeUsername(RandomStringUtils.randomAlphabetic(8, 16));
    userRepresentation.setUsername(username);
    userRepresentation.setEmail(emailDTO.getEmail());
    var userRepresentationList = new ArrayList<UserRepresentation>(1);
    userRepresentationList.add(userRepresentation);
    var usersResource = mock(UsersResource.class);
    when(usersResource.search(eq(emailDTO.getEmail()), anyInt(), anyInt()))
        .thenReturn(userRepresentationList);
    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenKeycloakFoundOwnEmailInUse() {
    var usernameTranscoder = new UsernameTranscoder();
    var userRepresentation = new UserRepresentation();
    var username = usernameTranscoder.encodeUsername(consultant.getUsername());
    userRepresentation.setUsername(username);
    userRepresentation.setEmail(emailDTO.getEmail());
    var userRepresentationList = new ArrayList<UserRepresentation>(1);
    userRepresentationList.add(userRepresentation);
    var usersResource = mock(UsersResource.class);
    when(usersResource.search(eq(emailDTO.getEmail()), anyInt(), anyInt()))
        .thenReturn(userRepresentationList);
    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenKeycloakFoundNoEmailInUse() {
    var userRepresentationList = new ArrayList<UserRepresentation>(0);
    var usersResource = mock(UsersResource.class);
    when(usersResource.search(eq(emailDTO.getEmail()), anyInt(), anyInt()))
        .thenReturn(userRepresentationList);
    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenAKeycloakSetupEmailOtpAnotherOtpConfigActiveErrorResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var codeInvalid = new HttpClientErrorException(HttpStatus.CONFLICT,
        "another otp configuration is already active", null, null);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenThrow(codeInvalid);
  }

  private void givenAnInfix() {
    infix = RandomStringUtils.randomAlphanumeric(7)
        + (easyRandom.nextBoolean() ? "ä" : "Ö")
        + RandomStringUtils.randomAlphanumeric(7);
  }

  private void givenConsultantsMatching(@PositiveOrZero int count, @NotBlank String infix) {
    givenConsultantsMatching(count, infix, false, false);
  }

  private void givenConsultantsMatching(@PositiveOrZero int count, @NotBlank String infix,
      boolean includingAgenciesMarkedAsDeleted, boolean markedAsDeleted) {
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

      var consultantAgency = ConsultantAgency.builder()
          .consultant(consultant)
          .agencyId(aPositiveLong())
          .build();
      if (includingAgenciesMarkedAsDeleted) {
        var deleteDate = easyRandom.nextBoolean() ? null : LocalDateTime.now();
        consultantAgency.setDeleteDate(deleteDate);
      }
      consultantAgencyRepository.save(consultantAgency);
      consultantAgencies.add(consultantAgency);
      consultant.setConsultantAgencies(Set.of(consultantAgency));
      consultantRepository.save(consultant);
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
    return RandomStringUtils.randomAlphabetic(4)
        + infix
        + RandomStringUtils.randomAlphabetic(4);
  }

  private void givenAKeycloakSetupEmailTooManyRequestsResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var tooManyAttempts = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
        "too many attempts", null, null);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenThrow(tooManyAttempts);
  }

  private void givenAKeycloakAlreadySetupEmailResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var successWithEmail = new SuccessWithEmail();
    email = givenAValidEmail();
    successWithEmail.setEmail(email);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenReturn(new ResponseEntity<>(successWithEmail, HttpStatus.OK));
  }

  private void givenAValidKeycloakLoginResponse() {
    var loginResponse = easyRandom.nextObject(KeycloakLoginResponseDTO.class);
    var urlSuffix = "/auth/realms/test/protocol/openid-connect/token";
    when(restTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(KeycloakLoginResponseDTO.class)
    )).thenReturn(ResponseEntity.ok().body(loginResponse));
  }

  private void givenAnInvalidKeycloakLoginResponseFailingPassword() {
    var exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
    var urlSuffix = "/auth/realms/test/protocol/openid-connect/token";
    when(restTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(KeycloakLoginResponseDTO.class)
    )).thenThrow(exception);
  }

  private void givenAnInvalidKeycloakLoginResponseMissingOtp() throws JsonProcessingException {
    var responseMap = Map.of(
        "error", "invalid_grant",
        "error_description", "Missing totp",
        "otpType", easyRandom.nextBoolean() ? "EMAIL" : "APP"
    );
    var body = objectMapper.writeValueAsString(responseMap).getBytes();
    var statusText = HttpStatus.BAD_REQUEST.getReasonPhrase();
    var exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, statusText, body, null);
    var urlSuffix = "/auth/realms/test/protocol/openid-connect/token";
    when(restTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(KeycloakLoginResponseDTO.class)
    )).thenThrow(exception);
  }

  private void givenAValidKeycloakVerifyEmailResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var success = easyRandom.nextObject(Success.class);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)
    )).thenReturn(ResponseEntity.ok(success));
  }

  private void givenAKeycloakVerifyEmailInvalidParameterErrorResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var invalidParameter = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "invalid parameter",
        null, null);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)
    )).thenThrow(invalidParameter);
  }

  private void givenAKeycloakVerifyEmailIAlreadyConfiguredErrorResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var invalidParameter = new HttpClientErrorException(HttpStatus.CONFLICT, "already configured",
        null, null);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)
    )).thenThrow(invalidParameter);
  }

  private void givenAKeycloakSetupOtpInvalidParameterErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidParameter = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "invalid parameter",
        null, null);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenThrow(invalidParameter);
  }

  private void givenAgencyServiceReturningAgencies() {
    var agencies = new ArrayList<AgencyDTO>();
    consultantAgencies.forEach(consultantAgency -> {
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

    when(agencyService.getAgenciesWithoutCaching(anyList()))
        .thenReturn(agencies);
  }

  private void givenAgencyServiceReturningDummyAgencies() {
    var agencies = new ArrayList<AgencyDTO>();

    when(agencyService.getAgenciesWithoutCaching(anyList()))
        .thenAnswer(i -> {
          List<Long> agencyIds = i.getArgument(0);
          agencyIds.forEach(agencyId -> {
            var agency = new AgencyDTO();
            agency.setId(agencyId);
            agency.setName(RandomStringUtils.randomAlphabetic(16));
            agency.setPostcode(RandomStringUtils.randomNumeric(5));
            agencies.add(agency);
          });
          return agencies;
        });
  }

  private void givenAKeycloakSetupOtpAnotherOtpConfigActiveErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidParameter = new HttpClientErrorException(HttpStatus.CONFLICT,
        "another otp configuration is already active", null, null);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenThrow(invalidParameter);
  }

  private void givenAKeycloakSetupOtpValidationErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidCode = new HttpClientErrorException(HttpStatus.UNAUTHORIZED,
        "the code was not valid", null, null);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenThrow(invalidCode);
  }

  private void givenKeycloakRespondsOtpByAppHasBeenSetup(String username) {
    var urlSuffix = "/auth/realms/test/otp-config/fetch-otp-setup-info/" + username;

    var otpInfo = new OtpInfoDTO();
    otpInfo.setOtpSetup(true);
    otpInfo.setOtpType(OtpType.APP);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenReturn(ResponseEntity.ok(otpInfo));
  }

  private void givenKeycloakRespondsOtpByEmailHasBeenSetup(String username) {
    var urlSuffix = "/auth/realms/test/otp-config/fetch-otp-setup-info/" + username;

    var otpInfo = new OtpInfoDTO();
    otpInfo.setOtpSetup(true);
    otpInfo.setOtpType(OtpType.EMAIL);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenReturn(ResponseEntity.ok(otpInfo));
  }

  private void givenKeycloakRespondsOtpHasNotBeenSetup(String username) {
    var otpInfo = new OtpInfoDTO();
    otpInfo.setOtpSetup(false);
    otpInfo.setOtpSecret(RandomStringUtils.randomAlphabetic(32));
    otpInfo.setOtpSecretQrCode(RandomStringUtils.randomAlphabetic(64));

    var urlSuffix = "/auth/realms/test/otp-config/fetch-otp-setup-info/" + username;

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenReturn(ResponseEntity.ok(otpInfo));
  }

  private void givenAValidRocketChatUnmuteResponse() {
    var urlSuffix = "/method.call/unmuteUserInRoom";
    var messageResponse = easyRandom.nextObject(MessageResponse.class);
    messageResponse.setSuccess(true);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(MessageResponse.class)
    )).thenReturn(ResponseEntity.ok(messageResponse));
  }

  private void givenAValidRocketChatInfoUserResponse() {
    userInfoResponse = new UserInfoResponseDTO();
    userInfoResponse.setSuccess(true);

    var chatUser = easyRandom.nextObject(RocketChatUserDTO.class);
    chatUser.setId(consultant.getRocketChatId());
    chatUser.setUsername(consultant.getUsername());
    chatUser.setName(usernameTranscoder.encodeUsername("Consultant Max"));
    userInfoResponse.setUser(chatUser);

    var urlSuffix = "/api/v1/users.info?userId=" + consultant.getRocketChatId();
    when(rocketChatRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(UserInfoResponseDTO.class))
    ).thenReturn(ResponseEntity.ok(userInfoResponse));
  }

  private void givenAValidRocketChatGetSubscriptionsResponse(int subscriptionSize) {
    subscriptionsGetResponse = new SubscriptionsGetDTO();
    subscriptionsGetResponse.setSuccess(true);

    var updates = new ArrayList<SubscriptionsUpdateDTO>(subscriptionSize);
    for (int i = 0; i < subscriptionSize; i++) {
      var subscriptionsUpdateDTO = easyRandom.nextObject(SubscriptionsUpdateDTO.class);
      subscriptionsUpdateDTO.setRoomId(RandomStringUtils.randomAlphanumeric(8));
      subscriptionsUpdateDTO.setE2eKey(
          "tmp.1234567890abU2FsdGVkX1+3tjZ5PaAKTMSKZS4v8t8BwGmmhqoMj68=");
      var user = new RocketChatUserDTO();
      user.setId(RandomStringUtils.randomAlphanumeric(17));
      subscriptionsUpdateDTO.setUser(user);
      updates.add(subscriptionsUpdateDTO);
    }
    subscriptionsGetResponse.setUpdate(updates.toArray(new SubscriptionsUpdateDTO[0]));

    var urlSuffix = "/api/v1/subscriptions.get";
    when(rocketChatRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class))
    ).thenReturn(ResponseEntity.ok(subscriptionsGetResponse));
  }

  private void givenARocketChatGetSubscriptionsResponseIncludingNoneTemporary() {
    subscriptionsGetResponse = new SubscriptionsGetDTO();
    subscriptionsGetResponse.setSuccess(true);

    var size = easyRandom.nextInt(5);
    var updates = new ArrayList<SubscriptionsUpdateDTO>(size);
    for (int i = 0; i <= size; i++) {
      var subscriptionsUpdateDTO = easyRandom.nextObject(SubscriptionsUpdateDTO.class);
      subscriptionsUpdateDTO.setRoomId(RandomStringUtils.randomAlphanumeric(8));
      subscriptionsUpdateDTO.setE2eKey(RandomStringUtils.randomAlphanumeric(60));
      var user = new RocketChatUserDTO();
      user.setId(RandomStringUtils.randomAlphanumeric(17));
      subscriptionsUpdateDTO.setUser(user);
      updates.add(subscriptionsUpdateDTO);
    }
    subscriptionsGetResponse.setUpdate(updates.toArray(new SubscriptionsUpdateDTO[0]));

    var urlSuffix = "/api/v1/subscriptions.get";
    when(rocketChatRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class))
    ).thenReturn(ResponseEntity.ok(subscriptionsGetResponse));
  }

  private void givenValidRocketChatGroupKeyUpdateResponses() {
    var urlSuffix = "/api/v1/e2e.updateGroupKey";
    var response = easyRandom.nextObject(StandardResponseDTO.class);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(StandardResponseDTO.class)
    )).thenReturn(ResponseEntity.ok(response));
  }

  private void givenFailedRocketChatGroupKeyUpdateResponses() {
    var urlSuffix = "/api/v1/e2e.updateGroupKey";

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(StandardResponseDTO.class)
    )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
  }

  private void givenAValidRocketChatUpdateUserResponse() {
    var urlSuffix = "/api/v1/users.update";
    var updateUserResponse = easyRandom.nextObject(Void.class);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(Void.class)
    )).thenReturn(ResponseEntity.ok(updateUserResponse));
  }

  private void givenAValidRocketChatRoomResponse(String roomId, boolean hasBannedUsers) {
    var urlSuffix = "/rooms.info?roomId=" + roomId;
    var roomResponse = easyRandom.nextObject(RoomResponse.class);
    roomResponse.setSuccess(true);
    if (hasBannedUsers) {
      roomResponse.getRoom().setMuted(List.of(user.getRcUserId()));
    } else {
      roomResponse.getRoom().setMuted(null);
    }

    when(rocketChatRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomResponse.class)
    )).thenReturn(ResponseEntity.ok(roomResponse));
  }

  private void givenAValidRocketChatMuteUserInRoomResponse() {
    var urlSuffix = "/api/v1/method.call/muteUserInRoom";
    var messageResponse = easyRandom.nextObject(MessageResponse.class);
    messageResponse.setSuccess(true);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(MessageResponse.class)
    )).thenReturn(ResponseEntity.ok(messageResponse));
  }

  private void givenAnInvalidRocketChatMuteUserInRoomResponse() {
    var urlSuffix = "/api/v1/method.call/muteUserInRoom";
    var messageResponse = easyRandom.nextObject(MessageResponse.class);
    messageResponse.setSuccess(true); // according to Rocket.Chat
    var message = RandomStringUtils.randomAlphanumeric(8)
        + "error-user-not-in-room"
        + RandomStringUtils.randomAlphanumeric(8);
    messageResponse.setMessage(message);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(MessageResponse.class)
    )).thenReturn(ResponseEntity.ok().body(messageResponse));
  }

  private void givenAValidEmailDTO() {
    var email = givenAValidEmail();
    emailDTO = new EmailDTO();
    emailDTO.setEmail(email);
  }

  @NonNull
  private String givenAValidEmail() {
    return RandomStringUtils.randomAlphabetic(8)
        + "@" + RandomStringUtils.randomAlphabetic(8)
        + ".com";
  }

  private void givenAnInvalidEmailDTO() {
    var email = RandomStringUtils.randomAlphabetic(16) + ".com";

    emailDTO = new EmailDTO();
    emailDTO.setEmail(email);
  }

  private void givenABearerToken() {
    var tokenManager = mock(TokenManager.class);
    when(tokenManager.getAccessTokenString())
        .thenReturn(RandomStringUtils.randomAlphanumeric(255));
    when(keycloak.tokenManager()).thenReturn(tokenManager);
  }

  private void givenACorrectlyFormattedTan() {
    tan = RandomStringUtils.randomNumeric(6);
  }

  private void givenAWronglyFormattedTan() {
    while (isNull(tan) || tan.length() == 6) {
      tan = RandomStringUtils.randomNumeric(1, 32);
    }
  }

  private void givenATanWithLetters() {
    tan = RandomStringUtils.randomAlphabetic(6);
  }

  private void givenAConsultantWithMultipleAgencies() {
    consultant = consultantRepository.findById("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe")
        .orElseThrow();
  }

  private void givenAValidConsultant() {
    givenAValidConsultant(false);
  }

  private void givenAValidConsultant(boolean isAuthUser) {
    consultant = consultantRepository.findAll().iterator().next();
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
      when(authenticatedUser.isUser()).thenReturn(false);
      when(authenticatedUser.isConsultant()).thenReturn(true);
      when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
      when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.CONSULTANT.getValue()));
      when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
    }
  }

  private void givenAValidUser() {
    givenAValidUser(false);
  }

  private void givenAValidUser(boolean isAuthUser) {
    user = userRepository.findAll().iterator().next();
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
      when(authenticatedUser.isUser()).thenReturn(true);
      when(authenticatedUser.isConsultant()).thenReturn(false);
      when(authenticatedUser.getUsername()).thenReturn(user.getUsername());
      when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.USER.getValue()));
      when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anotherAuthority"));
    }
  }

  private void givenAValidChat() {
    chat = easyRandom.nextObject(Chat.class);
    chat.setId(null);
    chat.setActive(true);
    chat.setRepetitive(false);
    chat.setChatOwner(consultant);
    chat.setConsultingTypeId(easyRandom.nextInt(128));
    chat.setDuration(easyRandom.nextInt(32768));
    chat.setMaxParticipants(easyRandom.nextInt(128));
    chatRepository.save(chat);

    var agencyId = consultant.getConsultantAgencies().iterator().next().getAgencyId();
    var chatAgency = new ChatAgency();
    chatAgency.setChat(chat);
    chatAgency.setAgencyId(agencyId);
    chatAgencyRepository.save(chatAgency);

    if (nonNull(user)) {
      var userAgency = new UserAgency();
      userAgency.setUser(user);
      userAgency.setAgencyId(agencyId);
      user.getUserAgencies().add(userAgency);
      userAgencyRepository.save(userAgency);
    }
  }

  private void givenConsultingTypeServiceResponse() {
    consultingTypeControllerApi.getApiClient().setBasePath("https://www.google.de/");
    when(restTemplate.getUriTemplateHandler()).thenReturn(new UriTemplateHandler() {
      @SneakyThrows
      @Override
      public @NonNull URI expand(@NonNull String uriTemplate,
          @NonNull Map<String, ?> uriVariables) {
        return new URI("");
      }

      @SneakyThrows
      @Override
      public @NonNull URI expand(@NonNull String uriTemplate, Object @NonNull ... uriVariables) {
        return new URI("");
      }
    });

    var body = new BasicConsultingTypeResponseDTO();
    body.setId(1);
    ParameterizedTypeReference<List<BasicConsultingTypeResponseDTO>> value = new ParameterizedTypeReference<>() {
    };
    when(restTemplate.exchange(any(RequestEntity.class), eq(value)))
        .thenReturn(ResponseEntity.ok(List.of(body)));
  }

  private void givenAValidConsultantSpeaking(LanguageCode languageCode) {
    givenAValidConsultant(true);
    consultant.setLanguages(Set.of(new Language(consultant, languageCode)));
    consultant = consultantRepository.save(consultant);
  }

  private long givenAnAgencyIdWithDefaultLanguageOnly() {
    return 121;
  }

  private long givenAnAgencyWithMultipleLanguages() {
    var agencyId = 0L;

    consultantAgencyRepository
        .findByAgencyIdAndDeleteDateIsNull(agencyId)
        .forEach(consultantAgency -> {
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
        language.getLanguageCode().name()
    );
  }

  private void givenAMinimalUpdateConsultantDto(String email) {
    updateConsultantDTO = new UpdateConsultantDTO()
        .email(email)
        .firstname(RandomStringUtils.randomAlphabetic(8))
        .lastname(RandomStringUtils.randomAlphabetic(12));
  }

  private void givenAPasswordDto() {
    passwordDto = easyRandom.nextObject(PasswordDTO.class);
  }

  private void givenADeleteUserAccountDto() {
    deleteUserAccountDto = easyRandom.nextObject(DeleteUserAccountDTO.class);
  }

  private HashMap<String, Object> givenAnInvalidPatchDto() {
    var patchDtoAsMap = new HashMap<String, Object>(1);
    if (easyRandom.nextBoolean()) {
      patchDtoAsMap.put("encourage2fa", null);
    } else {
      patchDtoAsMap.put("displayName", "");
    }

    return patchDtoAsMap;
  }

  private HashMap<String, Object> givenAnEmptyPatchDto() {
    return new HashMap<>(0);
  }

  private HashMap<String, Object> givenAPartialPatchDto() {
    var patchDtoAsMap = new HashMap<String, Object>(1);
    patchDtoAsMap.put("displayName", RandomStringUtils.randomAlphabetic(4));

    return patchDtoAsMap;
  }

  private void givenAFullPatchDto(boolean encourage2fa) {
    givenAFullPatchDto();
    patchUserDTO.setEncourage2fa(encourage2fa);
  }

  private void givenAFullPatchDto() {
    patchUserDTO = easyRandom.nextObject(PatchUserDTO.class);
  }

  private void givenAnUpdateConsultantDtoWithLanguages(String email) {
    givenAMinimalUpdateConsultantDto(email);

    var languages = List.of(
        easyRandom.nextObject(de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode.class),
        easyRandom.nextObject(de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode.class),
        easyRandom.nextObject(de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode.class)
    );
    updateConsultantDTO.languages(languages);
  }

  private void givenAValidRocketChatSystemUser() {
    when(rocketChatCredentialsProvider.getSystemUserSneaky()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
  }

  private void givenValidRocketChatTechUserResponse() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    var body = new UserInfoResponseDTO();
    body.setSuccess(true);
    if (nonNull(user)) {
      body.setUser(new RocketChatUserDTO("", user.getUsername(), null, null));
    }
    var userInfoResponseDTO = ResponseEntity.ok(body);
    when(restTemplate.exchange(anyString(), any(), any(), eq(UserInfoResponseDTO.class)))
        .thenReturn(userInfoResponseDTO);
    when(restTemplate.exchange(anyString(), any(), any(), eq(UserInfoResponseDTO.class),
        anyString())).thenReturn(userInfoResponseDTO);
  }

  @SuppressWarnings("unchecked")
  private void givenValidRocketChatCreationResponse() {
    var uriTemplateHandler = mock(UriTemplateHandler.class);
    when(uriTemplateHandler.expand(anyString(), anyMap()))
        .thenReturn(easyRandom.nextObject(URI.class));
    when(restTemplate.getUriTemplateHandler()).thenReturn(uriTemplateHandler);
    when(restTemplate.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
        .thenReturn(ResponseEntity.ok().build());
  }

  private void givenNoRocketChatRoomUpdates() {
    var response = new RoomsGetDTO();
    var roomsUpdate = new RoomsUpdateDTO[0];
    response.setUpdate(roomsUpdate);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
        eq(RoomsGetDTO.class))).thenReturn(ResponseEntity.ok(response));
  }

  private void givenNoRocketChatSubscriptionUpdates() {
    var response = new SubscriptionsGetDTO();
    var subscriptionsUpdate = new SubscriptionsUpdateDTO[0];
    response.setUpdate(subscriptionsUpdate);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
        eq(SubscriptionsGetDTO.class))).thenReturn(ResponseEntity.ok(response));
  }

  private void givenAUserWithASessionNotEnquired() {
    user = userRepository.findById("552d3f10-1b6d-47ee-aec5-b88fbf988f9e").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    session = user.getSessions().stream()
        .filter(s -> isNull(s.getEnquiryMessageDate()))
        .findFirst()
        .orElseThrow();
  }

  private void givenAValidSession() {
    session = sessionRepository.findById(1L).orElseThrow();
  }

  private void givenAUserWithSessions() {
    user = userRepository.findById("9c4057d0-05ad-4e86-a47c-dc5bdeec03b9").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUser.getRoles()).thenReturn(Set.of("user"));
  }

  private void givenAConsultantWithSessions() {
    consultant = consultantRepository.findById("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")
        .orElseThrow();
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
    when(authenticatedUser.getRoles()).thenReturn(Set.of("consultant"));
  }

  private void givenAnEnquiryMessageDto(boolean isLanguageSet) {
    enquiryMessageDTO = easyRandom.nextObject(EnquiryMessageDTO.class);
    if (!isLanguageSet) {
      enquiryMessageDTO.setLanguage(null);
    }
  }

  private void givenEnabledE2EEncryption() {
    videoChatConfig.setE2eEncryptionEnabled(true);
  }

  private void givenDisplayNameAllowedForConsultants() {
    identityConfig.setDisplayNameAllowedForConsultants(true);
  }

  private void restoreSession() {
    session.setEnquiryMessageDate(null);
    sessionRepository.save(session);
  }
}
