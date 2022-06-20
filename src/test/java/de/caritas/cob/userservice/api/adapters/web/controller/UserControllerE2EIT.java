package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static java.util.Objects.isNull;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
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
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UpdateUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailToggle;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailType;
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
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.OtpType;
import de.caritas.cob.userservice.api.model.Success;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
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
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
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
  private Keycloak keycloak;

  @Captor
  private ArgumentCaptor<HttpEntity<OtpSetupDTO>> otpSetupCaptor;

  @Captor
  private ArgumentCaptor<HttpEntity<UpdateUser>> updateUserCaptor;

  private User user;
  private Consultant consultant;
  private UpdateConsultantDTO updateConsultantDTO;
  private Set<Consultant> consultantsToReset = new HashSet<>();
  private List<ConsultantAgency> consultantAgencies = new ArrayList<>();
  private OneTimePasswordDTO oneTimePasswordDTO;
  private EmailDTO emailDTO;
  private String tan;
  private String email;
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
    oneTimePasswordDTO = null;
    emailDTO = null;
    tan = null;
    email = null;
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

    var savedUser = userRepository.findById(user.getUserId());
    assertTrue(savedUser.isPresent());
    assertEquals(patchUserDTO.getEncourage2fa(), savedUser.get().getEncourage2fa());
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
  public void deactivateAndFlagUserAccountForDeletionShouldDeactivateAndRespondWithOkIf2faIsOff()

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
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void startTwoFactorAuthByEmailSetupShouldRespondWithNoContent() throws Exception {
    givenAValidConsultant();
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
  void startTwoFactorAuthByEmailSetupShouldRespondWithNoContentIfEmailIsOwnedByUser()
      throws Exception {
    givenAValidConsultant();
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
  void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfEmailIsNotAvailable()
      throws Exception {
    givenAValidConsultant();
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
  void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTheEmailFormatIsInvalid()
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
  void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfNoPayloadIsGiven()
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
  void startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithInvalidParameterError()
      throws Exception {
    givenAValidConsultant();
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
  void startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithAlreadyConfiguredError()
      throws Exception {
    givenAValidConsultant();
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
  void finishTwoFactorAuthByEmailSetupForAConsultantShouldRespondWithNoContent()
      throws Exception {
    givenAValidConsultant();
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
  void finishTwoFactorAuthByEmailSetupForAUserShouldRespondWithNoContent() throws Exception {
    givenAValidUser();
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTanLengthIsWrong()
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTanHasLetters()
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithNotFoundIfTanIsEmpty()
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTheTanIsInvalid()
      throws Exception {
    givenAValidConsultant();
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
      throws Exception {
    givenAValidConsultant();
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithTooManyRequestsIfTooManyTanAttempts()
      throws Exception {
    givenAValidConsultant();
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
  void finishTwoFactorAuthByEmailSetupShouldRespondWithPreconditionFailedIfOtpByEmailHasBeenSetupBefore()
      throws Exception {
    givenAValidConsultant();
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
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void activateTwoFactorAuthForUserShouldRespondWithOK() throws Exception {
    givenAValidConsultant();
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
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenOtpHasWrongLength()
      throws Exception {
    givenAValidConsultant();
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
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenSecretHasWrongLength()
      throws Exception {
    givenAValidConsultant();
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
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfTheOtpIsInvalid()
      throws Exception {
    givenAValidConsultant();
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
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfParameterInvalid()
      throws Exception {
    givenAValidConsultant();
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
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
      throws Exception {
    givenAValidConsultant();
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
  void deactivateTwoFactorAuthByAppShouldRespondWithOK() throws Exception {
    givenAValidConsultant();
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
  void deactivateTwoFactorAuthByAppShouldRespondWithInternalServerErrorWhenKeycloakIsDown()
      throws Exception {
    givenAValidConsultant();
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

  private void givenARealmResource() {
    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(mock(UsersResource.class));
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

  private void givenAValidKeycloakSetupEmailResponse(String username) {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + username;
    var successWithEmail = new SuccessWithEmail();
    email = givenAValidEmail();
    successWithEmail.setEmail(email);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenReturn(new ResponseEntity<>(successWithEmail, CREATED));
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

  private void givenAValidRocketChatUpdateUserResponse() {
    var urlSuffix = "/api/v1/users.update";
    var updateUserResponse = easyRandom.nextObject(Void.class);

    when(rocketChatRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(Void.class)
    )).thenReturn(ResponseEntity.ok(updateUserResponse));
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

  private void givenAValidConsultant() {
    consultant = consultantRepository.findAll().iterator().next();
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
