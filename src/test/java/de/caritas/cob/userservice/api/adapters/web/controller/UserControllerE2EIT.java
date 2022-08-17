package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UpdateUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailToggle;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailType;
import de.caritas.cob.userservice.api.adapters.web.dto.PasswordDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ReassignmentNotificationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.config.VideoChatConfig;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserVerifier;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpType;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.topicservice.generated.ApiClient;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.Cookie;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@TestPropertySource(properties = "feature.topics.enabled=true")
class UserControllerE2EIT {

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
  private ChatRepository chatRepository;

  @Autowired
  private ChatAgencyRepository chatAgencyRepository;

  @Autowired
  private UserAgencyRepository userAgencyRepository;

  @Autowired
  private ConsultingTypeControllerApi consultingTypeControllerApi;

  @Autowired
  private VideoChatConfig videoChatConfig;

  @Autowired
  private IdentityConfig identityConfig;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private UserVerifier userVerifier;

  @MockBean
  private AuthenticatedUser authenticatedUser;

  @MockBean
  private RocketChatCredentialsProvider rocketChatCredentialsProvider;

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

  @MockBean
  private Keycloak keycloak;

  @Captor
  private ArgumentCaptor<HttpEntity<UpdateUser>> updateUserCaptor;

  private User user;
  private Consultant consultant;
  private UpdateConsultantDTO updateConsultantDTO;
  private Set<Consultant> consultantsToReset = new HashSet<>();
  private List<ConsultantAgency> consultantAgencies = new ArrayList<>();
  private PatchUserDTO patchUserDTO;
  private UserDTO userDTO;
  private Chat chat;
  private ChatAgency chatAgency;
  private UserAgency userAgency;
  private PasswordDTO passwordDto;
  private DeleteUserAccountDTO deleteUserAccountDto;
  private UserInfoResponseDTO userInfoResponse;

