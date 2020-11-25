package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_ACCEPT_ENQUIRY;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_CREATE_ENQUIRY_MESSAGE;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_CHAT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_CHAT_MEMBERS;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_CONSULTANTS;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_ENQUIRIES_FOR_AGENCY;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_MONITORING;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_GET_USER_DATA;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_CHAT_NEW;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_IMPORT_ASKERS;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_IMPORT_CONSULTANTS;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_NEW_MESSAGE_NOTIFICATION;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_REGISTER_NEW_CONSULTING_TYPE;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_POST_REGISTER_USER;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_ASSIGN_SESSION;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_CHAT_START;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_CHAT_STOP;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_CONSULTANT_ABSENT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_JOIN_CHAT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_LEAVE_CHAT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_UPDATE_CHAT;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_UPDATE_MONITORING;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_PUT_UPDATE_PASSWORD;
import static de.caritas.cob.userservice.testHelper.PathConstants.PATH_UPDATE_KEY;
import static de.caritas.cob.userservice.testHelper.RequestBodyConstants.VALID_UPDATE_CHAT_BODY;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.CreateSessionFacade;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.GetChatFacade;
import de.caritas.cob.userservice.api.facade.GetChatMembersFacade;
import de.caritas.cob.userservice.api.facade.JoinAndLeaveChatFacade;
import de.caritas.cob.userservice.api.facade.StartChatFacade;
import de.caritas.cob.userservice.api.facade.StopChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.AskerImportService;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.KeycloakService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.UserService;
import javax.servlet.http.Cookie;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserControllerAuthorizationIT {

  private final String CSRF_COOKIE = "csrfCookie";
  private final String CSRF_HEADER = "csrfHeader";
  private final String CSRF_VALUE = "test";

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
  private CreateEnquiryMessageFacade createEnquiryMessageFacade;
  @MockBean
  private LogService logService;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @MockBean
  private ConsultantService consultantService;
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
  private KeycloakService keycloakService;
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
  private ChatHelper chatHelper;
  @MockBean
  private StopChatFacade stopChatFacade;
  @MockBean
  private GetChatMembersFacade getChatMembersFacade;
  @MockBean
  private UserHelper userHelper;
  @MockBean
  private CreateSessionFacade createSessionFacade;

  private Cookie csrfCookie;

  @Before
  public void setUp() {
    csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);
  }

  /**
   * POST on /users/askers/new
   *
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
   *
   */

  @Test
  public void registerNewConsultingType_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_POST_REGISTER_NEW_CONSULTING_TYPE).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void registerNewConsultingType_Should_ReturnForbiddenAndCallNoMethods_WhenNoAskerDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_POST_REGISTER_NEW_CONSULTING_TYPE).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, userService, createSessionFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void registerNewConsultingType_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_POST_REGISTER_NEW_CONSULTING_TYPE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * GET on /users/sessions/open (role: consultant)
   *
   */

  @Test
  public void getOpenSessionsForAuthenticatedConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getOpenSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getOpenSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * GET on /users/sessions/consultants/new (role: consultant)
   *
   */

  @Test
  public void getEnquiriesForAgency_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        get(PATH_GET_ENQUIRIES_FOR_AGENCY).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);

  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getEnquiriesForAgency_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(
        get(PATH_GET_ENQUIRIES_FOR_AGENCY).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getEnquiriesForAgency_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_ENQUIRIES_FOR_AGENCY).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * PUT on /users/sessions/new/{sessionId} (role: consultant)
   *
   */
  @Test
  public void acceptEnquiry_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_ACCEPT_ENQUIRY + "/1").cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void acceptEnquiry_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_ACCEPT_ENQUIRY + "/1").cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void acceptEnquiry_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_ACCEPT_ENQUIRY + "/1").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * POST on /users/sessions/askers/new (role: user)
   *
   */

  @Test
  public void createEnquiryMessage_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_CREATE_ENQUIRY_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository, userService,
        userRepository);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void createEnquiryMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_CREATE_ENQUIRY_MESSAGE).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository, userService,
        userRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void createEnquiryMessage_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_CREATE_ENQUIRY_MESSAGE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository, userService,
        userRepository);
  }

  /**
   * GET on /users/sessions/askers (role: user)
   *
   */

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getSessionsForAuthenticatedUser_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void getSessionsForAuthenticatedUser_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, sessionRepository);
  }

  /**
   * PUT on /users/consultants/absences (role: consultant)
   *
   */

  @Test
  public void updateAbsence_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_CONSULTANT_ABSENT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantService, logService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void updateAbsence_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_CONSULTANT_ABSENT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantService, logService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void updateAbsence_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_CONSULTANT_ABSENT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantService, logService);
  }

  /**
   * GET on /users/sessions/consultants (role: consultants)
   *
   */

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  /**
   * GET on /users/data (role: consultant/user)
   *
   */

  @Test
  public void getUserData_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_USER_DATA).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantService, logService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getUserData_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthorityOrConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_USER_DATA).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantService, logService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT, Authority.USER_DEFAULT})
  public void getUserData_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_USER_DATA).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantService, logService);
  }

  /**
   * GET on /users/sessions/consultants (role: consultants)
   *
   */

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService);
  }

  /**
   * POST on /users/mails/messages/new (role: consultant/user)
   *
   */

  @Test
  public void sendNewMessageNotification_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_POST_NEW_MESSAGE_NOTIFICATION).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(emailNotificationFacade, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void sendNewMessageNotification_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserDefaultAuthorityOrConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_POST_NEW_MESSAGE_NOTIFICATION).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(emailNotificationFacade, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void sendNewMessageNotification_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_NEW_MESSAGE_NOTIFICATION).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(emailNotificationFacade, authenticatedUser);
  }

  /**
   * POST on /users/consultants/import (role: technical)
   *
   */

  @Test
  public void importConsultants_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(
        post(PATH_POST_IMPORT_CONSULTANTS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantImportService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CONSULTANT_DEFAULT, Authority.USER_DEFAULT, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void importConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(
        post(PATH_POST_IMPORT_CONSULTANTS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantImportService);
  }

  @Test
  @WithMockUser(authorities = {Authority.TECHNICAL_DEFAULT})
  public void importConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_CONSULTANTS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantImportService);
  }

  /**
   * GET on /users/sessions/{sessionId}/monitoring (role: consultant)
   *
   */
  @Test
  public void getMonitoring_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_GET_MONITORING).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.TECHNICAL_DEFAULT, Authority.USER_DEFAULT, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_GET_MONITORING).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_GET_MONITORING).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  /**
   * PUT on /users/sessions/monitoring/{sessionId} (role: consultant)
   *
   */
  @Test
  public void updateMonitoring_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MONITORING).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.TECHNICAL_DEFAULT, Authority.USER_DEFAULT, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void updateMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MONITORING).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void updateMonitoring_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_MONITORING).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(authenticatedUser, sessionService, monitoringService);
  }

  /**
   * POST on /users/askers/import (role: technical)
   *
   */

  @Test
  public void importAskers_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CONSULTANT_DEFAULT, Authority.USER_DEFAULT, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void importAskers_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {Authority.TECHNICAL_DEFAULT})
  public void importAskers_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  /**
   * POST on /users/askersWithoutSession/import (role: technical)
   *
   */

  @Test
  public void importAskersWithoutSession_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CONSULTANT_DEFAULT, Authority.CONSULTANT_DEFAULT, Authority.START_CHAT,
      Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void importAskersWithoutSession_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION).cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }

  @Test
  @WithMockUser(authorities = {Authority.TECHNICAL_DEFAULT})
  public void importAskersWithoutSession_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerImportService);
  }


  /**
   * GET on /users/consultants (authority: VIEW_AGENCY_CONSULTANTS)
   *
   */

  @Test
  public void getConsultants_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_CONSULTANTS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAgencyService);

  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT, Authority.CONSULTANT_DEFAULT, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.START_CHAT, Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoViewAgencyConsultantsAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_CONSULTANTS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAgencyService);
  }

  @Test
  @WithMockUser(authorities = {Authority.VIEW_AGENCY_CONSULTANTS})
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_CONSULTANTS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAgencyService);
  }

  /**
   * PUT on /users/sessions/{sessionId}/consultant/{consultantId} (authority:
   * ASSIGN_CONSULTANT_TO_ENQUIRY and/or ASSIGN_CONSULTANT_TO_SESSION)
   *
   */
  @Test
  public void assignSession_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_ASSIGN_SESSION).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantService, sessionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_ENQUIRY})
  public void assignSession_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_ASSIGN_SESSION).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantService, sessionService);
  }

  /**
   * PUT on /users/password/change (authorities: CONSULTANT_DEFAULT, USER_DEFAULT)
   *
   */
  @Test
  public void updatePassword_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_PASSWORD).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(keycloakService, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.USE_FEEDBACK,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.TECHNICAL_DEFAULT, Authority.START_CHAT, Authority.CREATE_NEW_CHAT,
      Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void updatePassword_Should_ReturnForbiddenAndCallNoMethods_WhenNoDefaultConsultantOrDefaultUserAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_PASSWORD).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(keycloakService, authenticatedUser);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT, Authority.TECHNICAL_DEFAULT,
      Authority.USER_DEFAULT})
  public void updatePassword_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_PASSWORD).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(keycloakService, authenticatedUser);
  }

  /**
   * POST on /users/messages/key
   *
   */

  @Test
  public void updateKey_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_UPDATE_KEY).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(encryptionService);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.USER_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void updateKey_Should_ReturnForbiddenAndCallNoMethods_WhenNoTechnicalDefaultAuthority()
      throws Exception {

    mvc.perform(post(PATH_UPDATE_KEY).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(encryptionService);
  }

  @Test
  @WithMockUser(authorities = {Authority.TECHNICAL_DEFAULT})
  public void updateKey_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(post(PATH_UPDATE_KEY).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(encryptionService);
  }

  /**
   * POST on /users/chat/new
   *
   */

  @Test
  public void createChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.USER_DEFAULT,
      Authority.TECHNICAL_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS,
      Authority.VIEW_ALL_PEER_SESSIONS, Authority.CONSULTANT_DEFAULT, Authority.START_CHAT,
      Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void createChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCreateNewChatAuthority()
      throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CREATE_NEW_CHAT})
  public void createChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CREATE_NEW_CHAT})
  public void createChat_Should_ReturnBadRequest_WhenProperlyAuthorized() throws Exception {

    mvc.perform(post(PATH_POST_CHAT_NEW).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * PUT on /users/chat/{chatId}/start
   *
   */
  @Test
  public void startChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatHelper);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.USER_DEFAULT,
      Authority.TECHNICAL_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS,
      Authority.VIEW_ALL_PEER_SESSIONS, Authority.CONSULTANT_DEFAULT, Authority.CREATE_NEW_CHAT,
      Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void startChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoStartChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatHelper);
  }

  @Test
  @WithMockUser(authorities = {Authority.START_CHAT})
  public void startChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(startChatFacade);
    verifyNoMoreInteractions(chatHelper);
  }

  @Test
  @WithMockUser(authorities = {Authority.START_CHAT})
  public void startChat_Should_ReturnBadRequest_WhenProperlyAuthorized() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_START).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * PUT on /users/chat/{chatId}/join
   *
   */
  @Test
  public void joinChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void joinChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void joinChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void joinChat_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void joinChat_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(put(PATH_PUT_JOIN_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * GET on /users/chat/{chatId}
   *
   */
  @Test
  public void getChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatHelper);

  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatHelper);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(get(PATH_GET_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatHelper);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getChat_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void getChat_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(get(PATH_GET_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * PUT on /users/chat/{chatId}/stop
   *
   */
  @Test
  public void stopChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.USER_DEFAULT,
      Authority.TECHNICAL_DEFAULT, Authority.VIEW_AGENCY_CONSULTANTS,
      Authority.VIEW_ALL_PEER_SESSIONS, Authority.CONSULTANT_DEFAULT, Authority.CREATE_NEW_CHAT,
      Authority.START_CHAT, Authority.UPDATE_CHAT})
  public void stopChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoStopChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.STOP_CHAT})
  public void stopChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(stopChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.STOP_CHAT})
  public void stopChat_Should_ReturnBadRequest_WhenProperlyAuthorized() throws Exception {

    mvc.perform(put(PATH_PUT_CHAT_STOP).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * GET on /users/chat/{chatId}/members
   *
   */
  @Test
  public void getChatMembers_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {
    mvc.perform(put(PATH_GET_CHAT_MEMBERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatHelper);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);

  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getChatMembers_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatHelper);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getChatMembers_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(getChatMembersFacade);
    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(chatHelper);
    verifyNoMoreInteractions(userHelper);
    verifyNoMoreInteractions(rocketChatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getChatMembers_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void getChatMembers_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(get(PATH_GET_CHAT_MEMBERS).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * PUT on /users/chat/{chatId}/leave
   *
   */
  @Test
  public void leaveChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void leaveChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void leaveChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens() throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
    verifyNoMoreInteractions(consultantService);
    verifyNoMoreInteractions(userService);
    verifyNoMoreInteractions(rocketChatService);
    verifyNoMoreInteractions(joinChatFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void leaveChat_Should_ReturnOK_WhenProperlyAuthorizedAsConsultant() throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void leaveChat_Should_ReturnOK_WhenProperlyAuthorizedAsUser() throws Exception {

    mvc.perform(put(PATH_PUT_LEAVE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  /**
   * PUT on /users/chat/{chatId}/update
   *
   */
  @Test
  public void updateChat_Should_ReturnUnauthorizedAndCallNoMethods_WhenNoKeycloakAuthorization()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT})
  public void updateChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoUserOrConsultantAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.UPDATE_CHAT})
  public void updateChat_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfToken() throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    verifyNoMoreInteractions(chatService);
  }

  @Test
  @WithMockUser(authorities = {Authority.UPDATE_CHAT})
  public void updateChat_Should_ReturnOK_WhenProperlyAuthorizedWithUpdateChatAuthority()
      throws Exception {

    mvc.perform(put(PATH_PUT_UPDATE_CHAT).cookie(csrfCookie).header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE_CHAT_BODY)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

}
