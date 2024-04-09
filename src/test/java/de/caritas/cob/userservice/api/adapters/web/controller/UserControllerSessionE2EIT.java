package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID_HEADER_PARAMETER_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.MessageType;
import de.caritas.cob.userservice.api.config.VideoChatConfig;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.facade.userdata.ConsultantDataFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.CustomLocalDateTime;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
class UserControllerSessionE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UsernameTranscoder usernameTranscoder;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private SessionRepository sessionRepository;

  @Autowired private ChatRepository chatRepository;

  @Autowired private ChatAgencyRepository chatAgencyRepository;

  @Autowired private UserAgencyRepository userAgencyRepository;

  @Autowired private VideoChatConfig videoChatConfig;

  @Autowired private IdentityConfig identityConfig;

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @TestConfiguration
  static class TestConfig {
    @Bean(name = "initializeFeedbackChat")
    public Boolean initializeFeedbackChat() {
      return false;
    }
  }

  @SuppressWarnings("unused")
  @MockBean
  private ConsultantDataFacade consultantDataFacade;

  @Autowired private MongoClient mockedMongoClient;

  @Mock private MongoDatabase mongoDatabase;

  @Mock private MongoCollection<Document> mongoCollection;

  @Mock private MongoCursor<Document> mongoCursor;

  @Mock private FindIterable<Document> findIterable;

  @MockBean
  @Qualifier("restTemplate")
  private RestTemplate restTemplate;

  @MockBean
  @Qualifier("rocketChatRestTemplate")
  private RestTemplate rocketChatRestTemplate;

  @MockBean private Keycloak keycloak;

  @MockBean private AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @Captor private ArgumentCaptor<RequestEntity<Object>> requestCaptor;

  private User user;
  private Consultant consultant;
  private Session session;
  private boolean deleteSession;
  private EnquiryMessageDTO enquiryMessageDTO;
  private List<ConsultantAgency> consultantAgencies = new ArrayList<>();
  private Chat chat;
  private ChatAgency chatAgency;
  private UserAgency userAgency;
  private UserInfoResponseDTO userInfoResponse;
  private SubscriptionsGetDTO subscriptionsGetResponse;
  private Consultant consultantToAssign;

  @AfterEach
  void reset() {
    if (nonNull(user)) {
      user.setDeleteDate(null);
      userRepository.save(user);
      user = null;
    }
    if (nonNull(session)) {
      if (deleteSession) {
        sessionRepository.delete(session);
        deleteSession = false;
      } else {
        sessionRepository.save(session);
      }
      session = null;
    }
    consultant = null;
    enquiryMessageDTO = null;
    consultantAgencyRepository.deleteAll(consultantAgencies);
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
    userInfoResponse = null;
    subscriptionsGetResponse = null;
    identityConfig.setDisplayNameAllowedForConsultants(false);
    consultantToAssign = null;
  }

  @BeforeEach
  public void setUp() {
    when(agencyServiceApiControllerFactory.createControllerApi())
        .thenReturn(
            new TestAgencyControllerApi(
                new de.caritas.cob.userservice.agencyserivce.generated.ApiClient()));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void createEnquiryMessageWithLanguageShouldSaveLanguageAndRespondWithCreated() throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatTechUserResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(true);
    givenASuccessfulMessageResponse(null);

    mockMvc
        .perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")))
        .andExpect(jsonPath("t", is(nullValue())));

    var savedSession = sessionRepository.findById(session.getId());
    assertTrue(savedSession.isPresent());
    assertEquals(
        LanguageCode.getByCode(enquiryMessageDTO.getLanguage().getValue()),
        savedSession.get().getLanguageCode());

    restoreSession();
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void createEnquiryMessageWithoutLanguageShouldSaveDefaultLanguageAndRespondWithCreated()
      throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatTechUserResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(false);
    givenASuccessfulMessageResponse(null);

    mockMvc
        .perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")))
        .andExpect(jsonPath("t", is(nullValue())));

    var savedSession = sessionRepository.findById(session.getId());
    assertTrue(savedSession.isPresent());
    assertEquals(LanguageCode.de, savedSession.get().getLanguageCode());

    restoreSession();
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void createEnquiryMessageWithShouldReturnIfMessageWasSentWithE2Ee() throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatTechUserResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(false);
    givenASuccessfulMessageResponse("e2e");

    mockMvc
        .perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")))
        .andExpect(jsonPath("t", is("e2e")));

    restoreSession();
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getSessionsForAuthenticatedConsultantShouldReturnGroupChats() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenAValidChat();
    givenAnEmptyRocketChatGetSubscriptionsResponse();
    givenAValidRocketChatGetRoomsResponse(null, null, "A message");

    mockMvc
        .perform(
            get("/users/sessions/consultants")
                .queryParam("status", "2")
                .queryParam("count", "15")
                .queryParam("filter", "all")
                .queryParam("offset", "0")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].chat.groupId", is(chat.getGroupId())))
        .andExpect(jsonPath("sessions[0].session", is(nullValue())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getSessionsForAuthenticatedConsultantShouldReturnSessionsLastMessageTypeE2eeActivated()
      throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenASessionInProgress();
    givenAValidRocketChatGetRoomsResponse(session.getGroupId(), MessageType.E2EE_ACTIVATED, null);
    givenAnEmptyRocketChatGetSubscriptionsResponse();

    mockMvc
        .perform(
            get("/users/sessions/consultants")
                .queryParam("status", "2")
                .queryParam("count", "15")
                .queryParam("filter", "all")
                .queryParam("offset", "0")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.lastMessageType", is("E2EE_ACTIVATED")))
        .andExpect(jsonPath("sessions[0].session.lastMessage", is(emptyString())))
        .andExpect(jsonPath("sessions[0].chat", is(nullValue())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getSessionsForAuthenticatedConsultantShouldReturnInformationAboutDeletedUsers()
      throws Exception {
    givenADeletedUser(false);
    givenAValidConsultant(true);
    givenASessionInProgress();
    givenAValidRocketChatGetRoomsResponse(session.getGroupId(), MessageType.E2EE_ACTIVATED, null);
    givenAnEmptyRocketChatGetSubscriptionsResponse();

    mockMvc
        .perform(
            get("/users/sessions/consultants")
                .queryParam("status", "2")
                .queryParam("count", "15")
                .queryParam("filter", "all")
                .queryParam("offset", "0")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.lastMessageType", is("E2EE_ACTIVATED")))
        .andExpect(jsonPath("sessions[0].session.lastMessage", is(emptyString())))
        .andExpect(jsonPath("sessions[0].chat", is(nullValue())))
        .andExpect(jsonPath("sessions[0].user.deleted", is(true)));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getSessionsForAuthenticatedConsultantShouldReturnSessionsLastMessageTypeFurtherSteps()
      throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenASessionInProgress();
    givenAValidRocketChatGetRoomsResponse(
        session.getGroupId(), MessageType.FURTHER_STEPS, "A message");
    givenAnEmptyRocketChatGetSubscriptionsResponse();

    mockMvc
        .perform(
            get("/users/sessions/consultants")
                .queryParam("status", "2")
                .queryParam("count", "15")
                .queryParam("filter", "all")
                .queryParam("offset", "0")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.lastMessageType", is("FURTHER_STEPS")))
        .andExpect(jsonPath("sessions[0].session.lastMessage", is("So geht es weiter")))
        .andExpect(jsonPath("sessions[0].chat", is(nullValue())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getSessionsForAuthenticatedConsultantShouldNotReturnTeamSessions() throws Exception {
    givenAValidUser();
    givenAValidConsultant(true);
    givenATeamSessionOfAColleagueInProgress();
    givenAnEmptyRocketChatGetSubscriptionsResponse();
    givenAValidRocketChatGetRoomsResponse(null, null, "A message");

    mockMvc
        .perform(
            get("/users/sessions/consultants")
                .queryParam("status", "2")
                .queryParam("count", "15")
                .queryParam("filter", "all")
                .queryParam("offset", "0")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  void getSessionsForAuthenticatedUserShouldReturnSessionsLastMessageTypeE2eeActivated()
      throws Exception {
    givenAValidUser(true);
    givenAValidConsultant();
    givenASessionInProgress();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatGetRoomsResponse(session.getGroupId(), MessageType.E2EE_ACTIVATED, null);
    givenAnEmptyRocketChatGetSubscriptionsResponse();
    user.getSessions()
        .forEach(session -> givenAValidRocketChatInfoUserResponse(session.getConsultant()));

    mockMvc
        .perform(
            get("/users/sessions/askers")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions", hasSize(2)))
        .andExpect(
            jsonPath(
                "sessions[*].session.lastMessageType",
                containsInAnyOrder("E2EE_ACTIVATED", "FURTHER_STEPS")))
        .andExpect(jsonPath("sessions[0].chat", is(nullValue())))
        .andExpect(jsonPath("sessions[1].chat", is(nullValue())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  void getSessionsForAuthenticatedUserShouldReturnSessionsLastMessageTypeResetLastMessage()
      throws Exception {
    givenAValidUser(true);
    givenAValidConsultant();
    givenASessionInProgress();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatGetRoomsResponse(
        session.getGroupId(), MessageType.REASSIGN_CONSULTANT_RESET_LAST_MESSAGE, null);
    givenAnEmptyRocketChatGetSubscriptionsResponse();
    user.getSessions()
        .forEach(session -> givenAValidRocketChatInfoUserResponse(session.getConsultant()));

    mockMvc
        .perform(
            get("/users/sessions/askers")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions", hasSize(2)))
        .andExpect(
            jsonPath(
                "sessions[*].session.lastMessageType",
                containsInAnyOrder("REASSIGN_CONSULTANT_RESET_LAST_MESSAGE", "FURTHER_STEPS")))
        .andExpect(jsonPath("sessions[0].chat", is(nullValue())))
        .andExpect(jsonPath("sessions[1].chat", is(nullValue())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  void getSessionsForAuthenticatedUserShouldReturnSessionsLastMessageTypeReassignConsultant()
      throws Exception {
    givenAValidUser(true);
    givenAValidConsultant();
    givenASessionInProgress();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatGetRoomsResponse(
        session.getGroupId(), MessageType.REASSIGN_CONSULTANT, "a message");
    givenAnEmptyRocketChatGetSubscriptionsResponse();
    user.getSessions()
        .forEach(session -> givenAValidRocketChatInfoUserResponse(session.getConsultant()));

    mockMvc
        .perform(
            get("/users/sessions/askers")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions", hasSize(2)))
        .andExpect(
            jsonPath(
                "sessions[*].session.lastMessageType",
                containsInAnyOrder("REASSIGN_CONSULTANT", "FURTHER_STEPS")))
        .andExpect(jsonPath("sessions[0].chat", is(nullValue())))
        .andExpect(jsonPath("sessions[1].chat", is(nullValue())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getSessionsForGroupOrFeedbackGroupIdsShouldFindSessionsByGroupOrFeedbackGroup()
      throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=mzAdWzQEobJ2PkoxP,9faSTWZ5gurHLXy4R")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.feedbackGroupId", is("9faSTWZ5gurHLXy4R")))
        .andExpect(jsonPath("sessions[1].session.groupId", is("mzAdWzQEobJ2PkoxP")))
        .andExpect(jsonPath("sessions", hasSize(2)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getSessionsForGroupOrFeedbackGroupIdsShouldFindSessionsByGroupOrFeedbackGroupForConsultant()
      throws Exception {
    givenAConsultantWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=YWKxhFX5K2HPpsFbs,4SPkApB8So88c7tQ3")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("YWKxhFX5K2HPpsFbs")))
        .andExpect(jsonPath("sessions[0].session.feedbackRead", is(true)))
        .andExpect(jsonPath("sessions[0].user.username", is("u25suchtler")))
        .andExpect(
            jsonPath("sessions[0].consultant.id", is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")))
        .andExpect(jsonPath("sessions[0].consultant.firstName", is("Manfred")))
        .andExpect(jsonPath("sessions[0].consultant.lastName", is("Main")))
        .andExpect(jsonPath("sessions[0].consultant.username").doesNotExist())
        .andExpect(jsonPath("sessions[1].session.feedbackGroupId", is("4SPkApB8So88c7tQ3")))
        .andExpect(jsonPath("sessions[1].session.feedbackRead", is(true)))
        .andExpect(jsonPath("sessions[1].user.username", is("u25depp")))
        .andExpect(
            jsonPath("sessions[1].consultant.id", is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")))
        .andExpect(jsonPath("sessions[1].consultant.firstName", is("Manfred")))
        .andExpect(jsonPath("sessions[1].consultant.lastName", is("Main")))
        .andExpect(jsonPath("sessions[1].consultant.username").doesNotExist())
        .andExpect(jsonPath("sessions", hasSize(2)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getSessionsForGroupOrFeedbackGroupIdsShouldFindSessionByGroupId() throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=mzAdWzQEobJ2PkoxP")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("mzAdWzQEobJ2PkoxP")))
        .andExpect(jsonPath("sessions[0].agency", is(notNullValue())))
        .andExpect(jsonPath("sessions", hasSize(1)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getSessionsForGroupOrFeedbackGroupIdsShouldContainConsultantOfUserSession()
      throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=YWKxhFX5K2HPpsFbs")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("YWKxhFX5K2HPpsFbs")))
        .andExpect(jsonPath("sessions[0].consultant.username", is("u25main")))
        .andExpect(
            jsonPath("sessions[0].consultant.id", is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")))
        .andExpect(jsonPath("sessions[0].consultant.firstName").doesNotExist())
        .andExpect(jsonPath("sessions[0].consultant.lastName").doesNotExist())
        .andExpect(jsonPath("sessions", hasSize(1)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getSessionsForGroupOrFeedbackGroupIdsShouldBeForbiddenIfUserDoesNotParticipateInSession()
      throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=4SPkApB8So88c7tQ3")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  @ValueSource(strings = {"QBv2xym9qQ2DoAxkR", "doesNotExist", "mzAdWzQEobJ2PkoxP"})
  void
      getSessionsForGroupOrFeedbackGroupIdsShouldBeNoContentIfConsultantDoesNotParticipateInSessionOrNoSessionsFoundForIdsOrNewEnquiriesForConsultantsNotInAgency(
          String rcGroupId) throws Exception {
    givenAConsultantWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=" + rcGroupId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void
      getSessionsForGroupOrFeedbackGroupIdsShouldReturnSessionsForNewEnquiriesOfConsultantInAgency()
          throws Exception {
    givenAConsultantWithSessionsOfNewEnquiries();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room?rcGroupIds=XJrRTzFg8Ac5BwE86")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("XJrRTzFg8Ac5BwE86")))
        .andExpect(jsonPath("sessions", hasSize(1)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getSessionForIdShouldFindSessionsBySessionId() throws Exception {
    givenAUserWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room/900")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.id", is(900)))
        .andExpect(jsonPath("sessions[0].session.groupId", is("YWKxhFX5K2HPpsFbs")))
        .andExpect(jsonPath("sessions[0].consultant.username", is("u25main")))
        .andExpect(
            jsonPath("sessions[0].consultant.id", is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")))
        .andExpect(jsonPath("sessions[0].consultant.firstName").doesNotExist())
        .andExpect(jsonPath("sessions[0].consultant.lastName").doesNotExist())
        .andExpect(jsonPath("sessions", hasSize(1)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getSessionForIdShouldFindSessionsBySessionIdForConsultant() throws Exception {
    givenAConsultantWithSessions();
    givenNoRocketChatSubscriptionUpdates();
    givenNoRocketChatRoomUpdates();

    mockMvc
        .perform(
            get("/users/sessions/room/900")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].session.groupId", is("YWKxhFX5K2HPpsFbs")))
        .andExpect(jsonPath("sessions[0].session.feedbackRead", is(true)))
        .andExpect(jsonPath("sessions[0].user.username", is("u25suchtler")))
        .andExpect(
            jsonPath("sessions[0].consultant.id", is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")))
        .andExpect(jsonPath("sessions[0].consultant.firstName", is("Manfred")))
        .andExpect(jsonPath("sessions[0].consultant.lastName", is("Main")))
        .andExpect(jsonPath("sessions[0].consultant.username").doesNotExist())
        .andExpect(jsonPath("sessions", hasSize(1)));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnForbiddenIfSessionIdFormatIsInvalid() throws Exception {
    givenAValidConsultant(true);
    var sessionId = RandomStringUtils.randomAlphabetic(8);

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    sessionId,
                    consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnBadRequestIfConsultantIdFormatIsInvalid() throws Exception {
    var consultantId = RandomStringUtils.randomAlphanumeric(8);

    mockMvc
        .perform(
            delete("/users/sessions/1/consultant/{consultantId}", consultantId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnNotFoundIfConsultantDoesNotExist() throws Exception {
    givenAValidSession();

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    UUID.randomUUID().toString())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnNotFoundIfSessionDoesNotExist() throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    RandomStringUtils.randomNumeric(5, 6),
                    consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnNoContentAndIgnoreRemovalIfNotInChat(CapturedOutput logOutput)
      throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenAValidSession();
    givenOnlyEmptyRocketChatGroupMemberResponses();
    givenKeycloakUserRoles(consultant.getId(), "consultant");

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getGroupId(), session.getConsultant().getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getGroupId(), 0);

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getFeedbackGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getFeedbackGroupId(), consultant.getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getFeedbackGroupId(), 0);
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnNoContentAndIgnoreRemovalIfNotTeaming(CapturedOutput logOutput)
      throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenAValidSession();
    givenOnlyEmptyRocketChatGroupMemberResponses();
    givenKeycloakUserRoles(consultant.getId(), "consultant");

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getGroupId(), session.getConsultant().getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getGroupId(), 0);

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getFeedbackGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getFeedbackGroupId(), consultant.getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getFeedbackGroupId(), 0);
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnNoContentAndRemoveConsultantFromSessionNotFromFeedbackChat(
      CapturedOutput logOutput) throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenAValidSession();
    var doc = givenSubscription(consultant.getRocketChatId(), "c", null);
    givenMongoResponseWith(doc);
    givenKeycloakUserRoles(consultant.getId(), "consultant");

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getGroupId(), 1);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getGroupId(), consultant.getRocketChatId(), 1);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getGroupId(), 1);

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getFeedbackGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getFeedbackGroupId(), consultant.getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getFeedbackGroupId(), 0);
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void removeFromSessionShouldReturnNoContentAndIgnoreRemovalIfAssigned(CapturedOutput logOutput)
      throws Exception {
    givenAValidConsultant(true);
    givenAValidSession();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse(session.getConsultant());
    givenKeycloakUserRoles(session.getConsultant().getId(), "consultant");

    mockMvc
        .perform(
            delete(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    session.getConsultant().getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getGroupId(), session.getConsultant().getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getGroupId(), 0);

    verifyRocketChatTechUserAddedToGroup(logOutput, session.getFeedbackGroupId(), 0);
    verifyRocketChatUserRemovedFromGroup(
        logOutput, session.getFeedbackGroupId(), consultant.getRocketChatId(), 0);
    verifyRocketChatTechUserLeftGroup(logOutput, session.getFeedbackGroupId(), 0);
  }

  @Test
  @SuppressWarnings("java:S2925") // "Thread.sleep" should not be used in tests
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void assignSessionShouldReturnOkAndAssignWhenRequestedByConsultant(CapturedOutput logOutput)
      throws Exception {
    givenAValidConsultant(true);
    givenAValidRocketChatSystemUser();
    givenValidRocketChatTechUserResponse();
    givenAValidSession();
    givenOnlyEmptyRocketChatGroupMemberResponses();
    givenAConsultantOfSameAgencyToAssignTo();

    var previousConsultant = session.getConsultant();
    assertNotEquals(consultantToAssign, previousConsultant);

    mockMvc
        .perform(
            put(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    consultantToAssign.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var updatedSession = sessionRepository.findById(session.getId()).orElseThrow();
    assertEquals(consultantToAssign, updatedSession.getConsultant());
    session.setConsultant(previousConsultant);

    TimeUnit.SECONDS.sleep(1); // wait for logging thread
    var out = logOutput.getOut();
    assertTrue(out.contains("Sending 1 emails"));

    var firstEmailLog =
        "Sending assign-enquiry-notification email to " + consultantToAssign.getEmail();
    int numFirstEmail = StringUtils.countOccurrencesOf(out, firstEmailLog);
    assertEquals(1, numFirstEmail);
  }

  @Test
  @SuppressWarnings("java:S2925") // "Thread.sleep" should not be used in tests
  @WithMockUser(authorities = AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)
  void assignSessionShouldReturnOkAndAssignWhenRequestedByAdviceSeeker(CapturedOutput logOutput)
      throws Exception {
    givenAValidUser(true);
    givenAValidConsultant();
    givenAValidRocketChatSystemUser();
    givenValidRocketChatTechUserResponse();
    givenAValidSession();
    givenOnlyEmptyRocketChatGroupMemberResponses();

    var previousConsultant = session.getConsultant();
    assertNotEquals(consultant, previousConsultant);

    mockMvc
        .perform(
            put(
                    "/users/sessions/{sessionId}/consultant/{consultantId}",
                    session.getId(),
                    consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var updatedSession = sessionRepository.findById(session.getId()).orElseThrow();
    assertEquals(consultant, updatedSession.getConsultant());
    session.setConsultant(previousConsultant);

    TimeUnit.SECONDS.sleep(1); // wait for logging thread
    var out = logOutput.getOut();
    assertFalse(out.contains("Sending 1 emails"));
  }

  private void givenKeycloakUserRoles(String userId, String... roles) {
    var realmResource = mock(RealmResource.class);
    var usersResource = mock(UsersResource.class);
    var userResource = mock(UserResource.class);
    var roleMappingResource = mock(RoleMappingResource.class);
    var roleScopeResource = mock(RoleScopeResource.class);
    var roleRepresentationList =
        Arrays.stream(roles)
            .map(role -> new RoleRepresentation(role, "", false))
            .collect(Collectors.toList());
    when(roleScopeResource.listAll()).thenReturn(roleRepresentationList);
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(usersResource.get(userId)).thenReturn(userResource);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenAValidRocketChatInfoUserResponse() {
    givenAValidRocketChatInfoUserResponse(consultant);
  }

  private void givenAValidRocketChatInfoUserResponse(Consultant consultant) {
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
            any(HttpEntity.class), eq(UserInfoResponseDTO.class)))
        .thenReturn(ResponseEntity.ok(userInfoResponse));
  }

  private void givenOnlyEmptyRocketChatGroupMemberResponses() {
    givenMongoResponseWith(null);
  }

  @SuppressWarnings("SameParameterValue")
  private void givenMongoResponseWith(Document doc, Document... docs) {
    if (nonNull(doc)) {
      when(mongoCursor.next()).thenReturn(doc, docs);
    }
    var booleanList = new LinkedList<Boolean>();
    var numExtraDocs = docs.length;
    while (numExtraDocs-- > 0) {
      booleanList.add(true);
    }
    booleanList.add(false);
    if (nonNull(doc)) {
      when(mongoCursor.hasNext()).thenReturn(true, booleanList.toArray(new Boolean[0]));
    } else {
      when(mongoCursor.hasNext()).thenReturn(false);
    }
    when(findIterable.iterator()).thenReturn(mongoCursor);
    when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable).thenReturn(findIterable);
    when(mockedMongoClient.getDatabase("rocketchat")).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection("rocketchat_subscription")).thenReturn(mongoCollection);
  }

  @SuppressWarnings("SameParameterValue")
  private Document givenSubscription(String chatUserId, String username, String name)
      throws JsonProcessingException {
    var doc = new LinkedHashMap<String, Object>();
    doc.put("_id", RandomStringUtils.randomAlphanumeric(17));
    doc.put("rid", RandomStringUtils.randomAlphanumeric(17));
    doc.put("name", RandomStringUtils.randomAlphanumeric(17));

    var user = new LinkedHashMap<>();
    user.put("_id", chatUserId);
    user.put("username", username);
    if (nonNull(name)) {
      user.put("name", name);
    }

    doc.put("u", user);

    var json = objectMapper.writeValueAsString(doc);

    return Document.parse(json);
  }

  private void givenAnEmptyRocketChatGetSubscriptionsResponse() {
    subscriptionsGetResponse = new SubscriptionsGetDTO();
    subscriptionsGetResponse.setSuccess(true);
    SubscriptionsUpdateDTO[] s = {};
    subscriptionsGetResponse.setUpdate(s);

    var urlSuffix = "/api/v1/subscriptions.get";
    when(restTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.GET),
            any(HttpEntity.class), eq(SubscriptionsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(subscriptionsGetResponse));
  }

  private void givenAValidConsultant() {
    givenAValidConsultant(false);
  }

  private void givenAValidConsultant(boolean isAuthUser) {
    consultant = consultantRepository.findAll().iterator().next();
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
      when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
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
    setAuthUserAttributes(isAuthUser);
  }

  private void givenADeletedUser(boolean isAuthUser) {
    user = userRepository.findAll().iterator().next();
    user.setDeleteDate(LocalDateTime.now());
    userRepository.save(user);
    setAuthUserAttributes(isAuthUser);
  }

  private void setAuthUserAttributes(boolean isAuthUser) {
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
      when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
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
    chat.setUpdateDate(CustomLocalDateTime.nowInUtc());
    chatRepository.save(chat);

    var agencyId = consultant.getConsultantAgencies().iterator().next().getAgencyId();
    chatAgency = new ChatAgency();
    chatAgency.setChat(chat);
    chatAgency.setAgencyId(agencyId);
    chatAgencyRepository.save(chatAgency);

    if (nonNull(user)) {
      userAgency = new UserAgency();
      userAgency.setUser(user);
      userAgency.setAgencyId(agencyId);
      user.getUserAgencies().add(userAgency);
      userAgencyRepository.save(userAgency);
    }
  }

  private void givenAValidRocketChatSystemUser() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getSystemUserSneaky()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rocketChatCredentialsProvider.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
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
    when(restTemplate.exchange(
            anyString(), any(), any(), eq(UserInfoResponseDTO.class), anyString()))
        .thenReturn(userInfoResponseDTO);
  }

  private void givenAValidRocketChatGetRoomsResponse(
      String groupId, MessageType messageType, String message) {
    var updateUserResponse = easyRandom.nextObject(RoomsGetDTO.class);
    var roomsUpdateDTO = Arrays.stream(updateUserResponse.getUpdate()).findFirst().orElseThrow();
    roomsUpdateDTO.getLastMessage().setMessage(message);
    if (nonNull(messageType)) {
      var alias = new AliasMessageDTO();
      alias.setMessageType(messageType);
      roomsUpdateDTO.getLastMessage().setAlias(alias);
    }
    if (nonNull(groupId)) {
      roomsUpdateDTO.setId(groupId);
    }
    RoomsUpdateDTO[] roomsUpdateDTOs = {roomsUpdateDTO};
    updateUserResponse.setUpdate(roomsUpdateDTOs);

    final var urlSuffix = "/api/v1/rooms.get";
    when(restTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(updateUserResponse));
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
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoomsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(response));
  }

  private void givenASuccessfulMessageResponse(String messageType) {
    de.caritas.cob.userservice.messageservice.generated.web.model.MessageResponseDTO
        messageResponseDTO =
            easyRandom.nextObject(
                de.caritas.cob.userservice.messageservice.generated.web.model.MessageResponseDTO
                    .class);
    messageResponseDTO.setT(messageType);
    ResponseEntity<Object> response = ResponseEntity.status(CREATED).body(messageResponseDTO);
    when(restTemplate.exchange(
            requestCaptor.capture(),
            eq(
                ParameterizedTypeReference.forType(
                    de.caritas.cob.userservice.messageservice.generated.web.model.MessageResponseDTO
                        .class))))
        .thenReturn(response);
  }

  private void givenNoRocketChatSubscriptionUpdates() {
    var response = new SubscriptionsGetDTO();
    var subscriptionsUpdate = new SubscriptionsUpdateDTO[0];
    response.setUpdate(subscriptionsUpdate);
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SubscriptionsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(response));
  }

  private void givenAUserWithASessionNotEnquired() {
    user = userRepository.findById("552d3f10-1b6d-47ee-aec5-b88fbf988f9e").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    session =
        user.getSessions().stream()
            .filter(s -> isNull(s.getEnquiryMessageDate()))
            .findFirst()
            .orElseThrow();
  }

  private void givenATeamSessionOfAColleagueInProgress() {
    session = new Session();
    session.setUser(user);
    session.setConsultant(
        StreamSupport.stream(consultantRepository.findAll().spliterator(), false)
            .filter(c -> !c.getId().equals(consultant.getId()))
            .findFirst()
            .orElseThrow());
    session.setConsultingTypeId(1);
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setLanguageCode(LanguageCode.de);
    session.setPostcode(RandomStringUtils.randomNumeric(5));
    session.setAgencyId(consultant.getConsultantAgencies().iterator().next().getAgencyId());
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setTeamSession(true);
    session.setIsConsultantDirectlySet(false);

    sessionRepository.save(session);
    deleteSession = true;
  }

  private void givenASessionInProgress() {
    session = new Session();
    session.setUser(user);
    session.setConsultant(consultant);
    session.setConsultingTypeId(1);
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setLanguageCode(LanguageCode.de);
    session.setPostcode(RandomStringUtils.randomNumeric(5));
    session.setAgencyId(consultant.getConsultantAgencies().iterator().next().getAgencyId());
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setTeamSession(false);
    session.setCreateDate(LocalDateTime.now());
    session.setGroupId(RandomStringUtils.randomAlphabetic(17));
    session.setIsConsultantDirectlySet(false);

    sessionRepository.save(session);
    deleteSession = true;
  }

  private void givenAValidSession() {
    session = sessionRepository.findById(1L).orElseThrow();
  }

  private void givenAUserWithSessions() {
    user = userRepository.findById("9c4057d0-05ad-4e86-a47c-dc5bdeec03b9").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUser.getRoles()).thenReturn(Set.of("user"));
  }

  private void givenADeletedUserWithSessions() throws JsonProcessingException {

    user = userRepository.findById("9c4057d0-05ad-4e86-a47c-dc5bdeec03b9").orElseThrow();
    user.setDeleteDate(LocalDateTime.now());
    session =
        user.getSessions().stream()
            .filter(s -> s.getEnquiryMessageDate() != null)
            .findFirst()
            .orElseThrow();

    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUser.getRoles()).thenReturn(Set.of("user"));
  }

  private void givenAConsultantOfSameAgencyToAssignTo() {
    consultantToAssign =
        StreamSupport.stream(consultantRepository.findAll().spliterator(), true)
            .filter(c -> !c.getId().equals(consultant.getId()))
            .filter(c -> !c.getId().equals(session.getConsultant().getId()))
            .filter(c -> c.isInAgency(session.getAgencyId()))
            .findFirst()
            .orElseThrow();
  }

  private void givenAConsultantWithSessions() {
    consultant =
        consultantRepository.findById("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc").orElseThrow();
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
    when(authenticatedUser.getRoles()).thenReturn(Set.of("consultant"));
  }

  private void givenAConsultantWithSessionsOfNewEnquiries() {
    consultant =
        consultantRepository.findById("94c3e0b1-0677-4fd2-a7ea-56a71aefd0e8").orElseThrow();
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

  private void restoreSession() {
    session.setEnquiryMessageDate(null);
    sessionRepository.save(session);
  }

  private void verifyRocketChatTechUserAddedToGroup(
      CapturedOutput logOutput, String groupId, int count) {
    int occurrencesOfAddTech =
        StringUtils.countOccurrencesOf(
            logOutput.getOut(),
            "RocketChatTestConfig.addTechnicalUserToGroup(" + groupId + ") called");
    assertEquals(count, occurrencesOfAddTech);
  }

  private void verifyRocketChatTechUserLeftGroup(
      CapturedOutput logOutput, String groupId, int count) {
    int occurrencesOfTechLeaving =
        StringUtils.countOccurrencesOf(
            logOutput.getOut(),
            "RocketChatTestConfig.leaveFromGroupAsTechnicalUser(" + groupId + ") called");
    assertEquals(count, occurrencesOfTechLeaving);
  }

  private void verifyRocketChatUserRemovedFromGroup(
      CapturedOutput logOutput, String groupId, String chatUserId, int count) {
    int occurrencesOfRemoval =
        StringUtils.countOccurrencesOf(
            logOutput.getOut(),
            "RocketChatTestConfig.removeUserFromGroup(" + chatUserId + "," + groupId + ") called");
    assertEquals(count, occurrencesOfRemoval);
  }
}
