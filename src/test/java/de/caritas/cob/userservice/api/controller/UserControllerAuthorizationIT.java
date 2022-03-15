package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.api.controller.UserControllerIT.PATH_GET_PUBLIC_CONSULTANT_DATA;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_ACCEPT_ENQUIRY;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_ARCHIVE_SESSION;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_CREATE_ENQUIRY_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_DEARCHIVE_SESSION;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_DELETE_FLAG_USER_DELETED;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_CHAT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_CHAT_MEMBERS;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_CONSULTANTS;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_ENQUIRIES_FOR_AGENCY;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_MONITORING;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_SESSION_FOR_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_GET_USER_DATA;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_CHAT_NEW;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_IMPORT_ASKERS;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_IMPORT_CONSULTANTS;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_NEW_MESSAGE_NOTIFICATION;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_REGISTER_NEW_CONSULTING_TYPE;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_POST_REGISTER_USER;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_ADD_MOBILE_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_ASSIGN_SESSION;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_CHAT_START;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_CHAT_STOP;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_CONSULTANT_ABSENT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_JOIN_CHAT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_LEAVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_UPDATE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_UPDATE_EMAIL;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_UPDATE_MOBILE_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_UPDATE_MONITORING;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_UPDATE_PASSWORD;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_PUT_UPDATE_SESSION_DATA;
import static de.caritas.cob.userservice.api.testHelper.PathConstants.PATH_UPDATE_KEY;
import static de.caritas.cob.userservice.api.testHelper.RequestBodyConstants.VALID_UPDATE_CHAT_BODY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.IdentityManager;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.facade.CreateSessionFacade;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.GetChatFacade;
import de.caritas.cob.userservice.api.facade.GetChatMembersFacade;
import de.caritas.cob.userservice.api.facade.JoinAndLeaveChatFacade;
import de.caritas.cob.userservice.api.facade.StartChatFacade;
import de.caritas.cob.userservice.api.facade.StopChatFacade;
import de.caritas.cob.userservice.api.facade.userdata.AskerDataProvider;
import de.caritas.cob.userservice.api.facade.userdata.ConsultantDataFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.model.EmailDTO;
import de.caritas.cob.userservice.api.model.MobileTokenDTO;
import de.caritas.cob.userservice.api.model.OneTimePasswordDTO;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.AskerImportService;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.archive.SessionArchiveService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.common.util.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UserControllerAuthorizationIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired
  private MockMvc mvc;

  @MockBean
  private UserService userService;
  @MockBean
  private SessionService sessionService;
  @MockBean
  private SessionRepository sessionRepository;
  @MockBean
  private UserRepository userRepository;
  @MockBean
  private LogService logService;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @MockBean
  private ConsultantDataFacade consultantDataFacade;
  @MockBean
  private EmailNotificationFacade emailNotificationFacade;
  @MockBean
  private ConsultantImportService consultantImportService;
  @MockBean
  private MonitoringService monitoringService;
  @MockBean
  private AskerImportService askerImportService;
  @MockBean
  private ConsultantAgencyService consultantAgencyService;
  @MockBean
  private IdentityClient identityClient;
  @MockBean
  private IdentityManager identityManager;
  @MockBean
  private DecryptionService encryptionService;
  @MockBean
  private ChatService chatService;
  @MockBean
  private StartChatFacade startChatFacade;
  @MockBean
  private JoinAndLeaveChatFacade joinChatFacade;
  @MockBean
  private GetChatFacade getChatFacade;
  @MockBean
  private RocketChatService rocketChatService;
  @MockBean
  private ChatPermissionVerifier chatPermissionVerifier;
  @MockBean
  private StopChatFacade stopChatFacade;
  @MockBean
  private GetChatMembersFacade getChatMembersFacade;
  @MockBean
  private UserHelper userHelper;
  @MockBean
  private CreateSessionFacade createSessionFacade;
  @MockBean
  private ValidatedUserAccountProvider validatedUserAccountProvider;
  @MockBean
  private SessionDataService sessionDataService;
  @MockBean
  private SessionArchiveService sessionArchiveService;
  @MockBean
  private ConsultantUpdateService consultantUpdateService;
  @MockBean
  private ConsultantService consultantService;
  @MockBean
  private AskerDataProvider askerDataProvider;

  /**
   * POST on /users/askers/new
   */

  @Test
  public void registerUser_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_REGISTER_USER).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(userService);
  }

  /**
   * POST on /users/askers/consultingType/new (role: asker)
   */

  @Test
  public void registerNewConsultingType_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_POST_REGISTER_NEW_CONSULTING_TYPE).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(
      authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
          AuthorityValue.USE_FEEDBACK, AuthorityValue.TECHNICAL_DEFAULT,
          AuthorityValue.CONSULTANT_DEFAULT,
          AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
          AuthorityValue.START_CHAT,
          AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
          AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void registerNewConsultingType_Should_ReturnForbiddenAndCallNoMethods_WhenNoAskerDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_POST_REGISTER_NEW_CONSULTING_TYPE).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, userService, createSessionFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void registerNewConsultingType_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_POST_REGISTER_NEW_CONSULTING_TYPE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * GET on /users/sessions/open (role: consultant)
   */

  @Test
  public void getOpenSessionsForAuthenticatedConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
      AuthorityValue.USER_ADMIN})
  public void getOpenSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getOpenSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * GET on /users/sessions/consultants/new (role: consultant)
   */

  @Test
  public void getEnquiriesForAgency_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        get(PATH_GET_ENQUIRIES_FOR_AGENCY).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);

  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getEnquiriesForAgency_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(
        get(PATH_GET_ENQUIRIES_FOR_AGENCY).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getEnquiriesForAgency_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_ENQUIRIES_FOR_AGENCY).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * PUT on /users/sessions/new/{sessionId} (role: consultant)
   */
  @Test
  public void acceptEnquiry_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_ACCEPT_ENQUIRY + "/1").cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void acceptEnquiry_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_ACCEPT_ENQUIRY + "/1").cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void acceptEnquiry_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_ACCEPT_ENQUIRY + "/1").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * POST on /users/sessions/askers/new (role: user)
   */

  @Test
  public void createEnquiryMessage_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_CREATE_ENQUIRY_MESSAGE).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository, userService,
        userRepository);
  }

  @Test
  @WithMockUser(
      authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
          AuthorityValue.USE_FEEDBACK, AuthorityValue.TECHNICAL_DEFAULT,
          AuthorityValue.CONSULTANT_DEFAULT,
          AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
          AuthorityValue.START_CHAT,
          AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
          AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void createEnquiryMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_CREATE_ENQUIRY_MESSAGE).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository, userService,
        userRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void createEnquiryMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_CREATE_ENQUIRY_MESSAGE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository, userService,
        userRepository);
  }

  /**
   * GET on /users/sessions/askers (role: user)
   */

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(
      authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
          AuthorityValue.USE_FEEDBACK, AuthorityValue.TECHNICAL_DEFAULT,
          AuthorityValue.CONSULTANT_DEFAULT,
          AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
          AuthorityValue.START_CHAT,
          AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
          AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getSessionsForAuthenticatedUser_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getSessionsForAuthenticatedUser_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * PUT on /users/consultants/absences (role: consultant)
   */

  @Test
  public void updateAbsence_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_CONSULTANT_ABSENT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantDataFacade, logService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateAbsence_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_CONSULTANT_ABSENT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade, logService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateAbsence_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_CONSULTANT_ABSENT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade, logService);
  }

  /**
   * GET on /users/sessions/consultants (role: consultants)
   */

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  public void getUserData_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_USER_DATA).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantDataFacade, logService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getUserData_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthorityOrConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_USER_DATA).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade, logService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  public void getUserData_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_USER_DATA).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade, logService);
  }

  /**
   * GET on /users/sessions/consultants (role: consultants)
   */

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  /**
   * POST on /users/mails/messages/new (role: consultant/user)
   */

  @Test
  public void sendNewMessageNotification_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_POST_NEW_MESSAGE_NOTIFICATION).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(emailNotificationFacade, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void sendNewMessageNotification_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthorityOrConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_POST_NEW_MESSAGE_NOTIFICATION).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(emailNotificationFacade, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void sendNewMessageNotification_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_NEW_MESSAGE_NOTIFICATION).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(emailNotificationFacade, authenticatedUser);
  }

  /**
   * POST on /users/consultants/import (role: technical)
   */

  @Test
  public void importConsultants_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_POST_IMPORT_CONSULTANTS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantImportService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT, AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void importConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_POST_IMPORT_CONSULTANTS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantImportService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TECHNICAL_DEFAULT})
  public void importConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_CONSULTANTS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantImportService);
  }

  /**
   * GET on /users/sessions/{sessionId}/monitoring (role: consultant)
   */
  @Test
  public void getMonitoring_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_GET_MONITORING).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.USER_DEFAULT, AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_GET_MONITORING).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_GET_MONITORING).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  /**
   * PUT on /users/sessions/monitoring/{sessionId} (role: consultant)
   */
  @Test
  public void updateMonitoring_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MONITORING).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.USER_DEFAULT, AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MONITORING).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MONITORING).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  /**
   * POST on /users/askers/import (role: technical)
   */

  @Test
  public void importAskers_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT, AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void importAskers_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TECHNICAL_DEFAULT})
  public void importAskers_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  /**
   * POST on /users/askersWithoutSession/import (role: technical)
   */

  @Test
  public void importAskersWithoutSession_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT,
      AuthorityValue.START_CHAT,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void importAskersWithoutSession_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TECHNICAL_DEFAULT})
  public void importAskersWithoutSession_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  public void getLanguages_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {
    mvc.perform(
        post("/users/consultants/languages")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAgencyService);
  }

  /**
   * GET on /users/consultants (authority: VIEW_AGENCY_CONSULTANTS)
   */

  @Test
  public void getConsultants_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_CONSULTANTS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAgencyService);

  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoViewAgencyConsultantsAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_CONSULTANTS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAgencyService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.VIEW_AGENCY_CONSULTANTS})
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_CONSULTANTS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAgencyService);
  }

  /**
   * PUT on /users/sessions/{sessionId}/consultant/{consultantId} (authority:
   * ASSIGN_CONSULTANT_TO_ENQUIRY and/or ASSIGN_CONSULTANT_TO_SESSION)
   */
  @Test
  public void assignSession_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_ASSIGN_SESSION).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantDataFacade, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY})
  public void assignSession_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_ASSIGN_SESSION).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade, sessionService);
  }

  /**
   * PUT on /users/password/change (authorities: CONSULTANT_DEFAULT, USER_DEFAULT)
   */
  @Test
  public void updatePassword_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_PASSWORD).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(identityClient, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.USE_FEEDBACK,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT,
      AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
      AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
      AuthorityValue.USER_ADMIN})
  public void updatePassword_Should_ReturnForbiddenAndCallNoMethods_WhenNoDefaultConsultantOrDefaultUserAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_PASSWORD).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT})
  public void updatePassword_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_PASSWORD).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient, authenticatedUser);
  }

  /**
   * POST on /users/messages/key
   */

  @Test
  public void updateKey_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_UPDATE_KEY).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(encryptionService);
  }

  @Test
  @WithMockUser(
      authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
          AuthorityValue.USE_FEEDBACK, AuthorityValue.USER_DEFAULT,
          AuthorityValue.CONSULTANT_DEFAULT,
          AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
          AuthorityValue.START_CHAT,
          AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
          AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
          AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateKey_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_UPDATE_KEY).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(encryptionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.TECHNICAL_DEFAULT})
  public void updateKey_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(post(PATH_UPDATE_KEY).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(encryptionService);
  }

  /**
   * POST on /users/chat/new
   */

  @Test
  public void createChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.USER_DEFAULT,
      AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS, AuthorityValue.CONSULTANT_DEFAULT,
      AuthorityValue.START_CHAT,
      AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
      AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
      AuthorityValue.USER_ADMIN})
  public void createChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCreateNewChatAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CREATE_NEW_CHAT})
  public void createChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CREATE_NEW_CHAT})
  public void createChat_Should_ReturnBadRequest_WhenProperlyAuthorized() throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * PUT on /users/chat/{chatId}/start
   */
  @Test
  public void startChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.USER_DEFAULT,
      AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS, AuthorityValue.CONSULTANT_DEFAULT,
      AuthorityValue.CREATE_NEW_CHAT,
      AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
      AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
      AuthorityValue.USER_ADMIN})
  public void startChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoStartChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.START_CHAT})
  public void startChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.START_CHAT})
  public void startChat_Should_ReturnBadRequest_WhenProperlyAuthorized() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * PUT on /users/chat/{chatId}/join
   */
  @Test
  public void joinChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void joinChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void joinChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void joinChat_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void joinChat_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * GET on /users/chat/{chatId}
   */
  @Test
  public void getChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatPermissionVerifier);

  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(get(PATH_GET_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatPermissionVerifier);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getChat_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getChat_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * PUT on /users/chat/{chatId}/stop
   */
  @Test
  public void stopChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.USER_DEFAULT,
      AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS, AuthorityValue.CONSULTANT_DEFAULT,
      AuthorityValue.CREATE_NEW_CHAT,
      AuthorityValue.START_CHAT, AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
      AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
      AuthorityValue.USER_ADMIN})
  public void stopChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoStopChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.STOP_CHAT})
  public void stopChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.STOP_CHAT})
  public void stopChat_Should_ReturnBadRequest_WhenProperlyAuthorized() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * GET on /users/chat/{chatId}/members
   */
  @Test
  public void getChatMembers_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_GET_CHAT_MEMBERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatPermissionVerifier);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);

  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getChatMembers_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatPermissionVerifier);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getChatMembers_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatPermissionVerifier);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getChatMembers_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void getChatMembers_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * PUT on /users/chat/{chatId}/leave
   */
  @Test
  public void leaveChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void leaveChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void leaveChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantDataFacade);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void leaveChat_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void leaveChat_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * PUT on /users/chat/{chatId}/update
   */
  @Test
  public void updateChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.UPDATE_CHAT})
  public void updateChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken() throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.UPDATE_CHAT})
  public void updateChat_Should_ReturnOK_WhenProperlyAuthorizedWithUpdateChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_CHAT_BODY)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  /**
   * GET on /users/consultants/sessions (role: consultants)
   */

  @Test
  public void fetchSessionForConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSION_FOR_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(validatedUserAccountProvider, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.USER_DEFAULT, AuthorityValue.VIEW_AGENCY_CONSULTANTS,
      AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.START_CHAT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.UPDATE_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void fetchSessionForConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSION_FOR_CONSULTANT).cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(validatedUserAccountProvider, sessionService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void fetchSessionForConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSION_FOR_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  public void updateEmailAddress_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_EMAIL)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateEmailAddress_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_EMAIL)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateEmailAddress_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_EMAIL)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateEmailAddress_Should_ReturnOK_WhenProperlyAuthorizedWithUpdateChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_EMAIL)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content("email")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void deleteEmailAddress_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(delete("/users/email")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void deleteEmailAddress_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(delete("/users/email")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void deleteEmailAddress_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken()
      throws Exception {

    mvc.perform(delete("/users/email")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void deleteEmailAddress_Should_ReturnOK_WhenProperlyAuthorizedWithUpdateChatAuthority()
      throws Exception {

    mvc.perform(delete("/users/email")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void deactivateAndFlagUserAccountForDeletion_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(delete(PATH_DELETE_FLAG_USER_DELETED)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN,
      AuthorityValue.CONSULTANT_DEFAULT})
  public void deactivateAndFlagUserAccountForDeletion_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserAuthority()
      throws Exception {

    mvc.perform(delete(PATH_DELETE_FLAG_USER_DELETED)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void deactivateAndFlagUserAccountForDeletion_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken()
      throws Exception {

    mvc.perform(delete(PATH_DELETE_FLAG_USER_DELETED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void deactivateAndFlagUserAccountForDeletion_Should_ReturnOK_WhenProperlyAuthorizedWithUpdateChatAuthority()
      throws Exception {

    mvc.perform(delete(PATH_DELETE_FLAG_USER_DELETED)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(new DeleteUserAccountDTO().password(
            "passwort")))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void updateMobileToken_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MOBILE_TOKEN)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(validatedUserAccountProvider);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateMobileToken_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MOBILE_TOKEN)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(validatedUserAccountProvider);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateMobileToken_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MOBILE_TOKEN)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(validatedUserAccountProvider);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateMobileToken_Should_ReturnOK_WhenProperlyAuthorizedWithUpdateChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MOBILE_TOKEN)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(new MobileTokenDTO().token(
            "token")))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void updateSessionData_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_PUT_UPDATE_SESSION_DATA)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(sessionDataService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateSessionData_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_PUT_UPDATE_SESSION_DATA)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionDataService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateSessionData_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(put(PATH_PUT_UPDATE_SESSION_DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionDataService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateSessionData_Should_ReturnOK_When_ProperlyAuthorizedWithUserAuthority()
      throws Exception {
    mvc.perform(put(PATH_PUT_UPDATE_SESSION_DATA)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(new SessionDataDTO().age("2")))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void patchUser_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(
            patch(PATH_GET_USER_DATA)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void patchUser_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {
    mvc.perform(
            patch(PATH_GET_USER_DATA)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void patchUser_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(
            patch(PATH_GET_USER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  public void updateUserData_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_GET_USER_DATA)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void updateUserData_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_GET_USER_DATA)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void updateUserData_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(put(PATH_GET_USER_DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantDataFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserData_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    var consultant = givenAValidConsultant();
    var updateConsultantDTO = givenAMinimalUpdateConsultantDto(consultant.getEmail());
    when(consultantUpdateService.updateConsultant(anyString(), any())).thenReturn(consultant);

    mvc.perform(put(PATH_GET_USER_DATA)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ANONYMOUS_DEFAULT})
  public void getUserData_Should_ReturnOK_When_AnonymousAuthority() throws Exception {

    when(this.authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.ANONYMOUS.getValue()));
    when(this.validatedUserAccountProvider.retrieveValidatedUser())
        .thenReturn(new EasyRandom().nextObject(User.class));
    when(askerDataProvider.retrieveData(any())).thenReturn(easyRandom.nextObject(
        UserDataResponseDTO.class));

    mvc.perform(get(PATH_GET_USER_DATA)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ANONYMOUS_DEFAULT})
  public void getAskerSessions_Should_ReturnNoContent_When_AnonymousAuthority() throws Exception {

    when(this.authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.ANONYMOUS.getValue()));
    when(this.validatedUserAccountProvider.retrieveValidatedUser())
        .thenReturn(new EasyRandom().nextObject(User.class));

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void archiveSession_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_ARCHIVE_SESSION)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(this.sessionArchiveService).archiveSession(any());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void archiveSession_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(put(PATH_ARCHIVE_SESSION))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN,
      AuthorityValue.USER_DEFAULT})
  public void archiveSession_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_ARCHIVE_SESSION)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  public void archiveSession_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_ARCHIVE_SESSION)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  public void dearchiveSession_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_DEARCHIVE_SESSION)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(this.sessionArchiveService).dearchiveSession(any());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  public void dearchiveSession_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(put(PATH_DEARCHIVE_SESSION))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void dearchiveSession_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_DEARCHIVE_SESSION)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  public void dearchiveSession_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_DEARCHIVE_SESSION)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(sessionArchiveService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  public void addMobileAppToken_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_PUT_ADD_MOBILE_TOKEN)
        .cookie(CSRF_COOKIE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(new MobileTokenDTO().token("test")))
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(this.validatedUserAccountProvider).addMobileAppToken("test");
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  public void addMobileAppToken_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(put(PATH_PUT_ADD_MOBILE_TOKEN))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(validatedUserAccountProvider);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void addMobileAppToken_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
      throws Exception {
    mvc.perform(put(PATH_PUT_ADD_MOBILE_TOKEN)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(validatedUserAccountProvider);
  }

  @Test
  public void addMobileAppToken_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_PUT_ADD_MOBILE_TOKEN)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(validatedUserAccountProvider);
  }


  @Test
  public void deleteSessionAndInactiveUser_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {
    var sessionId = Math.abs(easyRandom.nextLong());
    var path = "/users/sessions/" + sessionId;

    mvc.perform(
        delete(path)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
    ).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void deleteSessionAndInactiveUser_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken()
      throws Exception {
    var sessionId = Math.abs(easyRandom.nextLong());
    var path = "/users/sessions/" + sessionId;

    mvc.perform(
        delete(path)
    ).andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(
      authorities = {
          AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
          AuthorityValue.USE_FEEDBACK, AuthorityValue.TECHNICAL_DEFAULT, AuthorityValue.USER_ADMIN,
          AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
          AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
          AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION
      }
  )
  public void deactivateAndFlagUserAccountForDeletion_Should_ReturnForbidden_WhenNoConsultantAuthority()
      throws Exception {
    var sessionId = Math.abs(easyRandom.nextLong());
    var path = "/users/sessions/" + sessionId;

    mvc.perform(
        delete(path)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
    ).andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void deactivateAndFlagUserAccountForDeletion_Should_ReturnSessionNotFound_WhenProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    var sessionId = Math.abs(easyRandom.nextLong());
    var path = "/users/sessions/" + sessionId;

    mvc.perform(
        delete(path)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
    ).andExpect(status().isNotFound());

    verify(sessionService).getSession(sessionId);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void deactivateTwoFactorAuthByApp_Should_ReturnOK_When_ProperlyAuthorizedWithConsultant_Or_UserAuthority()
      throws Exception {
    mvc.perform(delete("/users/twoFactorAuth")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(identityManager).deleteOneTimePassword(any());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void deactivate2faForUser_Should_ReturnOK_When_ProperlyAuthorizedWithConsultant_Or_UserAuthority()
      throws Exception {
    mvc.perform(
            delete("/users/2fa")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(identityManager).deleteOneTimePassword(any());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void deactivateTwoFactorAuthByApp_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {
    mvc.perform(delete("/users/twoFactorAuth")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void deactivateTwoFactorAuthByApp_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(delete("/users/twoFactorAuth")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void startTwoFactorAuthByEmailSetup_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    var payload = givenAValidEmailDTO();

    mvc.perform(put("/users/2fa/email")
            .content(objectMapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoInteractions(identityManager);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void startTwoFactorAuthByEmailSetup_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {
    var payload = givenAValidEmailDTO();

    mvc.perform(put("/users/2fa/email")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoInteractions(identityManager);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void activate2faForUser_Should_ReturnOK_When_ProperlyAuthorizedWithConsultant_Or_UserAuthority()
      throws Exception {
    var payload = givenAValidOneTimePasswordDto();
    givenAValidOtpResponse();

    mvc.perform(put("/users/2fa/app")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthByApp_Should_ReturnOK_When_ProperlyAuthorizedWithConsultant_Or_UserAuthority()
      throws Exception {
    var payload = givenAValidOneTimePasswordDto();
    givenAValidOtpResponse();

    mvc.perform(put("/users/twoFactorAuth")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(identityManager).setUpOneTimePassword(any(), any(), any());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthByApp_Should_ReturnBadRequest_When_RequestBody_Is_Missing()
      throws Exception {
    mvc.perform(put("/users/twoFactorAuth")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void activateTwoFactorAuthByApp_Should_ReturnForbiddenAndCallNoMethods_When_NoUserOrConsultantAuthority()
      throws Exception {
    mvc.perform(put("/users/twoFactorAuth")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT, AuthorityValue.CONSULTANT_DEFAULT})
  public void activateTwoFactorAuthByApp_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    mvc.perform(put("/users/twoFactorAuth")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(identityClient);
  }

  @Test
  public void getConsultantPublicData_Should_ReturnForbiddenAndCallNoMethods_When_noCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_PUBLIC_CONSULTANT_DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoInteractions(consultantAgencyService);
  }

  @Test
  public void getConsultantPublicData_Should_ReturnOk_When_CsrfTokensAreGiven()
      throws Exception {
    givenAValidConsultant();

    mvc.perform(get(PATH_GET_PUBLIC_CONSULTANT_DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(consultantAgencyService).getAgenciesOfConsultant("65c1095e-b977-493a-a34f-064b729d1d6c");
  }

  private UpdateConsultantDTO givenAMinimalUpdateConsultantDto(String email) {
    return new UpdateConsultantDTO()
        .email(email).firstname("firstname").lastname("lastname");
  }

  private OneTimePasswordDTO givenAValidOneTimePasswordDto() {
    return new OneTimePasswordDTO().otp("111111")
        .secret(new RandomString(32).nextString());
  }

  private Consultant givenAValidConsultant() {
    var consultant = easyRandom.nextObject(Consultant.class);
    consultant.setEmail(
        RandomStringUtils.randomAlphabetic(8) + "@" + RandomStringUtils.randomAlphabetic(8) + ".com"
    );
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultant));

    return consultant;
  }

  private EmailDTO givenAValidEmailDTO() {
    var email = RandomStringUtils.randomAlphabetic(8)
        + "@" + RandomStringUtils.randomAlphabetic(8)
        + ".com";

    var emailDTO = new EmailDTO();
    emailDTO.setEmail(email);

    return emailDTO;
  }

  private void givenAValidOtpResponse() {
    when(identityManager.setUpOneTimePassword(any(), any(), any())).thenReturn(true);
  }
}
