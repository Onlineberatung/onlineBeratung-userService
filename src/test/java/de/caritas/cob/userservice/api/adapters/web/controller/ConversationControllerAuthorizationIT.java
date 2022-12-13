package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ANONYMOUS_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_PEER_SESSION;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CONSULTANT_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CREATE_NEW_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.RESTRICTED_AGENCY_ADMIN;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.SINGLE_TENANT_ADMIN;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.START_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.STOP_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.TECHNICAL_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.TENANT_ADMIN;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.UPDATE_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.USER_ADMIN;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.USER_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.USE_FEEDBACK;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_AGENCY_CONSULTANTS;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_ALL_PEER_SESSIONS;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.conversation.facade.AcceptAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.FinishAnonymousConversationFacade;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationControllerAuthorizationIT {

  private final String CSRF_COOKIE = "csrfCookie";
  private final String CSRF_HEADER = "csrfHeader";
  private final String CSRF_VALUE = "test";
  private final Cookie csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);

  @Autowired private MockMvc mvc;

  @MockBean private ConversationListResolver conversationListResolver;

  @MockBean private AcceptAnonymousEnquiryFacade acceptAnonymousEnquiryFacade;

  @MockBean private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  @MockBean private FinishAnonymousConversationFacade finishAnonymousConversationFacade;

  @MockBean
  @SuppressWarnings("unused")
  private UsernameTranscoder usernameTranscoder;

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getAnonymousEnquiries_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_ANONYMOUS_ENQUIRIES_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.conversationListResolver, times(1))
        .resolveConversations(0, 10, ANONYMOUS_ENQUIRY, RC_TOKEN);
  }

  @Test
  public void
      getAnonymousEnquiries_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
          throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_ANONYMOUS_ENQUIRIES_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.TECHNICAL_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.START_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USER_ADMIN
      })
  public void
      getAnonymousEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
          throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_ANONYMOUS_ENQUIRIES_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {USER_DEFAULT})
  public void getAnonymousEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_ANONYMOUS_ENQUIRIES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void
      getRegisteredEnquiries_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
          throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_REGISTERED_ENQUIRIES_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.conversationListResolver, times(1))
        .resolveConversations(0, 10, REGISTERED_ENQUIRY, RC_TOKEN);
  }

  @Test
  public void
      getRegisteredEnquiries_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
          throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_REGISTERED_ENQUIRIES_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.TECHNICAL_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.START_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USER_ADMIN
      })
  public void
      getRegisteredEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
          throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_REGISTERED_ENQUIRIES_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {USER_DEFAULT})
  public void getRegisteredEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc
        .perform(
            get(ConversationControllerIT.GET_REGISTERED_ENQUIRIES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void
      acceptAnonymousEnquirys_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
          throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.put(ConversationControllerIT.ACCEPT_ANONYMOUS_ENQUIRY_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(this.acceptAnonymousEnquiryFacade, times(1)).acceptAnonymousEnquiry(1L);
  }

  @Test
  public void
      acceptAnonymousEnquiry_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
          throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.put(ConversationControllerIT.ACCEPT_ANONYMOUS_ENQUIRY_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.TECHNICAL_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.START_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USER_ADMIN
      })
  public void
      acceptAnonymousEnquiry_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
          throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.put(ConversationControllerIT.ACCEPT_ANONYMOUS_ENQUIRY_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  @WithMockUser(authorities = {USER_DEFAULT})
  public void acceptAnonymousEnquiry_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc
        .perform(MockMvcRequestBuilders.put(ConversationControllerIT.ACCEPT_ANONYMOUS_ENQUIRY_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  public void createAnonymousEnquiry_Should_ReturnCreated_When_CsrfTokensMatch() throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.post(ConversationControllerIT.POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .content(
                    new ObjectMapper()
                        .writeValueAsString(
                            new EasyRandom().nextObject(CreateAnonymousEnquiryDTO.class)))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    verify(this.createAnonymousEnquiryFacade, times(1)).createAnonymousEnquiry(any());
  }

  @Test
  public void createAnonymousEnquiry_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.post(ConversationControllerIT.POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(createAnonymousEnquiryFacade);
  }

  @Test
  @WithMockUser(authorities = {USER_DEFAULT})
  public void
      finishAnonymousConversation_Should_ReturnForbiddenAndCallNoMethods_When_NoValidAuthority()
          throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.put(ConversationControllerIT.FINISH_ANONYMOUS_CONVERSATION_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.finishAnonymousConversationFacade);
  }

  @Test
  @WithMockUser(authorities = {ANONYMOUS_DEFAULT})
  public void
      finishAnonymousConversation_Should_ReturnOk_When_CsrfTokensMatchAndAnonymousDefaultAuthority()
          throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.put(ConversationControllerIT.FINISH_ANONYMOUS_CONVERSATION_PATH)
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.finishAnonymousConversationFacade, times(1)).finishConversation(any());
  }

  @Test
  public void finishAnonymousConversation_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc
        .perform(
            MockMvcRequestBuilders.put(ConversationControllerIT.FINISH_ANONYMOUS_CONVERSATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(finishAnonymousConversationFacade);
  }

  @Test
  public void getAnonymousEnquiryDetails_Should_ReturnUnauthorized_When_NoKeycloakAuthorization()
      throws Exception {
    mvc.perform(
            get("/conversations/anonymous/1")
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(
      authorities = {
        USER_DEFAULT,
        CONSULTANT_DEFAULT,
        USE_FEEDBACK,
        VIEW_ALL_FEEDBACK_SESSIONS,
        VIEW_ALL_PEER_SESSIONS,
        ASSIGN_CONSULTANT_TO_SESSION,
        ASSIGN_CONSULTANT_TO_ENQUIRY,
        ASSIGN_CONSULTANT_TO_PEER_SESSION,
        VIEW_AGENCY_CONSULTANTS,
        TECHNICAL_DEFAULT,
        CREATE_NEW_CHAT,
        UPDATE_CHAT,
        START_CHAT,
        STOP_CHAT,
        USER_ADMIN,
        SINGLE_TENANT_ADMIN,
        TENANT_ADMIN,
        RESTRICTED_AGENCY_ADMIN
      })
  public void getAnonymousEnquiryDetails_Should_ReturnForbidden_When_NoAnonymousAuthority()
      throws Exception {
    mvc.perform(
            get("/conversations/anonymous/1")
                .cookie(csrfCookie)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = ANONYMOUS_DEFAULT)
  public void getAnonymousEnquiryDetails_Should_ReturnForbidden_When_NoCsrfToken()
      throws Exception {
    mvc.perform(
            get("/conversations/anonymous/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
