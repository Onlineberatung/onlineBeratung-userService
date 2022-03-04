package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID_HEADER_PARAMETER_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.OneTimePasswordDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.config.auth.Authority;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.OtpType;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Success;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.service.rocketchat.dto.RocketChatUserDTO;
import de.caritas.cob.userservice.api.service.rocketchat.dto.user.UserInfoResponseDTO;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UserControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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
  private de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi consultingTypeControllerApi;

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
  private Keycloak keycloak;

  @Captor
  private ArgumentCaptor<HttpEntity<OtpSetupDTO>> captor;

  private User user;

  private Consultant consultant;

  private Session session;

  private UpdateConsultantDTO updateConsultantDTO;

  private EnquiryMessageDTO enquiryMessageDTO;

  private Set<de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode> allLanguages = new HashSet<>();

  private Set<Consultant> consultantsToReset = new HashSet<>();

  private OneTimePasswordDTO oneTimePasswordDTO;

  private EmailDTO emailDTO;

  private String tan;

  private String email;

  private PatchUserDTO patchUserDTO;

  private UserDTO userDTO;

  private Chat chat;

  @AfterEach
  public void reset() {
    user = null;
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
    oneTimePasswordDTO = null;
    emailDTO = null;
    tan = null;
    email = null;
    patchUserDTO = null;
    userDTO = null;
    chat = null;
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void createEnquiryMessageWithLanguageShouldSaveLanguageAndRespondWithCreated()
      throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatInfoResponse();
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
    givenValidRocketChatInfoResponse();
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
    givenAValidConsultant();
    givenKeycloakRespondsOtpByAppHasBeenSetup(consultant.getUsername());

    var consultantAgency = consultant.getConsultantAgencies().iterator().next();

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(consultant.getId())))
        .andExpect(jsonPath("userName", is("emigration-team")))
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
        .andExpect(jsonPath("userRoles[0]", is("CONSULTANT")))
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
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faByAppIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidUser();
    givenConsultingTypeServiceResponse();
    givenKeycloakRespondsOtpByAppHasBeenSetup(user.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(user.getUserId())))
        .andExpect(jsonPath("userName", is("performance-asker-72")))
        .andExpect(jsonPath("firstName", is(nullValue())))
        .andExpect(jsonPath("lastName", is(nullValue())))
        .andExpect(jsonPath("email").exists())
        .andExpect(jsonPath("languages", is(nullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", is(nullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("USER")))
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
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faByEmailIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant();
    givenKeycloakRespondsOtpByEmailHasBeenSetup(consultant.getUsername());

    var consultantAgency = consultant.getConsultantAgencies().iterator().next();

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(consultant.getId())))
        .andExpect(jsonPath("userName", is("emigration-team")))
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
        .andExpect(jsonPath("userRoles[0]", is("CONSULTANT")))
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
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faByEmailIsActive()
      throws Exception {
    givenABearerToken();
    givenAValidUser();
    givenConsultingTypeServiceResponse();
    givenKeycloakRespondsOtpByEmailHasBeenSetup(user.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(user.getUserId())))
        .andExpect(jsonPath("userName", is("performance-asker-72")))
        .andExpect(jsonPath("firstName", is(nullValue())))
        .andExpect(jsonPath("lastName", is(nullValue())))
        .andExpect(jsonPath("email").exists())
        .andExpect(jsonPath("languages", is(nullValue())))
        .andExpect(jsonPath("encourage2fa").doesNotExist())
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", is(nullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("USER")))
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
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getUserDataShouldRespondWithConsultantDataAndStatusOkWhen2faIsNotActivated()
      throws Exception {
    givenABearerToken();
    givenAValidConsultant();
    givenKeycloakRespondsOtpHasNotBeenSetup(consultant.getUsername());

    var consultantAgency = consultant.getConsultantAgencies().iterator().next();

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(consultant.getId())))
        .andExpect(jsonPath("userName", is("emigration-team")))
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
        .andExpect(jsonPath("userRoles[0]", is("CONSULTANT")))
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
        .andExpect(jsonPath("inTeamAgency", is(consultant.isTeamConsultant())));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getUserDataShouldRespondWithUserDataAndStatusOkWhen2faIsNotActivated()
      throws Exception {
    givenABearerToken();
    givenAValidUser();
    givenConsultingTypeServiceResponse();
    givenKeycloakRespondsOtpHasNotBeenSetup(user.getUsername());

    mockMvc.perform(
            get("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("userId", is(user.getUserId())))
        .andExpect(jsonPath("userName", is("performance-asker-72")))
        .andExpect(jsonPath("firstName", is(nullValue())))
        .andExpect(jsonPath("lastName", is(nullValue())))
        .andExpect(jsonPath("email", is(nullValue())))
        .andExpect(jsonPath("languages", is(nullValue())))
        .andExpect(jsonPath("absenceMessage", is(nullValue())))
        .andExpect(jsonPath("agencies", is(nullValue())))
        .andExpect(jsonPath("userRoles", hasSize(1)))
        .andExpect(jsonPath("userRoles[0]", is("USER")))
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
        .andExpect(jsonPath("inTeamAgency", is(false)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void patchUserDataShouldSaveAdviceSeekerAndRespondWithNoContent() throws Exception {
    givenAValidUser();
    givenAValidPatchDto();

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
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
    givenAValidConsultant();
    givenAValidPatchDto();

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(patchUserDTO.getEncourage2fa(), savedConsultant.get().getEncourage2fa());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void patchUserDataShouldOverrideDefaultAndRespondWithNoContent() throws Exception {
    givenAValidConsultant();
    givenAValidPatchDto(false);

    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
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
    givenAValidConsultant();

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(true, savedConsultant.get().getEncourage2fa());

    givenAValidPatchDto(false);
    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUserDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    assertEquals(false, savedConsultant.get().getEncourage2fa());

    givenAValidPatchDto(true);
    mockMvc.perform(
            patch("/users/data")
                .cookie(CSRF_COOKIE)
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
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldSaveDefaultLanguageAndRespondWithOk() throws Exception {
    givenAValidConsultant();
    givenAMinimalUpdateConsultantDto(consultant.getEmail());
    givenValidRocketChatInfoResponse();

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
    givenAValidConsultant();
    givenAnUpdateConsultantDtoWithLanguages(consultant.getEmail());
    givenValidRocketChatInfoResponse();

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
    givenValidRocketChatInfoResponse();

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
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnBadRequestIfUserIdHasInvalidFormat() throws Exception {
    var invalidUserId = RandomStringUtils.randomAlphabetic(16);

    mockMvc.perform(
            post("/users/{userId}/chat/{chatId}/ban", invalidUserId, aPositiveLong())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnBadRequestIfChatIdHasInvalidFormat() throws Exception {
    var invalidChatId = RandomStringUtils.randomAlphabetic(16);

    mockMvc.perform(
            post("/users/{userId}/chat/{chatId}/ban", UUID.randomUUID(), invalidChatId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNotFoundIfUserDoesNotExist() throws Exception {
    givenAValidConsultant();
    givenAValidChat(consultant);

    mockMvc.perform(
            post("/users/{userId}/chat/{chatId}/ban", UUID.randomUUID(), chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNotFoundIfChatDoesNotExist() throws Exception {
    givenAValidUser();

    mockMvc.perform(
            post("/users/{userId}/chat/{chatId}/ban", user.getUserId(), aPositiveLong())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.UPDATE_CHAT)
  public void banFromChatShouldReturnNoContentIfBanWentWell() throws Exception {
    givenAValidUser();
    givenAValidConsultant();
    givenAValidChat(consultant);

    mockMvc.perform(
            post("/users/{userId}/chat/{chatId}/ban", user.getUserId(), chat.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithNoContent() throws Exception {
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
        .exchange(endsWith(urlSuffix), eq(HttpMethod.PUT), captor.capture(), eq(Success.class));

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(emailDTO.getEmail(), otpSetupDTO.getEmail());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithNoContentIfEmailIsOwnedByUser()
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
  public void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfEmailIsNotAvailable()
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
  public void startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithAlreadyConfiguredError()
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
  public void finishTwoFactorAuthByEmailSetupForAConsultantShouldRespondWithNoContent()
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
            endsWith(urlSuffix), captor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());

    var c = consultantRepository.findById(consultant.getId()).orElseThrow();
    assertEquals(email, c.getEmail());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupForAUserShouldRespondWithNoContent() throws Exception {
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
            endsWith(urlSuffix), captor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = captor.getValue().getBody();
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
            endsWith(urlSuffix), captor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
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
            endsWith(urlSuffix), captor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithTooManyRequestsIfTooManyTanAttempts()
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
            endsWith(urlSuffix), captor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithPreconditionFailedIfOtpByEmailHasBeenSetupBefore()
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
            endsWith(urlSuffix), captor.capture(), eq(SuccessWithEmail.class)
        );

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithOK() throws Exception {
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();

    mockMvc.perform(
            put("/users/twoFactorAuth")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    verify(keycloakRestTemplate).exchange(endsWith(urlSuffix), eq(HttpMethod.PUT), captor.capture(),
        eq(OtpInfoDTO.class));

    var otpSetupDTO = captor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(oneTimePasswordDTO.getOtp(), otpSetupDTO.getInitialCode());
    assertEquals(oneTimePasswordDTO.getSecret(), otpSetupDTO.getSecret());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenOtpHasWrongLength()
      throws Exception {
    givenAValidConsultant();
    givenAnInvalidOneTimePasswordDTO();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa/app");

    mockMvc.perform(
            put(path)
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
    givenAValidConsultant();
    givenAWronglyFormattedSecret();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa/app");

    mockMvc.perform(
            put(path)
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
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpValidationErrorResponse();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa/app");

    mockMvc.perform(
            put(path)
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
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpInvalidParameterErrorResponse();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa/app");

    mockMvc.perform(
            put(path)
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
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpAnotherOtpConfigActiveErrorResponse();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa/app");

    mockMvc.perform(
            put(path)
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
    givenAValidConsultant();
    givenABearerToken();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa");

    mockMvc.perform(
            delete(path)
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
    givenAValidConsultant();
    givenABearerToken();
    givenKeycloakIsDown();
    var path = "/users/" + (easyRandom.nextBoolean() ? "twoFactorAuth" : "2fa");

    mockMvc.perform(
            delete(path)
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
    consultant = consultantRepository.findAll().iterator().next();
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
    when(authenticatedUser.isUser()).thenReturn(false);
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
    when(authenticatedUser.getRoles()).thenReturn(Set.of(Authority.CONSULTANT.name()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
  }

  private void givenAValidUser() {
    user = userRepository.findAll().iterator().next();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUser.isUser()).thenReturn(true);
    when(authenticatedUser.isConsultant()).thenReturn(false);
    when(authenticatedUser.getUsername()).thenReturn(user.getUsername());
    when(authenticatedUser.getRoles()).thenReturn(Set.of(Authority.USER.name()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anotherAuthority"));
  }

  private void givenAValidChat(Consultant consultant) {
    chat = easyRandom.nextObject(Chat.class);
    chat.setId(null);
    chat.setChatOwner(consultant);
    chat.setConsultingTypeId(easyRandom.nextInt(128));
    chat.setDuration(easyRandom.nextInt(32768));
    chat.setMaxParticipants(easyRandom.nextInt(128));
    chatRepository.save(chat);
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

  private long givenAnAgencyIdWithDefaultLanguageOnly() {
    return 121;
  }

  private long givenAnAgencyWithMultipleLanguages() {
    var agencyId = 0L;

    consultantAgencyRepository
        .findByAgencyIdAndDeleteDateIsNull(agencyId)
        .forEach(consultantAgency -> {
          var consultant = consultantAgency.getConsultant();
          var language1 = new Language(consultant, easyRandom.nextObject(LanguageCode.class));
          var language2 = new Language(consultant, easyRandom.nextObject(LanguageCode.class));
          allLanguages.add(mapLanguageCode(language1));
          allLanguages.add(mapLanguageCode(language2));
          var languages = Set.of(language1, language2);
          consultant.setLanguages(languages);
          consultantRepository.save(consultant);

          consultantsToReset.add(consultant);
        });

    return agencyId;
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

  private HashMap<String, Object> givenAnInvalidPatchDto() {
    var patchDtoAsMap = new HashMap<String, Object>(1);
    patchDtoAsMap.put("encourage2fa", null);

    return patchDtoAsMap;
  }

  @SuppressWarnings("SameParameterValue")
  private void givenAValidPatchDto(boolean encourage2fa) {
    givenAValidPatchDto();
    patchUserDTO.setEncourage2fa(encourage2fa);
  }

  private void givenAValidPatchDto() {
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

  private void givenValidRocketChatInfoResponse() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    var body = new UserInfoResponseDTO();
    body.setSuccess(true);
    if (nonNull(user)) {
      body.setUser(new RocketChatUserDTO("", user.getUsername(), null));
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

  private void givenAUserWithASessionNotEnquired() {
    user = userRepository.findById("552d3f10-1b6d-47ee-aec5-b88fbf988f9e").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    session = user.getSessions().stream()
        .filter(s -> isNull(s.getEnquiryMessageDate()))
        .findFirst()
        .orElseThrow();
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
}
