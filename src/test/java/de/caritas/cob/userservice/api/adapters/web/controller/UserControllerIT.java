package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.*;
import static de.caritas.cob.userservice.api.testHelper.RequestBodyConstants.*;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.ApiResponseEntityExceptionHandler;
import de.caritas.cob.userservice.api.adapters.web.dto.*;
import de.caritas.cob.userservice.api.adapters.web.dto.validation.MandatoryFieldsProvider;
import de.caritas.cob.userservice.api.adapters.web.mapping.ConsultantDtoMapper;
import de.caritas.cob.userservice.api.adapters.web.mapping.UserDtoMapper;
import de.caritas.cob.userservice.api.admin.facade.AdminUserFacade;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.config.VideoChatConfig;
import de.caritas.cob.userservice.api.config.auth.Authority;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.*;
import de.caritas.cob.userservice.api.facade.*;
import de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacade;
import de.caritas.cob.userservice.api.facade.assignsession.AssignSessionFacade;
import de.caritas.cob.userservice.api.facade.sessionlist.SessionListFacade;
import de.caritas.cob.userservice.api.facade.userdata.*;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
import de.caritas.cob.userservice.api.model.*;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.port.in.IdentityManaging;
import de.caritas.cob.userservice.api.port.in.Messaging;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.*;
import de.caritas.cob.userservice.api.service.archive.SessionArchiveService;
import de.caritas.cob.userservice.api.service.archive.SessionDeleteService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.*;
import javax.servlet.http.Cookie;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.service.spi.ServiceException;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=testing,feature.topics.enabled=true")
public class UserControllerIT {

  private static final Cookie RC_TOKEN_COOKIE =
      new Cookie("rc_token", RandomStringUtils.randomAlphanumeric(43));