  @AfterEach
  void reset() {
    if (nonNull(user)) {
      user.setDeleteDate(null);
      userRepository.save(user);
      user = null;
    }
    consultant = null;
    updateConsultantDTO = null;
    consultantsToReset.forEach(consultantToReset -> {
      consultantToReset.setLanguages(null);
      consultantToReset.setNotifyEnquiriesRepeating(true);
      consultantToReset.setNotifyNewChatMessageFromAdviceSeeker(true);
      consultantToReset.setNotifyNewFeedbackMessageFromAdviceSeeker(true);
      consultantRepository.save(consultantToReset);
    });
    consultantsToReset = new HashSet<>();
    consultantAgencyRepository.deleteAll(consultantAgencies);
    consultantAgencies = new ArrayList<>();
    patchUserDTO = null;
    userDTO = null;
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
    passwordDto = null;
    deleteUserAccountDto = null;
    userInfoResponse = null;
    identityConfig.setDisplayNameAllowedForConsultants(false);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faByAppIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant();
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
        .andExpect(jsonPath("agencies", hasSize(2)))
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
        .andExpect(jsonPath("emailToggles", hasSize(3)))
        .andExpect(jsonPath("emailToggles[*].name", containsInAnyOrder(
            "DAILY_ENQUIRY",
            "NEW_CHAT_MESSAGE_FROM_ADVICE_SEEKER",
            "NEW_FEEDBACK_MESSAGE_FROM_ADVICE_SEEKER"
        )))
        .andExpect(jsonPath("emailToggles[0].state", is(true)))
        .andExpect(jsonPath("emailToggles[1].state", is(true)))
        .andExpect(jsonPath("emailToggles[2].state", is(true)))
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faByAppIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidUser();
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
        .andExpect(jsonPath("emailToggles", is(nullValue())))
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faByEmailIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant();
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
        .andExpect(jsonPath("agencies", hasSize(2)))
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
        .andExpect(jsonPath("emailToggles", hasSize(3)))
        .andExpect(jsonPath("emailToggles[*].name", containsInAnyOrder(
            "DAILY_ENQUIRY",
            "NEW_CHAT_MESSAGE_FROM_ADVICE_SEEKER",
            "NEW_FEEDBACK_MESSAGE_FROM_ADVICE_SEEKER"
        )))
        .andExpect(jsonPath("emailToggles[0].state", is(true)))
        .andExpect(jsonPath("emailToggles[1].state", is(true)))
        .andExpect(jsonPath("emailToggles[2].state", is(true)))
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faByEmailIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidUser();
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
        .andExpect(jsonPath("emailToggles", is(nullValue())))
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getSessionsForAuthenticatedConsultant_ShouldGetSessionsWithTopics()
      throws Exception {
    givenABearerToken();
    givenAValidConsultantWithId("34c3x5b1-0677-4fd2-a7ea-56a71aefd099");
    givenConsultingTypeServiceResponse();
    givenAValidRocketChatInfoUserResponse();
    givenAValidRocketChatSubscriptionsResponse();
    givenAValidRocketChatRoomsResponse();
    givenAValidTopicServiceResponse();

    mockMvc.perform(
        get("/users/sessions/consultants?status=2&count=15&filter=all&offset=0")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header("rcToken", RC_TOKEN)

            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("offset", is(0)))
        .andExpect(jsonPath("count", is(2)))
        .andExpect(jsonPath("total", is(2)))
        .andExpect(jsonPath("sessions", hasSize(2)))
        .andExpect(jsonPath("sessions[0].session.id", is(1215)))
        .andExpect(jsonPath("sessions[0].session.agencyId", is(1)))
        .andExpect(jsonPath("sessions[0].session.topic.id", is(1)))
        .andExpect(jsonPath("sessions[0].session.topic.name", is("topic name")))
        .andExpect(jsonPath("sessions[0].session.topic.description", is("topic desc")))
        .andExpect(jsonPath("sessions[0].session.topic.status", is("INACTIVE")));
  }


  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faIsNotActivated()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant();
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
        .andExpect(jsonPath("agencies", hasSize(2)))
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
        .andExpect(jsonPath("preferredLanguage", is(consultant.getLanguageCode().toString())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("emailToggles", hasSize(3)))
        .andExpect(jsonPath("emailToggles[*].name", containsInAnyOrder(
            "DAILY_ENQUIRY",
            "NEW_CHAT_MESSAGE_FROM_ADVICE_SEEKER",
            "NEW_FEEDBACK_MESSAGE_FROM_ADVICE_SEEKER"
        )))
        .andExpect(jsonPath("emailToggles[0].state", is(true)))
        .andExpect(jsonPath("emailToggles[1].state", is(true)))
        .andExpect(jsonPath("emailToggles[2].state", is(true)))
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faIsNotActivated()
      throws Exception {
    givenABearerToken();
    givenAValidUser();
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
        .andExpect(jsonPath("preferredLanguage", is(user.getLanguageCode().toString())))
        .andExpect(jsonPath("e2eEncryptionEnabled", is(false)))
        .andExpect(jsonPath("emailToggles", is(nullValue())))
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getUserDataShouldContainSetFlags() throws Exception {
    givenABearerToken();
    givenAValidConsultant();
    givenConsultingTypeServiceResponse();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenKeycloakRespondsOtpHasNotBeenSetup(consultant.getUsername());
    givenEnabledE2EEncryption();
    givenDisplayNameAllowedForConsultants();
    givenConsultantIsNotToNotifyAboutNewEnquiries();

    mockMvc.perform(
        get("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("emailToggles", hasSize(3)))
        .andExpect(jsonPath("emailToggles[?(@.name =~ /DAILY_ENQUIRY/)].state",
            is(List.of(false)))
        )
        .andExpect(jsonPath("emailToggles[?(@.name =~ /NEW_.*_MESSAGE_FROM_ADVICE_SEEKER/)].state",
            is(List.of(true, true)))
        )
        .andExpect(jsonPath("e2eEncryptionEnabled", is(true)))
        .andExpect(jsonPath("isDisplayNameEditable", is(true)));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getUserDataShouldContainDisabledFollowUpEmails() throws Exception {
    givenABearerToken();
    givenAValidConsultant();
    givenConsultingTypeServiceResponse();
    givenAValidRocketChatSystemUser();
    givenAValidRocketChatInfoUserResponse();
    givenKeycloakRespondsOtpHasNotBeenSetup(consultant.getUsername());
    givenEnabledE2EEncryption();
    givenDisplayNameAllowedForConsultants();
    givenConsultantIsNotToNotifyAboutNewFollowUps();

    mockMvc.perform(
        get("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("emailToggles", hasSize(3)))
        .andExpect(jsonPath("emailToggles[?(@.name =~ /DAILY_ENQUIRY/)].state",
            is(List.of(true)))
        )
        .andExpect(jsonPath("emailToggles[?(@.name =~ /NEW_.*_MESSAGE_FROM_ADVICE_SEEKER/)].state",
            is(List.of(false, false)))
        );
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  void patchUserDataShouldSaveAdviceSeekerAndRespondWithNoContent() throws Exception {
    givenAValidUser();
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

    var savedUser = userRepository.findById(user.getUserId()).orElseThrow();
    assertEquals(patchUserDTO.getEncourage2fa(), savedUser.getEncourage2fa());
    assertEquals(patchUserDTO.getPreferredLanguage().getValue(),
        savedUser.getLanguageCode().toString());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void patchUserDataShouldSaveConsultantAndRespondWithNoContent() throws Exception {
    givenAValidConsultant();
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

    var savedConsultant = consultantRepository
        .findById(consultant.getId())
        .orElseThrow();
    assertEquals(patchUserDTO.getEncourage2fa(), savedConsultant.getEncourage2fa());
    assertEquals(patchUserDTO.getPreferredLanguage().toString(),
        savedConsultant.getLanguageCode().toString());

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
  void patchUserDataShouldOverrideDefaultAndRespondWithNoContent() throws Exception {
    givenAValidConsultant();
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
  void patchUserDataShouldOverridePreviousValueAndRespondWithNoContentEachTime()
      throws Exception {
    givenAValidConsultant();

    var savedConsultant = consultantRepository.findById(consultant.getId()).orElseThrow();
    assertEquals(true, savedConsultant.getEncourage2fa());
    assertEquals("de", savedConsultant.getLanguageCode().toString());

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

    savedConsultant = consultantRepository.findById(consultant.getId()).orElseThrow();
    assertEquals(false, savedConsultant.getEncourage2fa());
    assertEquals(patchUserDTO.getPreferredLanguage().toString(),
        savedConsultant.getLanguageCode().toString());

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

    savedConsultant = consultantRepository.findById(consultant.getId()).orElseThrow();
    assertEquals(true, savedConsultant.getEncourage2fa());
    assertEquals(patchUserDTO.getPreferredLanguage().toString(),
        savedConsultant.getLanguageCode().toString());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void patchUserDataShouldRespondWithBadRequestOnNullInMandatoryDtoFields()
      throws Exception {
    givenAValidConsultant();
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
  void patchUserDataShouldRespondWithBadRequestOnEmptyPayload() throws Exception {
    givenAValidUser();
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
  void patchUserDataShouldRespondWithNoContentOnPartialPayload() throws Exception {
    givenAValidUser();
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
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void patchUserDataShouldRespondWithBadRequestOnUnknownEmailToggle() throws Exception {
    givenAValidConsultant();
    var patchDtoJson = givenAnUnknownEmailTypeTogglePatchDto();

    mockMvc.perform(
        patch("/users/data")
            .cookie(CSRF_COOKIE)
            .cookie(RC_TOKEN_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(patchDtoJson)
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void patchUserDataShouldRespondWithBadRequestOnUnknownPreferredLanguage() throws Exception {
    givenAValidConsultant();
    var patchDtoMap = givenAnUnknownPreferredLanguagePatchDto();

    mockMvc.perform(
        patch("/users/data")
            .cookie(CSRF_COOKIE)
            .cookie(RC_TOKEN_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patchDtoMap))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void patchUserDataShouldRespondWithNoContentOnEmailToggleAndChangeDbConsultant()
      throws Exception {
    givenAValidConsultant();
    var patchDto = givenAValidEmailTogglePatchDto(false);

    mockMvc.perform(
        patch("/users/data")
            .cookie(CSRF_COOKIE)
            .cookie(RC_TOKEN_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patchDto))
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNoContent());

    var dbConsultant = consultantRepository.findById(consultant.getId()).orElseThrow();
    assertFalse(dbConsultant.getNotifyEnquiriesRepeating());
    assertFalse(dbConsultant.getNotifyNewChatMessageFromAdviceSeeker());
    assertFalse(dbConsultant.getNotifyNewFeedbackMessageFromAdviceSeeker());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  void deactivateAndFlagUserAccountForDeletionShouldDeactivateAndRespondWithOkIf2faIsOff()
      throws Exception {
    givenAValidUser();
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
  void deactivateAndFlagUserAccountForDeletionShouldDeactivateAndRespondWithOkIf2faIsOn()
      throws Exception {
    givenAValidUser();
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
  void deactivateAndFlagUserAccountForDeletionShouldRespondWithBadRequestIfPasswordIsFalse()
      throws Exception {
    givenAValidUser();
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
  void updatePasswordShouldUpdatePasswordAndRespondWithOkIf2faIsOff() throws Exception {
    givenAValidUser();
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
  void updatePasswordShouldUpdatePasswordAndRespondWithOkIf2faIsOn() throws Exception {
    givenAValidUser();
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
  void updatePasswordShouldRespondWithBadRequestIfPasswordIsFalse() throws Exception {
    givenAValidUser();
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
  void updateUserDataShouldSaveDefaultLanguageAndRespondWithOk() throws Exception {
    givenAValidConsultant();
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
  void updateUserDataShouldSaveGivenLanguagesAndRespondWithOk() throws Exception {
    givenAValidConsultant();
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
  void updateUserDataShouldCascadeLanguageDeletionAndRespondWithOk() throws Exception {
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
  void registerUserWithoutConsultingIdShouldSaveMonitoring() throws Exception {
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

  @Test
  void registerUserWithoutConsultingIdShouldSaveCreateUserWithDemographicsData() throws Exception {
    ReflectionTestUtils
        .setField(userVerifier, "demographicsFeatureEnabled", true);
    givenConsultingTypeServiceResponse(2);
    givenARealmResource();
    givenAUserDTOWithDemographics();

    mockMvc.perform(
            post("/users/askers/new")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
        .andExpect(status().isCreated());

    ReflectionTestUtils
        .setField(userVerifier, "demographicsFeatureEnabled", false);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  void sendReassignmentNotificationShouldSendEmailAndRespondWithOk() throws Exception {
    var apiClientMock = mock(de.caritas.cob.userservice.mailservice.generated.ApiClient.class);
    when(mailsControllerApi.getApiClient()).thenReturn(apiClientMock);
    var session = givenAExistingSession();
    var assignemtNotification = new ReassignmentNotificationDTO()
        .toConsultantId(UUID.randomUUID())
        .rcGroupId(session.getGroupId());

    mockMvc.perform(post("/users/mails/reassignment")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(assignemtNotification))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verifyAsync(a -> verify(mailsControllerApi).sendMails(any()));
  }

  private Session givenAExistingSession() {
    var user = new EasyRandom().nextObject(User.class);
    user.setSessions(null);
    user.setUserAgencies(null);
    user.setUserMobileTokens(null);
    var session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);
    session.setUser(userRepository.save(user));
    session.setId(null);
    session.setSessionData(null);
    session.setPostcode("12345");
    session.setConsultingTypeId(1);
    session.setSessionTopics(Lists.newArrayList());
    return sessionRepository.save(session);
  }

  private void givenARealmResource() {
    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(mock(UsersResource.class));
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

  private void givenAUserDTOWithDemographics() {
    givenAUserDTO();
    userDTO.setUserGender("MALE");
  }

  private long aPositiveLong() {
    return Math.abs(easyRandom.nextLong());
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
        any(HttpEntity.class), eq(UserInfoResponseDTO.class))
    ).thenReturn(ResponseEntity.ok(userInfoResponse));
  }

  private void givenAValidRocketChatRoomsResponse() {
    var roomsGetDTO = new RoomsGetDTO();
    roomsGetDTO.setUpdate(new RoomsUpdateDTO[]{});
    when(restTemplate.exchange(
        Mockito.anyString(), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(RoomsGetDTO.class))
    ).thenReturn(ResponseEntity.ok(roomsGetDTO));
  }

  private void givenAValidTopicServiceResponse() {
    var firstTopic = new TopicDTO().id(1L).name("topic name").description("topic desc")
        .status("INACTIVE");
    var secondTopic = new TopicDTO().id(2L).name("topic name 2").description("topic desc 2")
        .status("ACTIVE");
    when(topicControllerApi.getApiClient()).thenReturn(new ApiClient());
    when(topicControllerApi.getAllTopics()).thenReturn(Lists.newArrayList(firstTopic, secondTopic));
  }

  private void givenAValidRocketChatSubscriptionsResponse() {
    var subscriptionsGetDTO = new SubscriptionsGetDTO();
    subscriptionsGetDTO.setUpdate(new SubscriptionsUpdateDTO[]{});
    when(rocketChatRestTemplate.exchange(
        Mockito.anyString(), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class))
    ).thenReturn(ResponseEntity.ok(subscriptionsGetDTO));

    when(restTemplate.exchange(
        Mockito.anyString(), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(SubscriptionsGetDTO.class))
    ).thenReturn(ResponseEntity.ok(subscriptionsGetDTO));
  }

  private void givenAValidRocketChatUpdateUserResponse() {
    var urlSuffix = "/api/v1/users.update";
    var updateUserResponse = easyRandom.nextObject(Void.class);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(Void.class)
    )).thenReturn(ResponseEntity.ok(updateUserResponse));
  }

  @NonNull
  private String givenAValidEmail() {
    return RandomStringUtils.randomAlphabetic(8)
        + "@" + RandomStringUtils.randomAlphabetic(8)
        + ".com";
  }

  private void givenABearerToken() {
    var tokenManager = mock(TokenManager.class);
    when(tokenManager.getAccessTokenString())
        .thenReturn(RandomStringUtils.randomAlphanumeric(255));
    when(keycloak.tokenManager()).thenReturn(tokenManager);
  }

  private void givenAValidConsultant() {
    consultant = consultantRepository.findAll().iterator().next();
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
    when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
    when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.CONSULTANT.getValue()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
  }

  @SuppressWarnings("SameParameterValue")
  private void givenAValidConsultantWithId(String id) {
    consultant = consultantRepository.findById(id).orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
    when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
    when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.CONSULTANT.getValue()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
  }

  private void givenAValidUser() {
    user = userRepository.findAll().iterator().next();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
    when(authenticatedUser.isConsultant()).thenReturn(false);
    when(authenticatedUser.getUsername()).thenReturn(user.getUsername());
    when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.USER.getValue()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anotherAuthority"));
  }

  private void givenConsultingTypeServiceResponse(Integer consultingTypeId) {
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
    body.setId(consultingTypeId);
    ParameterizedTypeReference<List<BasicConsultingTypeResponseDTO>> value = new ParameterizedTypeReference<>() {
    };
    when(restTemplate.exchange(any(RequestEntity.class), eq(value)))
        .thenReturn(ResponseEntity.ok(List.of(body)));
  }

  private void givenConsultingTypeServiceResponse() {
    this.givenConsultingTypeServiceResponse(1);
  }

  private void givenAValidConsultantSpeaking(LanguageCode languageCode) {
    givenAValidConsultant();
    consultant.setLanguages(Set.of(new Language(consultant, languageCode)));
    consultant = consultantRepository.save(consultant);
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
    } else if (easyRandom.nextBoolean()) {
      patchDtoAsMap.put("displayName", "");
    } else {
      patchDtoAsMap.put("emailToggles", Set.of(new EmailToggle()));
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

  private HashMap<String, Object> givenAnUnknownPreferredLanguagePatchDto() {
    var patchDtoAsMap = new HashMap<String, Object>(1);
    patchDtoAsMap.put("preferredLanguage", "xx");

    return patchDtoAsMap;
  }

  private HashMap<String, Object> givenAValidEmailTogglePatchDto(boolean state) {
    var dailyEnquiryToggle = new EmailToggle();
    dailyEnquiryToggle.setName(EmailType.DAILY_ENQUIRY);
    dailyEnquiryToggle.setState(state);

    var newChatMessageToggle = new EmailToggle();
    newChatMessageToggle.setName(EmailType.NEW_CHAT_MESSAGE_FROM_ADVICE_SEEKER);
    newChatMessageToggle.setState(state);

    var newFeedbackMessageToggle = new EmailToggle();
    newFeedbackMessageToggle.setName(EmailType.NEW_FEEDBACK_MESSAGE_FROM_ADVICE_SEEKER);
    newFeedbackMessageToggle.setState(state);

    var patchDtoAsMap = new HashMap<String, Object>(3);
    patchDtoAsMap.put("emailToggles", Set.of(
        dailyEnquiryToggle, newChatMessageToggle, newFeedbackMessageToggle)
    );

    if (!state) {
      consultantsToReset.add(consultant);
    }

    return patchDtoAsMap;
  }

  private void givenConsultantIsNotToNotifyAboutNewEnquiries() {
    consultant.setNotifyEnquiriesRepeating(false);
    consultantRepository.save(consultant);
    consultantsToReset.add(consultant);
  }

  private void givenConsultantIsNotToNotifyAboutNewFollowUps() {
    consultant.setNotifyNewChatMessageFromAdviceSeeker(false);
    consultant.setNotifyNewFeedbackMessageFromAdviceSeeker(false);
    consultantRepository.save(consultant);
    consultantsToReset.add(consultant);
  }

  private String givenAnUnknownEmailTypeTogglePatchDto() throws JsonProcessingException {
    var patchDto = givenAValidEmailTogglePatchDto(true);

    return objectMapper.writeValueAsString(patchDto)
        .replaceAll("\"[A-Z_]{2,}\"", "\"" + RandomStringUtils.randomAlphanumeric(8)) + "\"";
  }

  private void givenAFullPatchDto(boolean encourage2fa) {
    givenAFullPatchDto();
    patchUserDTO.setEncourage2fa(encourage2fa);
  }

  private void givenAFullPatchDto() {
    patchUserDTO = easyRandom.nextObject(PatchUserDTO.class);

    var dailyEnquiries = new EmailToggle();
    dailyEnquiries.setName(EmailType.DAILY_ENQUIRY);
    dailyEnquiries.setState(true);

    var newChat = new EmailToggle();
    newChat.setName(EmailType.NEW_CHAT_MESSAGE_FROM_ADVICE_SEEKER);
    newChat.setState(true);

    var newFeedback = new EmailToggle();
    newFeedback.setName(EmailType.NEW_FEEDBACK_MESSAGE_FROM_ADVICE_SEEKER);
    newFeedback.setState(true);

    patchUserDTO.setEmailToggles(Set.of(dailyEnquiries, newChat, newFeedback));
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
    when(restTemplate.exchange(anyString(), any(), any(), eq(UserInfoResponseDTO.class),
        anyString())).thenReturn(userInfoResponseDTO);
  }

  private void givenEnabledE2EEncryption() {
    videoChatConfig.setE2eEncryptionEnabled(true);
  }

  private void givenDisplayNameAllowedForConsultants() {
    identityConfig.setDisplayNameAllowedForConsultants(true);
  }
}
