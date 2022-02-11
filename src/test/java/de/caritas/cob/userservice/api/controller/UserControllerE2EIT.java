package de.caritas.cob.userservice.api.controller;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.EmailDTO;
import de.caritas.cob.userservice.api.model.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.model.LanguageResponseDTO;
import de.caritas.cob.userservice.api.model.OneTimePasswordDTO;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.model.Success;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.rocketchat.RocketChatUserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultant.Language;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.Cookie;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
  private KeycloakAdminClientAccessor keycloakAdminClientAccessor;

  @Captor
  private ArgumentCaptor<HttpEntity<OtpSetupDTO>> captor;

  private User user;

  private Consultant consultant;

  private Session session;

  private UpdateConsultantDTO updateConsultantDTO;

  private EnquiryMessageDTO enquiryMessageDTO;

  private Set<de.caritas.cob.userservice.api.model.LanguageCode> allLanguages = new HashSet<>();

  private Set<Consultant> consultantsToReset = new HashSet<>();

  private OneTimePasswordDTO oneTimePasswordDTO;

  private EmailDTO emailDTO;

  private String tan;

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
        de.caritas.cob.userservice.api.model.LanguageCode.fromValue(
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
        de.caritas.cob.userservice.api.model.LanguageCode.fromValue(
            language.getLanguageCode().toString())
    )));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetupShouldRespondWithNoContent() throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
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
  public void startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithError()
      throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
    givenABearerToken();
    givenAKeycloakVerifyEmailErrorResponse();

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
  public void finishTwoFactorAuthByEmailSetupShouldRespondWithNoContent() throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAValidKeycloakSetupEmailResponse();

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

    mockMvc.perform(
            put("/users/twoFactorAuth")
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

    mockMvc.perform(
            put("/users/twoFactorAuth")
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

    mockMvc.perform(
            put("/users/twoFactorAuth")
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

    mockMvc.perform(
            put("/users/twoFactorAuth")
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

    mockMvc.perform(
            delete("/users/twoFactorAuth")
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

    mockMvc.perform(
            delete("/users/twoFactorAuth")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
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

  private void givenAValidKeycloakSetupEmailResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var successWithEmail = new SuccessWithEmail();
    successWithEmail.setEmail(givenAValidEmail());

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

  private void givenAKeycloakSetupEmailTooManyRequestsResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var tooManyAttempts = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS,
        "too many attempts", null, null);

    when(keycloakRestTemplate.postForEntity(
        endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)
    )).thenThrow(tooManyAttempts);
  }

  private void givenAValidKeycloakVerifyEmailResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var success = easyRandom.nextObject(Success.class);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)
    )).thenReturn(ResponseEntity.ok(success));
  }

  private void givenAKeycloakVerifyEmailErrorResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var invalidParameter = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "invalid parameter",
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

  private void givenAKeycloakSetupOtpValidationErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidCode = new HttpClientErrorException(HttpStatus.UNAUTHORIZED,
        "the code was not valid", null, null);

    when(keycloakRestTemplate.exchange(
        endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)
    )).thenThrow(invalidCode);
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
    when(keycloakAdminClientAccessor.getBearerToken()).thenReturn(
        RandomStringUtils.randomAlphanumeric(255));
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

  private de.caritas.cob.userservice.api.model.LanguageCode mapLanguageCode(Language language) {
    return de.caritas.cob.userservice.api.model.LanguageCode.fromValue(
        language.getLanguageCode().name()
    );
  }

  private void givenAMinimalUpdateConsultantDto(String email) {
    updateConsultantDTO = new UpdateConsultantDTO()
        .email(email)
        .firstname(RandomStringUtils.randomAlphabetic(8))
        .lastname(RandomStringUtils.randomAlphabetic(12));
  }

  private void givenAnUpdateConsultantDtoWithLanguages(String email) {
    givenAMinimalUpdateConsultantDto(email);

    var languages = List.of(
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.LanguageCode.class),
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.LanguageCode.class),
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.LanguageCode.class)
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