  private final String VALID_ENQUIRY_MESSAGE_BODY = "{\"message\": \"" + MESSAGE + "\"}";
  private final User USER = new User(USER_ID, null, "username", "name@domain.de", false);
  private final Consultant TEAM_CONSULTANT =
      new Consultant(
          CONSULTANT_ID,
          ROCKETCHAT_ID,
          "consultant",
          "first name",
          "last name",
          "consultant@cob.de",
          false,
          true,
          "",
          false,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Set<String> ROLES_WITH_USER =
      new HashSet<>(Arrays.asList("dummyRoleA", UserRole.USER.getValue(), "dummyRoleB"));
  private final SessionDTO SESSION_DTO =
      new SessionDTO()
          .id(SESSION_ID)
          .agencyId(AGENCY_ID)
          .consultingType(0)
          .status(2)
          .postcode(POSTCODE)
          .groupId(RC_GROUP_ID)
          .askerRcId(RC_USER_ID)
          .messageDate(MESSAGE_DATE)
          .isTeamSession(IS_NO_TEAM_SESSION);
  private final AgencyDTO AGENCY_DTO =
      new AgencyDTO()
          .id(AGENCY_ID)
          .name(NAME)
          .postcode(POSTCODE)
          .city(CITY)
          .description(DESCRIPTION)
          .teamAgency(false)
          .offline(false)
          .consultingType(CONSULTING_TYPE_ID_SUCHT);
  private final SessionConsultantForUserDTO SESSION_CONSULTANT_DTO =
      new SessionConsultantForUserDTO(null, NAME, IS_ABSENT, ABSENCE_MESSAGE, null);
  private final UserSessionResponseDTO USER_SESSION_RESPONSE_DTO =
      new UserSessionResponseDTO()
          .session(SESSION_DTO)
          .agency(AGENCY_DTO)
          .consultant(SESSION_CONSULTANT_DTO);
  protected static final String PATH_GET_PUBLIC_CONSULTANT_DATA =
      "/users/consultants/65c1095e-b977-493a-a34f-064b729d1d6c";
  private final String ERROR = "error";
  private final Session SESSION =
      Session.builder()
          .id(SESSION_ID)
          .user(USER)
          .consultant(TEAM_CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .agencyId(AGENCY_ID)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .postcode(POSTCODE)
          .status(SessionStatus.IN_PROGRESS)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(false)
          .isPeerChat(false)
          .build();

  private final Session SESSION_WITHOUT_CONSULTANT =
      Session.builder()
          .id(SESSION_ID)
          .user(USER)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .agencyId(AGENCY_ID)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .postcode(POSTCODE)
          .status(SessionStatus.NEW)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(false)
          .isPeerChat(false)
          .build();

  private final Session TEAM_SESSION =
      Session.builder()
          .id(SESSION_ID)
          .user(USER)
          .consultant(TEAM_CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .agencyId(AGENCY_ID)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .postcode(POSTCODE)
          .status(SessionStatus.IN_PROGRESS)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(true)
          .isPeerChat(false)
          .build();

  private final Session TEAM_SESSION_WITHOUT_GROUP_ID =
      Session.builder()
          .id(SESSION_ID)
          .user(USER)
          .consultant(TEAM_CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .agencyId(AGENCY_ID)
          .enquiryMessageDate(nowInUtc())
          .postcode(POSTCODE)
          .status(SessionStatus.IN_PROGRESS)
          .createDate(nowInUtc())
          .updateDate(nowInUtc())
          .teamSession(true)
          .isPeerChat(false)
          .build();

  private final ConsultantResponseDTO CONSULTANT_RESPONSE_DTO =
      new ConsultantResponseDTO()
          .consultantId(CONSULTANT_ID)
          .firstName(FIRST_NAME)
          .lastName(LAST_NAME);
  private final List<ConsultantResponseDTO> CONSULTANT_RESPONSE_DTO_LIST =
      Collections.singletonList(CONSULTANT_RESPONSE_DTO);
  private final Set<String> AUTHORITIES_ASSIGN_SESSION_AND_ENQUIRY =
      new HashSet<>(
          Arrays.asList(
              AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
              AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION));
  private final Set<String> AUTHORITY_ASSIGN_SESSION =
      new HashSet<>(Collections.singletonList(AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION));

  private final EasyRandom easyRandom = new EasyRandom();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockMvc mvc;

  @MockBean private UserAccountService accountProvider;
  @MockBean private SessionService sessionService;
  @MockBean private AuthenticatedUser authenticatedUser;
  @MockBean private CreateEnquiryMessageFacade createEnquiryMessageFacade;

  @MockBean
  @SuppressWarnings("unused")
  private ConsultantImportService consultantImportService;

  @MockBean private EmailNotificationFacade emailNotificationFacade;

  @MockBean
  @SuppressWarnings("unused")
  private AskerImportService askerImportService;

  @MockBean private SessionListFacade sessionListFacade;
  @MockBean private ConsultantAgencyService consultantAgencyService;
  @MockBean private AssignSessionFacade assignSessionFacade;
  @MockBean private AssignEnquiryFacade assignEnquiryFacade;
  @MockBean private IdentityClient identityClient;
  @MockBean private DecryptionService encryptionService;
  @MockBean private ConsultingTypeManager consultingTypeManager;
  @MockBean private UserHelper userHelper;
  @MockBean private ChatService chatService;
  @MockBean private StartChatFacade startChatFacade;
  @MockBean private GetChatFacade getChatFacade;
  @MockBean private JoinAndLeaveChatFacade joinAndLeaveChatFacade;
  @MockBean private AssignChatFacade assignChatFacade;
  @MockBean private CreateChatFacade createChatFacade;
  @MockBean private RocketChatService rocketChatService;
  @MockBean private ChatPermissionVerifier chatPermissionVerifier;
  @MockBean private StopChatFacade stopChatFacade;
  @MockBean private GetChatMembersFacade getChatMembersFacade;
  @MockBean private CreateUserFacade createUserFacade;

  @MockBean
  @SuppressWarnings("unused")
  private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean
  @SuppressWarnings("unused")
  private LinkDiscoverers linkDiscoverers;

  @MockBean private CreateNewConsultingTypeFacade createNewConsultingTypeFacade;
  @MockBean private MandatoryFieldsProvider mandatoryFieldsProvider;
  @MockBean private ConsultantDataFacade consultantDataFacade;
  @MockBean private SessionDataService sessionDataService;
  @MockBean private SessionArchiveService sessionArchiveService;
  @MockBean private ActionsRegistry actionsRegistry;

  @MockBean
  @SuppressWarnings("unused")
  private IdentityClientConfig identityClientConfig;

  @MockBean
  @SuppressWarnings("unused")
  private IdentityManaging identityManager;

  @MockBean
  @SuppressWarnings("unused")
  private AccountManaging accountManager;

  @MockBean
  @SuppressWarnings("unused")
  private Messaging messenger;

  @MockBean private ConsultantUpdateService consultantUpdateService;

  @SpyBean
  @SuppressWarnings("unused")
  private ConsultantDtoMapper consultantDtoMapper;

  @MockBean
  @SuppressWarnings("unused")
  private UserDtoMapper userDtoMapper;

  @MockBean private ConsultantService consultantService;

  @MockBean
  @SuppressWarnings("unused")
  private ConsultantDataProvider consultantDataProvider;

  @MockBean
  @SuppressWarnings("unused")
  private AskerDataProvider askerDataProvider;

  @MockBean
  @SuppressWarnings("unused")
  private KeycloakUserDataProvider keycloakUserDataProvider;

  @MockBean
  @SuppressWarnings("unused")
  private VideoChatConfig videoChatConfig;

  @MockBean private AdminUserFacade adminUserFacade;

  @MockBean private EmailNotificationMapper emailNotificationMapper;

  @MockBean private SessionDeleteService sessionDeleteService;

  @Mock private Logger logger;

  @Mock private Chat chat;

  @Before
  public void setUp() {
    HashMap<String, Object> drugsMap = new HashMap<>();
    drugsMap.put("others", false);
    HashMap<String, Object> addictiveDrugsMap = new HashMap<>();
    addictiveDrugsMap.put("drugs", drugsMap);
    setInternalState(UserController.class, "log", logger);
    setInternalState(LogService.class, "LOGGER", logger);
    setInternalState(ApiResponseEntityExceptionHandler.class, "log", logger);
    TenantContext.clear();
  }

  @Test
  public void userExists_Should_Return404_When_UserDoesNotExist() throws Exception {
    /* given */
    val username = "john@doe.com";
    when(identityClient.isUsernameAvailable(username)).thenReturn(Boolean.TRUE);
    /* when */
    mvc.perform(get("/users/{username}", username).accept(MediaType.APPLICATION_JSON))
        /* then */
        .andExpect(status().isNotFound());
  }

  @Test
  public void userExists_Should_Return200_When_UserDoesExist() throws Exception {
    /* given */
    val username = "john@doe.com";
    when(identityClient.isUsernameAvailable(username)).thenReturn(Boolean.FALSE);

    /* when */
    mvc.perform(get("/users/{username}", username).accept(MediaType.APPLICATION_JSON))
        /* then */
        .andExpect(status().isOk());
  }

  /** Method: registerUser */
  @Test
  public void registerUser_Should_ReturnBadRequest_WhenProvidedWithInvalidRequestBody()
      throws Exception {

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(INVALID_USER_REQUEST_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      registerUser_Should_ReturnBadRequest_WhenProvidedWithConsultingTypeWithMandatoryFieldsAndInvalidAge()
          throws Exception {

    when(consultingTypeManager.getConsultingTypeSettings(1))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(INVALID_U25_USER_REQUEST_BODY_AGE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      registerUser_Should_ReturnBadRequest_WhenProvidedWithConsultingTypeWithMandatoryFieldsAndInvalidState()
          throws Exception {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_FIELDS
                    .getRegistration()
                    .getMandatoryFields()));

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(INVALID_U25_USER_REQUEST_BODY_STATE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerUser_Should_ReturnBadRequest_WhenProvidedUsernameIsTooShort()
      throws Exception {

    when(consultingTypeManager.getConsultingTypeSettings(0))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_FIELDS);

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(USER_REQUEST_BODY_WITH_USERNAME_TOO_SHORT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      activateTwoFactorAuthByApp_Should_NotActivateIfSingleTenantAdminButNotConfiguredToUse2Fa()
          throws Exception {
    when(authenticatedUser.isSingleTenantAdmin()).thenReturn(true);
    when(identityClientConfig.getOtpAllowedForSingleTenantAdmins()).thenReturn(false);

    mvc.perform(
            put(PATH_ACTIVATE_2FA)
                .content(ACTIVATE_2FA_BODY)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  public void
      activateTwoFactorAuthByApp_Should_NotActivateIfTenantSuperAdminButNotConfiguredToUse2Fa()
          throws Exception {
    when(authenticatedUser.isTenantSuperAdmin()).thenReturn(true);
    when(identityClientConfig.getOtpAllowedForTenantSuperAdmins()).thenReturn(false);

    mvc.perform(
            put(PATH_ACTIVATE_2FA)
                .content(ACTIVATE_2FA_BODY)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  public void activateTwoFactorAuthByApp_Should_Activate() throws Exception {
    when(authenticatedUser.getUsername()).thenReturn("username");
    when(identityManager.setUpOneTimePassword(anyString(), anyString(), anyString()))
        .thenReturn(true);

    mvc.perform(
            put(PATH_ACTIVATE_2FA)
                .content(ACTIVATE_2FA_BODY)
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void registerUser_Should_ReturnBadRequest_WhenProvidedUsernameIsTooLong()
      throws Exception {

    when(consultingTypeManager.getConsultingTypeSettings(0))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_FIELDS);

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(USER_REQUEST_BODY_WITH_USERNAME_TOO_LONG)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      registerUser_Should_ReturnCreated_WhenProvidedWithValidRequestBodyAndKeycloakResponseIsSuccessful()
          throws Exception {
    when(userHelper.isUsernameValid(anyString())).thenReturn(true);
    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_FIELDS
                    .getRegistration()
                    .getMandatoryFields()));

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(VALID_USER_REQUEST_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  public void
      registerUser_Should_ReturnCreated_WhenProvidedWithValidU25RequestBodyAndKeycloakResponseIsSuccessful()
          throws Exception {
    when(userHelper.isUsernameValid(anyString())).thenReturn(true);
    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_FIELDS
                    .getRegistration()
                    .getMandatoryFields()));

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(VALID_U25_USER_REQUEST_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  public void
      registerUser_Should_ReturnConflict_WhenProvidedWithValidRequestBodyAndKeycloakResponseIsConflict()
          throws Exception {
    when(userHelper.isUsernameValid(anyString())).thenReturn(true);
    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_FIELDS
                    .getRegistration()
                    .getMandatoryFields()));
    doThrow(new CustomValidationHttpStatusException(USERNAME_NOT_AVAILABLE, HttpStatus.CONFLICT))
        .when(createUserFacade)
        .createUserAccountWithInitializedConsultingType(Mockito.any());

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(VALID_U25_USER_REQUEST_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  public void registerUser_Should_ReturnBadRequest_When_PostcodeIsMissing() throws Exception {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_SUCHT.getRegistration().getMandatoryFields()));

    mvc.perform(
            post(PATH_POST_REGISTER_USER)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_USER_REQUEST_BODY_WITH_INVALID_POSTCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerUser_Should_ReturnBadRequest_When_PostcodeIsInvalid() throws Exception {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_SUCHT.getRegistration().getMandatoryFields()));

    mvc.perform(
            post(PATH_POST_REGISTER_USER)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_USER_REQUEST_BODY_WITOUT_POSTCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /** Method: registerNewConsultingType */
  @Test
  public void
      registerNewConsultingType_Should_ReturnBadRequest_When_ProvidedWithInvalidRequestBody()
          throws Exception {
    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_USER_REQUEST_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingType_Should_ReturnBadRequest_When_PostcodeIsInvalid()
      throws Exception {
    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_NEW_REGISTRATION_BODY_WITH_INVALID_POSTCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingType_Should_ReturnBadRequest_When_PostcodeIsMissing()
      throws Exception {
    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_NEW_REGISTRATION_BODY_WITHOUT_POSTCODE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingType_Should_ReturnBadRequest_When_AgencyIdMissing()
      throws Exception {
    when(consultingTypeManager.getConsultingTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_NEW_REGISTRATION_BODY_WITHOUT_AGENCY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingType_Should_ReturnBadRequest_When_ConsultingTypeMissing()
      throws Exception {
    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(INVALID_NEW_REGISTRATION_BODY_WITHOUT_CONSULTING_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingType_Should_ReturnBadRequest_When_RcUserIdIsMissing()
      throws Exception {
    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(VALID_NEW_REGISTRATION_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingType_Should_ReturnBadRequest_When_RcTokenIsMissing()
      throws Exception {
    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .content(VALID_NEW_REGISTRATION_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerNewConsultingTyp_Should_ReturnCreated_When_ProvidedWithValidRequestBody()
      throws Exception {

    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    when(createNewConsultingTypeFacade.initializeNewConsultingType(
            any(), any(), any(RocketChatCredentials.class)))
        .thenReturn(new NewRegistrationResponseDto().sessionId(1L).status(HttpStatus.CREATED));
    when(consultingTypeManager.getConsultingTypeSettings(any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    mvc.perform(
            post(PATH_POST_REGISTER_NEW_CONSULTING_TYPE)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .content(VALID_NEW_REGISTRATION_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  /** Method: acceptEnquiry */
  @Test
  public void acceptEnquiry_Should_ReturnInternalServerError_WhenNoConsultantInDbFound()
      throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant())
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            put(PATH_ACCEPT_ENQUIRY + SESSION_ID)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  public void acceptEnquiry_Should_ReturnInternalServerError_WhenSessionNotFoundInDb()
      throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.empty());
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            put(PATH_ACCEPT_ENQUIRY + SESSION_ID)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verify(logger, atLeastOnce()).error(anyString(), anyLong());
  }

  @Test
  public void acceptEnquiry_Should_ReturnInternalServerError_WhenSessionHasNoRocketChatGroupId()
      throws Exception {

    when(sessionService.getSession(SESSION_ID))
        .thenReturn(Optional.of(TEAM_SESSION_WITHOUT_GROUP_ID));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            put(PATH_ACCEPT_ENQUIRY + SESSION_ID)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verify(logger, atLeastOnce()).error(anyString(), anyLong());
  }

  @Test
  public void acceptEnquiry_Should_ReturnSuccess_WhenAcceptEnquiryIsSuccessfull() throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(TEAM_SESSION));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            put(PATH_ACCEPT_ENQUIRY + SESSION_ID)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void acceptEnquiry_Should_ReturnConflict_WhenEnquiryIsAlreadyAssigned() throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(TEAM_SESSION));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    doThrow(new ConflictException(""))
        .when(assignEnquiryFacade)
        .assignRegisteredEnquiry(TEAM_SESSION, TEAM_CONSULTANT);

    mvc.perform(
            put(PATH_ACCEPT_ENQUIRY + SESSION_ID)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.CONFLICT.value()));
  }

  @Test
  public void
      acceptEnquiry_Should_ReturnInternalServerError_WhenAuthenticatedUserIsNotPresentInApplicationDb()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            post(PATH_CREATE_ENQUIRY_MESSAGE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, "xxx")
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, "xxx")
                .content(VALID_ENQUIRY_MESSAGE_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @Test
  public void createEnquiryMessage_Should_ReturnConflict_WhenMessageIsAlreadyCreated()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    doThrow(new ConflictException(ERROR))
        .when(createEnquiryMessageFacade)
        .createEnquiryMessage(any(EnquiryData.class));

    mvc.perform(
            post(PATH_CREATE_ENQUIRY_MESSAGE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .content(VALID_ENQUIRY_MESSAGE_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.CONFLICT.value()));
  }

  @Test
  public void createEnquiryMessage_Should_ReturnCreated_WhenMessageWasCreated() throws Exception {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);
    var expectedRCCredentials =
        RocketChatCredentials.builder()
            .rocketChatToken(RC_TOKEN)
            .rocketChatUserId(RC_USER_ID)
            .build();
    var expectedEnquiryData =
        new EnquiryData(USER, SESSION_ID, MESSAGE, null, expectedRCCredentials);
    when(createEnquiryMessageFacade.createEnquiryMessage(expectedEnquiryData))
        .thenReturn(
            new CreateEnquiryMessageResponseDTO().rcGroupId(RC_GROUP_ID).sessionId(SESSION_ID));

    mvc.perform(
            post(PATH_CREATE_ENQUIRY_MESSAGE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .content(VALID_ENQUIRY_MESSAGE_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.CREATED.value()))
        .andExpect(jsonPath("$.sessionId", is(SESSION_ID.intValue())))
        .andExpect(jsonPath("$.rcGroupId", is(RC_GROUP_ID)));
  }

  @Test
  public void
      createEnquiryMessage_Should_ReturnInternalServerError_WhenAuthenticatedUserIsNotPresentInApplicationDb()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            post(PATH_CREATE_ENQUIRY_MESSAGE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, "xxx")
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, "xxx")
                .content(VALID_ENQUIRY_MESSAGE_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  /** Method: getSessionsForAuthenticatedUser (role: user) */
  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnUserSessionsAndOk_WhenAuthorized()
      throws Exception {

    List<UserSessionResponseDTO> sessions = new ArrayList<>();
    sessions.add(USER_SESSION_RESPONSE_DTO);
    UserSessionListResponseDTO response = new UserSessionListResponseDTO().sessions(sessions);

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);

    when(sessionListFacade.retrieveSortedSessionsForAuthenticatedUser(anyString(), Mockito.any()))
        .thenReturn(response);

    var displayName = RandomStringUtils.randomAlphanumeric(16);
    Map<String, Object> map = Map.of("displayName", displayName);
    when(userDtoMapper.displayNameOf(map)).thenReturn(displayName);
    when(accountManager.findConsultantByUsername(anyString())).thenReturn(Optional.of(map));

    response.getSessions().get(0).getConsultant().setDisplayName(displayName);
    var sessionsJson = objectMapper.writeValueAsString(response);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("sessions[0].consultant.displayName", is(displayName)))
        .andExpect(content().json(sessionsJson));

    verify(accountProvider, atLeastOnce()).retrieveValidatedUser();
  }

  @Test
  public void
      getSessionsForAuthenticatedUser_Should_ReturnInternalServerError_WhenAuthorizedButUserNotFound()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

    verify(sessionListFacade, times(0))
        .retrieveSortedSessionsForAuthenticatedUser(Mockito.any(), Mockito.any());
  }

  @Test
  public void
      getSessionsForAuthenticatedUser_Should_ReturnNoContent_WhenAuthorizedAndNoOpenSessionsAvailableAndSessionListIsEmpty()
          throws Exception {
    List<UserSessionResponseDTO> session = new ArrayList<>();
    UserSessionListResponseDTO response = new UserSessionListResponseDTO().sessions(session);

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenReturn(USER);

    when(sessionListFacade.retrieveSortedSessionsForAuthenticatedUser(anyString(), Mockito.any()))
        .thenReturn(response);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnBadRequest_WhenHeaderParamIsMissing()
      throws Exception {

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateAbsence_Should_ReturnOk_When_Saved() throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedTeamConsultant()).thenReturn(TEAM_CONSULTANT);

    var validAbsentMessageBody = "{\"absent\": true, \"message\": \"" + MESSAGE + "\"}";
    mvc.perform(
            put(PATH_PUT_CONSULTANT_ABSENT)
                .content(validAbsentMessageBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void updateAbsence_Should_ReturnBadRequest_When_RequestBodyIsMissing() throws Exception {
    mvc.perform(
            put(PATH_PUT_CONSULTANT_ABSENT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /** Method: getSessionsForAuthenticatedConsultant (role: consultant) */
  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenStatusParamIsMissing()
          throws Exception {
    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_STATUS)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnUnauthorized_WhenUnauthorizedExceptionIsRaised()
          throws Exception {
    var runtimeException = easyRandom.nextObject(RuntimeException.class);
    var unauthorizedException = new RocketChatUnauthorizedException("userId", runtimeException);
    when(accountProvider.retrieveValidatedConsultant()).thenThrow(unauthorizedException);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    var stackTrace = ExceptionUtils.getStackTrace(unauthorizedException);
    verify(logger).warn(stackTrace);
    assertTrue(
        stackTrace.contains(
            "Could not get Rocket.Chat subscriptions for user ID userId: Token is not active (401 Unauthorized)"));
    assertTrue(
        stackTrace.startsWith(
            "de.caritas.cob.userservice.api.exception.httpresponses.RocketChatUnauthorizedException:"));
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenHeaderParamIsMissing()
          throws Exception {
    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnInternalServerError_WhenNoConsultantInDbFound()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant())
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnSuccess_WhenAuthorizedAndSessionAvailable()
          throws Exception {
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnNoContent_WhenAuthorizedAndNoSessionsAvailable()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnNoContent_WhenAuthorizedAndSessionListIsEmpty()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamOffestIsMissing()
          throws Exception {

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_OFFSET)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamOffestHasANegativeValue()
          throws Exception {

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_OFFSET)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamCountIsMissing()
          throws Exception {

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_COUNT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamCountHasANegativeValue()
          throws Exception {

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_COUNT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getSessionsForAuthenticatedConsultant_Should_ReturnNotContent_WhenFilterParamIsInvalid()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_INVALID_FILTER)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  /** Method: getUserData (role: consultant / user) */
  @Test
  public void
      getUserData_ForUser_Should_ReturnInternalServerError_WhenAuthenticatedUserIsNotPresentInApplicationDb()
          throws Exception {

    when(authenticatedUser.getRoles()).thenReturn(ROLES_WITH_USER);
    when(authenticatedUser.getGrantedAuthorities())
        .thenReturn(new HashSet<>(Authority.getAuthoritiesByUserRole(UserRole.USER)));
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(accountProvider.retrieveValidatedUser()).thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            get(PATH_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(RC_TOKEN_COOKIE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @Test
  public void getUserData_ForSingleTenantAdmin_Should_ReturnUserDataFromKeycloak()
      throws Exception {
    when(authenticatedUser.isSingleTenantAdmin()).thenReturn(true);
    when(keycloakUserDataProvider.retrieveAuthenticatedUserData())
        .thenReturn(new UserDataResponseDTO());

    mvc.perform(
            get(PATH_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(RC_TOKEN_COOKIE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void getUserData_ForTenantSuperAdmin_Should_ReturnUserDataFromKeycloak() throws Exception {
    when(authenticatedUser.isTenantSuperAdmin()).thenReturn(true);
    when(keycloakUserDataProvider.retrieveAuthenticatedUserData())
        .thenReturn(new UserDataResponseDTO());

    mvc.perform(
            get(PATH_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(RC_TOKEN_COOKIE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void getUserData_ForAgencySuperAdmin_Should_ReturnUserDataFromKeycloak() throws Exception {
    when(authenticatedUser.isAgencySuperAdmin()).thenReturn(true);
    when(keycloakUserDataProvider.retrieveAuthenticatedUserData())
        .thenReturn(new UserDataResponseDTO());

    mvc.perform(
            get(PATH_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(RC_TOKEN_COOKIE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void getUserData_ForRestrictedAgencyAdmin_Should_ReturnUserDataFromKeycloak()
      throws Exception {
    when(authenticatedUser.isRestrictedAgencyAdmin()).thenReturn(true);
    when(keycloakUserDataProvider.retrieveAuthenticatedUserData())
        .thenReturn(new UserDataResponseDTO());

    mvc.perform(
            get(PATH_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(RC_TOKEN_COOKIE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  /** Method: getTeamSessionsForAuthenticatedConsultant (role: consultant) */
  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenHeaderParamIsMissing()
          throws Exception {

    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnInternalServerError_WhenNoConsultantInDbFound()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedTeamConsultant())
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnForbidden_WhenConsultantIsNoTeamConsultant()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedTeamConsultant()).thenThrow(new ForbiddenException(""));

    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verify(logger, atLeastOnce()).warn(anyString(), anyString());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnNoContent_WhenAuthorizedAndNoSessionsAvailable()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnSucess_WhenAuthorizedAndSessionsAvailable()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamOffestIsMissing()
          throws Exception {
    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_OFFSET)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamOffestHasANegativeValue()
          throws Exception {
    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_OFFSET)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamCountIsMissing()
          throws Exception {
    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_COUNT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnBadRequest_WhenParamCountHasANegativeValue()
          throws Exception {
    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_COUNT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      getTeamSessionsForAuthenticatedConsultant_Should_ReturnNotContent_WhenFilterParamIsInvalid()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);

    mvc.perform(
            get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_INVALID_FILTER)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  /** sendNewMessageNotification() */
  @Test
  public void
      sendNewMessageNotification_Should_CallEmailNotificationFacadeAndReturn2xxSuccessful_WhenCalled()
          throws Exception {
    var validNewMessageRequestBody = "{\"rcGroupId\": \"" + RC_GROUP_ID + "\"}";
    mvc.perform(
            post(PATH_SEND_NEW_MESSAGE_NOTIFICATION)
                .content(validNewMessageRequestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());

    verify(emailNotificationFacade, atLeastOnce())
        .sendNewMessageNotification(
            RC_GROUP_ID, authenticatedUser.getRoles(), authenticatedUser.getUserId(), null);
  }

  /** Method: getConsultants (authority: VIEW_AGENCY_CONSULTANTS) */
  @Test
  public void getConsultants_Should_ReturnBadRequest_WhenQueryParamIsMissing() throws Exception {

    mvc.perform(
            get(PATH_GET_CONSULTANTS_FOR_AGENCY_WITHOUT_PARAM)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getConsultants_Should_ReturnNoContent_WhenNoConsultantInDbFound() throws Exception {

    mvc.perform(
            get(PATH_GET_CONSULTANTS_FOR_AGENCY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void
      getConsultants_Should_ReturnInternalServerError_WhenConsultantAgencyServiceThrowsException()
          throws Exception {

    when(consultantAgencyService.getConsultantsOfAgency(Mockito.anyLong()))
        .thenThrow(new ServiceException(ERROR));

    mvc.perform(
            get(PATH_GET_CONSULTANTS_FOR_AGENCY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @Test
  public void
      getConsultants_Should_ReturnOkAndValidContent_WhenConsultantAgencyServiceReturnsListWithEntries()
          throws Exception {

    when(consultantAgencyService.getConsultantsOfAgency(Mockito.anyLong()))
        .thenReturn(CONSULTANT_RESPONSE_DTO_LIST);

    var validConsultantResponseDtoResult =
        "[{\"consultantId\": \""
            + CONSULTANT_ID
            + "\", \"firstName\": \""
            + FIRST_NAME
            + "\", \"lastName\": \""
            + LAST_NAME
            + "\"}]";
    mvc.perform(
            get(PATH_GET_CONSULTANTS_FOR_AGENCY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(validConsultantResponseDtoResult));
  }

  /** Method: assignSession (role: consultant) */
  @Test
  public void assignSession_Should_ReturnBadRequest_WhenQueryParamsAreInvalid() throws Exception {

    mvc.perform(
            put(PATH_PUT_ASSIGN_SESSION_INVALID_PARAMS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void
      assignSession_Should_ReturnInternalServerErrorAndLogError_WhenSessionServiceThrowsException()
          throws Exception {

    when(sessionService.getSession(Mockito.anyLong())).thenThrow(new ServiceException(ERROR));

    mvc.perform(
            put(PATH_PUT_ASSIGN_SESSION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  public void assignSession_Should_ReturnHttpStatusOfAssignSessionFacade() throws Exception {

    when(accountProvider.retrieveValidatedConsultantById(any())).thenReturn(TEAM_CONSULTANT);
    when(sessionService.getSession(Mockito.anyLong())).thenReturn(Optional.of(SESSION));
    when(authenticatedUser.getGrantedAuthorities())
        .thenReturn(AUTHORITIES_ASSIGN_SESSION_AND_ENQUIRY);
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    doThrow(new ConflictException(""))
        .when(assignSessionFacade)
        .assignSession(SESSION, TEAM_CONSULTANT, CONSULTANT);

    mvc.perform(
            put(PATH_PUT_ASSIGN_SESSION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.CONFLICT.value()));
  }

  @Test
  public void
      assignSession_Should_ReturnInternalServerErrorAndLogError_WhenConsultantIsNotFoundInDb()
          throws Exception {

    when(accountProvider.retrieveValidatedConsultantById(anyString()))
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            put(PATH_PUT_ASSIGN_SESSION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verify(logger, atLeastOnce()).error(anyString(), anyLong());
  }

  @Test
  public void assignSession_Should_ReturnInternalServerErrorAndLogError_WhenSessionIsNotFoundInDb()
      throws Exception {

    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(sessionService.getSession(Mockito.anyLong())).thenReturn(Optional.empty());

    mvc.perform(
            put(PATH_PUT_ASSIGN_SESSION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verify(logger, atLeastOnce()).error(anyString(), anyLong());
  }

  @Test
  public void
      assignSession_Should_ReturnForbiddenAndLogError_WhenCallerDoesNotHaveTheRightToAssignEnquiries()
          throws Exception {

    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(sessionService.getSession(Mockito.anyLong()))
        .thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(AUTHORITY_ASSIGN_SESSION);

    mvc.perform(
            put(PATH_PUT_ASSIGN_SESSION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

    verify(logger, atLeastOnce()).warn(anyString(), anyString(), anyString());
  }

  @Test
  public void registerUser_Should_DecodePassword() throws Exception {
    when(userHelper.isUsernameValid(anyString())).thenReturn(true);
    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(anyString()))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_FIELDS
                    .getRegistration()
                    .getMandatoryFields()));

    mvc.perform(
            post(PATH_REGISTER_USER)
                .content(VALID_USER_REQUEST_BODY_WITH_ENCODED_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    ArgumentCaptor<UserDTO> argument = ArgumentCaptor.forClass(UserDTO.class);
    verify(createUserFacade, times(1))
        .createUserAccountWithInitializedConsultingType(argument.capture());
    assertEquals(DECODED_PASSWORD, argument.getValue().getPassword());
  }

  /** updatePassword() */
  @Test
  public void updatePassword_Should_ReturnBadRequest_When_PasswordsAreMissing() throws Exception {

    mvc.perform(
            put(PATH_PUT_UPDATE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

    verify(identityClient, times(0)).changePassword(anyString(), anyString());
  }

  @Test
  public void updateKey_Should_ReturnConflict_WhenProvidedWithSameKey() throws Exception {

    when(encryptionService.getMasterKey()).thenReturn(MASTER_KEY_1);

    mvc.perform(
            post(PATH_UPDATE_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MASTER_KEY_DTO_KEY_1)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  public void updateKey_Should_ReturnAccepted_WhenProvidedWithNewKey() throws Exception {

    when(encryptionService.getMasterKey()).thenReturn(MASTER_KEY_1);

    mvc.perform(
            post(PATH_UPDATE_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MASTER_KEY_DTO_KEY_2)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void createChat_Should_ReturnBadRequest_WhenQueryParamsAreInvalid() throws Exception {

    mvc.perform(
            post(PATH_POST_CHAT_NEW)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(accountProvider);
  }

  @Test
  public void createChat_Should_ReturnInternalServerErrorAndLogError_When_ChatCouldNotBeCreated()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(createChatFacade.createChatV1(Mockito.any(), Mockito.any()))
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            post(PATH_POST_CHAT_NEW)
                .content(giveValidCreateChatBodyWithAgencyId(1L))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  private String giveValidCreateChatBodyWithAgency(ConsultantAgency consultantAgency) {
    return VALID_CREATE_CHAT_BODY_WITH_AGENCY_PLACEHOLDER.replace(
        "${AGENCY_ID}", consultantAgency.getAgencyId().toString());
  }

  private String giveValidCreateChatBodyWithAgencyId(Long agencyId) {
    return VALID_CREATE_CHAT_BODY_WITH_AGENCY_PLACEHOLDER.replace(
        "${AGENCY_ID}", agencyId.toString());
  }

  @Test
  public void createChat_Should_ReturnCreated_When_ChatWasCreated() throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(createChatFacade.createChatV1(Mockito.any(), Mockito.any()))
        .thenReturn(CREATE_CHAT_RESPONSE_DTO);

    mvc.perform(
            post(PATH_POST_CHAT_NEW)
                .content(giveValidCreateChatBodyWithAgencyId(1L))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.CREATED.value()));
  }

  @Test
  public void createChatV2_Should_ReturnBadRequest_WhenQueryParamsAreInvalid() throws Exception {

    mvc.perform(
            post(PATH_POST_CHAT_NEW_V2)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(accountProvider);
  }

  @Test
  public void createChatV2_Should_ReturnInternalServerErrorAndLogError_When_ChatCouldNotBeCreated()
      throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(createChatFacade.createChatV2(Mockito.any(), Mockito.any()))
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            post(PATH_POST_CHAT_NEW_V2)
                .content(giveValidCreateChatBodyWithAgencyId(1L))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @Test
  public void createChatV2_Should_ReturnCreated_When_ChatWasCreated() throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(createChatFacade.createChatV2(Mockito.any(), Mockito.any()))
        .thenReturn(CREATE_CHAT_RESPONSE_DTO);

    mvc.perform(
            post(PATH_POST_CHAT_NEW_V2)
                .content(giveValidCreateChatBodyWithAgencyId(1L))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.CREATED.value()));
  }

  /** Method: startChat */
  @Test
  public void startChat_Should_ReturnBadRequest_WhenPathParamsAreInvalid() throws Exception {

    mvc.perform(
            put(PATH_PUT_CHAT_START_WITH_INVALID_PATH_PARAMS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  public void startChat_Should_ReturnOK_When_ChatWasStarted() throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(chatService.getChat(Mockito.any())).thenReturn(Optional.of(INACTIVE_CHAT));

    mvc.perform(
            put(PATH_PUT_CHAT_START)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void startChat_Should_ReturnBadRequest_When_StartChatThrowsBadRequest() throws Exception {
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(chatService.getChat(Mockito.any())).thenReturn(Optional.empty());

    mvc.perform(
            put(PATH_PUT_CHAT_START)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    // prints stack trace
    verify(logger).warn(contains("Bad Request:"), any(BadRequestException.class));
  }

  @Test
  public void startChat_Should_ReturnBadRequest_When_ChatWasNotFound() throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant())
        .thenThrow(new InternalServerErrorException(""));
    when(chatService.getChat(Mockito.any())).thenReturn(Optional.empty());

    mvc.perform(
            put(PATH_PUT_CHAT_START)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /** Method: getChat */
  @Test
  public void getChat_Should_ReturnBadRequest_WhenPathParamsAreInvalid() throws Exception {

    mvc.perform(
            get(PATH_GET_CHAT_WITH_INVALID_PATH_PARAMS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(accountProvider);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  public void getChat_Should_ReturnOk_When_RequestOk() throws Exception {

    mvc.perform(
            get(PATH_GET_CHAT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(getChatFacade, times(1)).getChat(Mockito.any());
  }

  @Test
  public void assignChat_Should_ReturnOk_When_ChatWasAssigned() throws Exception {
    mvc.perform(
            put(PATH_PUT_ASSIGN_CHAT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(assignChatFacade).assignChat(RC_GROUP_ID, authenticatedUser);
  }

  /** Method: joinChat */
  @Test
  public void joinChat_Should_ReturnBadRequest_WhenPathParamsAreInvalid() throws Exception {

    mvc.perform(
            put(PATH_PUT_JOIN_CHAT_WITH_INVALID_PATH_PARAMS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(joinAndLeaveChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(accountProvider);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  public void joinChat_Should_ReturnOk_When_ChatWasJoined() throws Exception {

    mvc.perform(
            put(PATH_PUT_JOIN_CHAT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(joinAndLeaveChatFacade, times(1)).joinChat(Mockito.any(), Mockito.any());
  }

  @Test
  public void stopChat_Should_ReturnBadRequest_WhenQueryParamsAreInvalid() throws Exception {

    mvc.perform(
            put(PATH_PUT_CHAT_STOP_INVALID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(accountProvider);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  public void stopChat_Should_ReturnInternalServerError_When_CallingConsultantDoesNotExist()
      throws Exception {

    when(chatService.getChat(any())).thenReturn(Optional.of(mock(Chat.class)));
    when(accountProvider.retrieveValidatedConsultant())
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(put(PATH_PUT_CHAT_STOP).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  public void stopChat_Should_ReturnBadRequest_When_ChatNotFound() throws Exception {

    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(chatService.getChat(Mockito.anyLong())).thenReturn(Optional.empty());

    mvc.perform(put(PATH_PUT_CHAT_STOP).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void stopChat_Should_ReturnOk_When_ChatWasStopped() throws Exception {

    when(accountProvider.retrieveValidatedConsultant()).thenReturn(TEAM_CONSULTANT);
    when(chatService.getChat(Mockito.anyLong())).thenReturn(Optional.of(chat));

    mvc.perform(put(PATH_PUT_CHAT_STOP).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  /** Method: getChat */
  @Test
  public void getChatMembers_Should_ReturnBadRequest_WhenPathParamsAreInvalid() throws Exception {

    mvc.perform(
            get(PATH_GET_CHAT_MEMBERS_WITH_INVALID_PATH_PARAMS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(accountProvider);
    verifyNoMoreInteractions(chatPermissionVerifier);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  public void getChatMembers_Should_ReturnOk_When_RequestOk() throws Exception {

    mvc.perform(
            get(PATH_GET_CHAT_MEMBERS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(getChatMembersFacade, times(1)).getChatMembers(Mockito.any());
  }

  /** Method: updateChat */
  @Test
  public void updateChat_Should_ReturnBadRequest_WhenPathParamsAreInvalid() throws Exception {

    mvc.perform(
            put(PATH_PUT_UPDATE_CHAT_INVALID_PATH_PARAMS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  public void updateChat_Should_ReturnOk_When_RequestOk() throws Exception {

    mvc.perform(
            put(PATH_PUT_UPDATE_CHAT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_CHAT_BODY)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(chatService, times(1)).updateChat(Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /** Method: fetchSessionForConsultant */
  @Test
  public void fetchSessionForConsultant_Should_ReturnOk_WhenRequestOk() throws Exception {

    mvc.perform(
            get(PATH_GET_SESSION_FOR_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(sessionService, atLeastOnce()).fetchSessionForConsultant(Mockito.any(), Mockito.any());
  }

  @Test
  public void
      fetchSessionForConsultant_Should_ReturnInternalServerError_WhenAuthorizedButUserNotFound()
          throws Exception {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(accountProvider.retrieveValidatedConsultant())
        .thenThrow(new InternalServerErrorException(""));

    mvc.perform(
            get(PATH_GET_SESSION_FOR_CONSULTANT)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

    verify(sessionService, never()).fetchSessionForConsultant(Mockito.any(), Mockito.any());
  }

  @Test
  public void updateEmailAddress_Should_ReturnOk_When_RequestOk() throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("email")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(accountProvider).changeUserAccountEmailAddress(Optional.of("email"));
  }

  @Test
  public void deleteEmailAddress_Should_ReturnOk_When_RequestOk() throws Exception {
    mvc.perform(delete("/users/email").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(accountProvider).changeUserAccountEmailAddress(Optional.empty());
  }

  @Test
  public void updateEmailAddress_Should_ReturnBadRequest_When_bodyIsEmpty() throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(accountProvider);
  }

  @Test
  public void
      deactivateAndFlagUserAccountForDeletion_Should_ReturnBadRequest_When_BodyValuesAreMissing()
          throws Exception {

    mvc.perform(
            delete(PATH_DELETE_FLAG_USER_DELETED)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  public void deleteSessionAndInactiveUser_Should_ReturnNotFound_When_SessionIdIsUnknown()
      throws Exception {
    var sessionId = easyRandom.nextLong();

    doThrow(new NotFoundException("")).when(sessionDeleteService).deleteSession(sessionId);
    mvc.perform(delete("/users/sessions/{sessionId}", sessionId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(sessionDeleteService).deleteSession(sessionId);
  }

  @Test
  public void deleteSessionAndInactiveUser_Should_ReturnOK_When_SessionIdIsKnown()
      throws Exception {
    var sessionId = givenAPresentSession(false);

    mvc.perform(delete("/users/sessions/{sessionId}", sessionId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(sessionDeleteService).deleteSession(sessionId);
  }

  @Test
  public void updateMobileToken_Should_ReturnOk_When_RequestOk() throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_MOBILE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MobileTokenDTO().token("token")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(accountProvider, times(1)).updateUserMobileToken("token");
  }

  @Test
  public void updateMobileToken_Should_ReturnBadRequest_When_bodyIsEmpty() throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_MOBILE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(accountProvider);
  }

  @Test
  public void updateSessionData_Should_ReturnBadRequest_When_BodyIsEmpty() throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_SESSION_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(sessionDataService);
  }

  @Test
  public void updateSessionData_Should_ReturnBadRequest_When_PathVariableIsInvalid()
      throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_SESSION_DATA_INVALID_PATH_VAR)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(sessionDataService);
  }

  @Test
  public void updateSessionData_Should_ReturnOk_When_RequestIsOk() throws Exception {
    mvc.perform(
            put(PATH_PUT_UPDATE_SESSION_DATA)
                .content(objectMapper.writeValueAsString(new SessionDTO()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void updateUserData_Should_ReturnNotFound_When_ConsultantDoesNotExist() throws Exception {
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.empty());
    var updateConsultant =
        objectMapper.writeValueAsString(givenAMinimalUpdateConsultantDto(givenAValidEmail()));

    mvc.perform(
            put(PATH_GET_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateConsultant)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  public void updateUserData_Should_ReturnBadRequest_When_PathVariableIsInvalid() throws Exception {
    mvc.perform(
            put(PATH_GET_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  public void updateUserData_Should_ReturnBadRequest_When_LanguageIsInvalid() throws Exception {
    var updateConsultantDTO = givenAnUpdateConsultantDtoWithInvalidLanguage();

    mvc.perform(
            put(PATH_GET_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateConsultantDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(authenticatedUser);
  }

  @Test
  public void updateUserData_Should_ReturnOk_When_RequestIsOk() throws Exception {
    var consultant = givenAValidConsultant();
    var updateConsultantDTO = givenAMinimalUpdateConsultantDto(consultant.getEmail());

    mvc.perform(
            put(PATH_GET_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateConsultantDTO))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var captor = ArgumentCaptor.forClass(UpdateAdminConsultantDTO.class);
    verify(consultantUpdateService).updateConsultant(any(), captor.capture());

    var updateAdminConsultantDTO = captor.getValue();
    assertEquals(updateConsultantDTO.getEmail().toLowerCase(), updateAdminConsultantDTO.getEmail());
    assertEquals(updateConsultantDTO.getFirstname(), updateAdminConsultantDTO.getFirstname());
    assertEquals(updateConsultantDTO.getLastname(), updateAdminConsultantDTO.getLastname());
    assertEquals(consultant.isAbsent(), updateAdminConsultantDTO.getAbsent());
    assertEquals(consultant.isLanguageFormal(), updateAdminConsultantDTO.getFormalLanguage());
    assertEquals(consultant.getAbsenceMessage(), updateAdminConsultantDTO.getAbsenceMessage());
  }

  @Test
  public void updateUserData_Should_ReturnBadRequest_When_emailAddressIsNotValid()
      throws Exception {
    var updateConsultantDto = givenAMinimalUpdateConsultantDto("invalid");

    mvc.perform(
            put(PATH_GET_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateConsultantDto))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void archiveSession_Should_ReturnBadRequest_When_PathVariableIsInvalid() throws Exception {
    mvc.perform(
            put(PATH_ARCHIVE_SESSION_INVALID_PATH_VAR)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  public void archiveSession_Should_ReturnOk_When_RequestIsOk() throws Exception {
    mvc.perform(put(PATH_ARCHIVE_SESSION)).andExpect(status().isOk());
  }

  @Test
  public void dearchiveSession_Should_ReturnBadRequest_When_PathVariableIsInvalid()
      throws Exception {
    mvc.perform(
            put(PATH_DEARCHIVE_SESSION_INVALID_PATH_VAR)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  public void dearchiveSession_Should_ReturnOk_When_RequestIsOk() throws Exception {
    mvc.perform(put(PATH_DEARCHIVE_SESSION)).andExpect(status().isOk());
  }

  @Test
  public void addMobileAppToken_Should_returnOk_When_RequestIsOk() throws Exception {
    mvc.perform(
            put(PATH_PUT_ADD_MOBILE_TOKEN)
                .content(objectMapper.writeValueAsString(new MobileTokenDTO()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void addMobileAppToken_Should_returnBadRequest_When_RequestIsEmpty() throws Exception {
    mvc.perform(put(PATH_PUT_ADD_MOBILE_TOKEN).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getConsultantPublicData_Should_returnOk_When_consultantIdIsGiven() throws Exception {
    givenAValidConsultant();

    mvc.perform(get(PATH_GET_PUBLIC_CONSULTANT_DATA).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(consultantAgencyService)
        .getOnlineAgenciesOfConsultant("65c1095e-b977-493a-a34f-064b729d1d6c");
  }

  @Test
  public void
      updateE2eInChats_Should_returnAccepted_When_adviceSeekerWithoutInitializedSessionIsGiven()
          throws Exception {
    givenAdviceSeekerWithoutInitializedSession(false);

    mvc.perform(
            put("/users/chat/e2e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    new ObjectMapper()
                        .writeValueAsString(
                            new E2eKeyDTO()
                                .publicKey(
                                    "zYnD6llaxxtN2Hnc2njpd3iMW4vwrCxUXXJfi1knTsYIlIbHCsDkUMRnuw38ydaKufuzHXsCjdfYWYSJObduz5rOrRiwZTmxWujcUkrJlTg9ON50gnWbtRro3yrGX9IgoIrGfNmPTSeTasCchJ-S5z6V7OPdRthxQSoqtBtVt4XJD2lbl-fU_c4nzWZ47Gk8kes6kHMdpXtmbVROGbKAH5MVEc6XqW1-FJDVcVVE9ZoQZiPe3slnuJLMgGstnzvDlwwcRetc_9dbQf_QRFZ-_3e_QA3tOnguBnu6naLciffAHET70b-YE1n6IN_zMPL5eC1ses_tFd8CTG3p7Dvo5w"))))
        .andExpect(status().isAccepted());
  }

  @Test
  public void
      updateE2eInChats_Should_returnServerError_When_adviceSeekerWithoutInitializedSessionShouldHaveBeenInitialized()
          throws Exception {
    givenAdviceSeekerWithoutInitializedSession(true);

    mvc.perform(
            put("/users/chat/e2e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    new ObjectMapper()
                        .writeValueAsString(
                            new E2eKeyDTO()
                                .publicKey(
                                    "zYnD6llaxxtN2Hnc2njpd3iMW4vwrCxUXXJfi1knTsYIlIbHCsDkUMRnuw38ydaKufuzHXsCjdfYWYSJObduz5rOrRiwZTmxWujcUkrJlTg9ON50gnWbtRro3yrGX9IgoIrGfNmPTSeTasCchJ-S5z6V7OPdRthxQSoqtBtVt4XJD2lbl-fU_c4nzWZ47Gk8kes6kHMdpXtmbVROGbKAH5MVEc6XqW1-FJDVcVVE9ZoQZiPe3slnuJLMgGstnzvDlwwcRetc_9dbQf_QRFZ-_3e_QA3tOnguBnu6naLciffAHET70b-YE1n6IN_zMPL5eC1ses_tFd8CTG3p7Dvo5w"))))
        .andExpect(status().isInternalServerError());
  }

  private void givenAdviceSeekerWithoutInitializedSession(boolean wasUpdated) {
    var user = new User();
    user.setCreateDate(nowInUtc());
    var updateDate = wasUpdated ? nowInUtc().plusDays(1) : user.getCreateDate();
    user.setUpdateDate(updateDate);
    when(accountProvider.retrieveValidatedUser()).thenReturn(user);
    when(accountManager.findAdviceSeeker(any())).thenReturn(Optional.of(new HashMap<>()));
    when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
  }

  private long givenAPresentSession(boolean isOnlySession) {
    var sessionId = easyRandom.nextLong();
    var session = easyRandom.nextObject(Session.class);
    if (isOnlySession) {
      session.getUser().setSessions(Set.of(session));
    }

    when(sessionService.getSession(sessionId)).thenReturn(Optional.of(session));

    return sessionId;
  }

  private Map<String, Object> givenAnUpdateConsultantDtoWithInvalidLanguage() {
    return Map.of(
        "firstname",
        "firstname",
        "lastname",
        "lastname",
        "email",
        givenAValidEmail(),
        "languages",
        List.of("de", "xx"));
  }

  private UpdateConsultantDTO givenAMinimalUpdateConsultantDto(String email) {
    return new UpdateConsultantDTO().email(email).firstname("firstname").lastname("lastname");
  }

  private Consultant givenAValidConsultant() {
    var consultant = easyRandom.nextObject(Consultant.class);
    consultant.setEmail(givenAValidEmail());
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultant));

    return consultant;
  }

  private String givenAValidEmail() {
    return RandomStringUtils.randomAlphabetic(8)
        + "@"
        + RandomStringUtils.randomAlphabetic(8)
        + ".com";
  }
}
