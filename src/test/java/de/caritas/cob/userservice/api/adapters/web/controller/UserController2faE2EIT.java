package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.OneTimePasswordDTO;
import de.caritas.cob.userservice.api.config.VideoChatConfig;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.Cookie;
import lombok.NonNull;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
class UserController2faE2EIT {

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

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean
  @Qualifier("keycloakRestTemplate")
  private RestTemplate keycloakRestTemplate;

  @MockBean private Keycloak keycloak;

  @Captor private ArgumentCaptor<HttpEntity<OtpSetupDTO>> otpSetupCaptor;

  private User user;
  private Consultant consultant;
  private List<ConsultantAgency> consultantAgencies = new ArrayList<>();
  private OneTimePasswordDTO oneTimePasswordDTO;
  private EmailDTO emailDTO;
  private String tan;
  private String email;
  private Chat chat;
  private ChatAgency chatAgency;
  private UserAgency userAgency;

  @AfterEach
  void reset() {
    if (nonNull(user)) {
      user.setDeleteDate(null);
      userRepository.save(user);
      user = null;
    }
    consultant = null;
    consultantAgencyRepository.deleteAll(consultantAgencies);
    consultantAgencies = new ArrayList<>();
    oneTimePasswordDTO = null;
    emailDTO = null;
    tan = null;
    email = null;
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
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void startTwoFactorAuthByEmailSetupShouldRespondWithNoContent() throws Exception {
    startTwoFactorAuthorizationAndAssertResponseIsCorrect();
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.RESTRICTED_AGENCY_ADMIN)
  void startTwoFactorAuthByEmailSetupShouldRespondWithNoContent_If_Called_As_AgencyAdmin()
      throws Exception {
    startTwoFactorAuthorizationAndAssertResponseIsCorrect();
  }

  private void startTwoFactorAuthorizationAndAssertResponseIsCorrect() throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
    givenKeycloakFoundNoEmailInUse();
    givenABearerToken();
    givenAValidKeycloakVerifyEmailResponse();

    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), otpSetupCaptor.capture(), eq(Success.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(emailDTO.getEmail().toLowerCase(), otpSetupDTO.getEmail());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void startTwoFactorAuthByEmailSetupShouldRespondWithNoContentIfEmailIsOwnedByUser()
      throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
    givenKeycloakFoundOwnEmailInUse();
    givenABearerToken();
    givenAValidKeycloakVerifyEmailResponse();

    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfEmailIsNotAvailable()
      throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
    givenKeycloakFoundAnEmailInUse();
    givenABearerToken();

    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isPreconditionFailed());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTheEmailFormatIsInvalid()
      throws Exception {
    givenAnInvalidEmailDTO();

    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void startTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfNoPayloadIsGiven()
      throws Exception {
    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void
      startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithInvalidParameterError()
          throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
    givenKeycloakFoundNoEmailInUse();
    givenABearerToken();
    givenAKeycloakVerifyEmailInvalidParameterErrorResponse();

    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void
      startTwoFactorAuthByEmailSetupShouldRespondWithInternalServerErrorIfKeycloakRespondsWithAlreadyConfiguredError()
          throws Exception {
    givenAValidConsultant();
    givenAValidEmailDTO();
    givenKeycloakFoundNoEmailInUse();
    givenABearerToken();
    givenAKeycloakVerifyEmailIAlreadyConfiguredErrorResponse();

    mockMvc
        .perform(
            put("/users/2fa/email")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupForAConsultantShouldRespondWithNoContent() throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAValidKeycloakSetupEmailResponse(consultant.getUsername());
    givenAValidKeycloakEmailChangeByUsernameResponse(consultant.getUsername());

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class));

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

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + user.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());

    var u = userRepository.findById(user.getUserId()).orElseThrow();
    assertEquals(email, u.getEmail());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTanLengthIsWrong()
      throws Exception {
    givenAWronglyFormattedTan();

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTanHasLetters()
      throws Exception {
    givenATanWithLetters();

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupShouldRespondWithNotFoundIfTanIsEmpty() throws Exception {
    mockMvc
        .perform(
            post("/users/2fa/email/validate/")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfTheTanIsInvalid()
      throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakSetupEmailInvalidCodeResponse();

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
      throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakSetupEmailOtpAnotherOtpConfigActiveErrorResponse();

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void finishTwoFactorAuthByEmailSetupShouldRespondWithTooManyRequestsIfTooManyTanAttempts()
      throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakSetupEmailTooManyRequestsResponse();

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isTooManyRequests());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void
      finishTwoFactorAuthByEmailSetupShouldRespondWithPreconditionFailedIfOtpByEmailHasBeenSetupBefore()
          throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenACorrectlyFormattedTan();
    givenAKeycloakAlreadySetupEmailResponse();

    mockMvc
        .perform(
            post("/users/2fa/email/validate/{tan}", tan)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isPreconditionFailed());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .postForEntity(endsWith(urlSuffix), otpSetupCaptor.capture(), eq(SuccessWithEmail.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(tan, otpSetupDTO.getInitialCode());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void activateTwoFactorAuthForUserShouldRespondWithOK() throws Exception {
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();

    mockMvc
        .perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .exchange(
            endsWith(urlSuffix),
            eq(HttpMethod.PUT),
            otpSetupCaptor.capture(),
            eq(OtpInfoDTO.class));

    var otpSetupDTO = otpSetupCaptor.getValue().getBody();
    assertNotNull(otpSetupDTO);
    assertEquals(oneTimePasswordDTO.getOtp(), otpSetupDTO.getInitialCode());
    assertEquals(oneTimePasswordDTO.getSecret(), otpSetupDTO.getSecret());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenOtpHasWrongLength()
      throws Exception {
    givenAValidConsultant();
    givenAnInvalidOneTimePasswordDTO();

    mockMvc
        .perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestWhenSecretHasWrongLength()
      throws Exception {
    givenAValidConsultant();
    givenAWronglyFormattedSecret();

    mockMvc
        .perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfTheOtpIsInvalid() throws Exception {
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpValidationErrorResponse();

    mockMvc
        .perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfParameterInvalid()
      throws Exception {
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpInvalidParameterErrorResponse();

    mockMvc
        .perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void activateTwoFactorAuthForUserShouldRespondWithBadRequestIfAnotherOtpConfigIsActive()
      throws Exception {
    givenAValidConsultant();
    givenACorrectlyFormattedOneTimePasswordDTO();
    givenABearerToken();
    givenAKeycloakSetupOtpAnotherOtpConfigActiveErrorResponse();

    mockMvc
        .perform(
            put("/users/2fa/app")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void deactivateTwoFactorAuthByAppShouldRespondWithOK() throws Exception {
    givenAValidConsultant();
    givenABearerToken();

    mockMvc
        .perform(
            delete("/users/2fa")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var urlSuffix = "/auth/realms/test/otp-config/delete-otp/" + consultant.getUsername();
    verify(keycloakRestTemplate)
        .exchange(
            endsWith(urlSuffix), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void deactivateTwoFactorAuthByAppShouldRespondWithInternalServerErrorWhenKeycloakIsDown()
      throws Exception {
    givenAValidConsultant();
    givenABearerToken();
    givenKeycloakIsDown();

    mockMvc
        .perform(
            delete("/users/2fa")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oneTimePasswordDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
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

    when(usersResource.search(encodedUsername)).thenReturn(userRepresentationList);
    when(usersResource.get(keycloakId)).thenReturn(userResource);

    var realmResource = mock(RealmResource.class);
    when(realmResource.users()).thenReturn(usersResource);
    when(keycloak.realm(anyString())).thenReturn(realmResource);
  }

  private void givenKeycloakIsDown() {
    var urlSuffix = "/auth/realms/test/otp-config/delete-otp/" + consultant.getUsername();
    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
        .thenThrow(new RestClientException("Keycloak down"));
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
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + username;
    var successWithEmail = new SuccessWithEmail();
    email = givenAValidEmail();
    successWithEmail.setEmail(email);

    when(keycloakRestTemplate.postForEntity(
            endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)))
        .thenReturn(new ResponseEntity<>(successWithEmail, CREATED));
  }

  private void givenAKeycloakSetupEmailInvalidCodeResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var codeInvalid =
        new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "the code was not valid", null, null);

    when(keycloakRestTemplate.postForEntity(
            endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)))
        .thenThrow(codeInvalid);
  }

  private void givenKeycloakFoundAnEmailInUse() {
    var usernameTranscoder = new UsernameTranscoder();
    var userRepresentation = new UserRepresentation();
    var username = usernameTranscoder.encodeUsername(RandomStringUtils.randomAlphabetic(8, 16));
    userRepresentation.setUsername(username);
    userRepresentation.setEmail(emailDTO.getEmail().toLowerCase());
    var userRepresentationList = new ArrayList<UserRepresentation>(1);
    userRepresentationList.add(userRepresentation);
    var usersResource = mock(UsersResource.class);
    when(usersResource.search(eq(emailDTO.getEmail().toLowerCase()), anyInt(), anyInt()))
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
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var codeInvalid =
        new HttpClientErrorException(
            HttpStatus.CONFLICT, "another otp configuration is already active", null, null);

    when(keycloakRestTemplate.postForEntity(
            endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)))
        .thenThrow(codeInvalid);
  }

  private void givenAKeycloakSetupEmailTooManyRequestsResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var tooManyAttempts =
        new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "too many attempts", null, null);

    when(keycloakRestTemplate.postForEntity(
            endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)))
        .thenThrow(tooManyAttempts);
  }

  private void givenAKeycloakAlreadySetupEmailResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp-mail/" + consultant.getUsername();
    var successWithEmail = new SuccessWithEmail();
    email = givenAValidEmail();
    successWithEmail.setEmail(email);

    when(keycloakRestTemplate.postForEntity(
            endsWith(urlSuffix), any(HttpEntity.class), eq(SuccessWithEmail.class)))
        .thenReturn(new ResponseEntity<>(successWithEmail, HttpStatus.OK));
  }

  private void givenAValidKeycloakVerifyEmailResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var success = easyRandom.nextObject(Success.class);

    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)))
        .thenReturn(ResponseEntity.ok(success));
  }

  private void givenAKeycloakVerifyEmailInvalidParameterErrorResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var invalidParameter =
        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "invalid parameter", null, null);

    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)))
        .thenThrow(invalidParameter);
  }

  private void givenAKeycloakVerifyEmailIAlreadyConfiguredErrorResponse() {
    var urlSuffix =
        "/auth/realms/test/otp-config/send-verification-mail/" + consultant.getUsername();
    var invalidParameter =
        new HttpClientErrorException(HttpStatus.CONFLICT, "already configured", null, null);

    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Success.class)))
        .thenThrow(invalidParameter);
  }

  private void givenAKeycloakSetupOtpInvalidParameterErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidParameter =
        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "invalid parameter", null, null);

    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)))
        .thenThrow(invalidParameter);
  }

  private void givenAKeycloakSetupOtpAnotherOtpConfigActiveErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidParameter =
        new HttpClientErrorException(
            HttpStatus.CONFLICT, "another otp configuration is already active", null, null);

    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)))
        .thenThrow(invalidParameter);
  }

  private void givenAKeycloakSetupOtpValidationErrorResponse() {
    var urlSuffix = "/auth/realms/test/otp-config/setup-otp/" + consultant.getUsername();
    var invalidCode =
        new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "the code was not valid", null, null);

    when(keycloakRestTemplate.exchange(
            endsWith(urlSuffix), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OtpInfoDTO.class)))
        .thenThrow(invalidCode);
  }

  private void givenAValidEmailDTO() {
    var email = givenAValidEmail();
    emailDTO = new EmailDTO();
    emailDTO.setEmail(email);
  }

  @NonNull
  private String givenAValidEmail() {
    return RandomStringUtils.randomAlphabetic(8)
        + "@"
        + RandomStringUtils.randomAlphabetic(8)
        + ".com";
  }

  private void givenAnInvalidEmailDTO() {
    var email = RandomStringUtils.randomAlphabetic(16) + ".com";

    emailDTO = new EmailDTO();
    emailDTO.setEmail(email);
  }

  private void givenABearerToken() {
    var tokenManager = mock(TokenManager.class);
    when(tokenManager.getAccessTokenString()).thenReturn(RandomStringUtils.randomAlphanumeric(255));
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
}
