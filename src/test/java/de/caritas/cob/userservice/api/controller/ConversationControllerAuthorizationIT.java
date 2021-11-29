package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue.ANONYMOUS_DEFAULT;
import static de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue.USER_DEFAULT;
import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.ACCEPT_ANONYMOUS_ENQUIRY_PATH;
import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.FINISH_ANONYMOUS_CONVERSATION_PATH;
import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.GET_ANONYMOUS_ENQUIRIES_PATH;
import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.GET_REGISTERED_ENQUIRIES_PATH;
import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.POST_CREATE_ANONYMOUS_ENQUIRY_PATH;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.conversation.facade.AcceptAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.FinishAnonymousConversationFacade;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationControllerAuthorizationIT {

  private final String CSRF_COOKIE = "csrfCookie";
  private final String CSRF_HEADER = "csrfHeader";
  private final String CSRF_VALUE = "test";
  private final Cookie csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ConversationListResolver conversationListResolver;

  @MockBean
  private AcceptAnonymousEnquiryFacade acceptAnonymousEnquiryFacade;

  @MockBean
  private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  @MockBean
  private FinishAnonymousConversationFacade finishAnonymousConversationFacade;

  @MockBean
  private UsernameTranscoder usernameTranscoder;

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getAnonymousEnquiries_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
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
  public void getAnonymousEnquiries_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getAnonymousEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
      throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
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
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getRegisteredEnquiries_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
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
  public void getRegisteredEnquiries_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void getRegisteredEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
      throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
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
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void acceptAnonymousEnquirys_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    this.mvc.perform(put(ACCEPT_ANONYMOUS_ENQUIRY_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(this.acceptAnonymousEnquiryFacade, times(1)).acceptAnonymousEnquiry(1L);
  }

  @Test
  public void acceptAnonymousEnquiry_Should_ReturnUnauthorizedAndCallNoMethods_When_NoKeycloakAuthorization()
      throws Exception {
    this.mvc.perform(put(ACCEPT_ANONYMOUS_ENQUIRY_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USE_FEEDBACK,
      AuthorityValue.TECHNICAL_DEFAULT,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT,
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
      AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY, AuthorityValue.USER_ADMIN})
  public void acceptAnonymousEnquiry_Should_ReturnForbiddenAndCallNoMethods_When_NoConsultantAuthority()
      throws Exception {
    this.mvc.perform(put(ACCEPT_ANONYMOUS_ENQUIRY_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  @WithMockUser(authorities = {USER_DEFAULT})
  public void acceptAnonymousEnquiry_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc.perform(put(ACCEPT_ANONYMOUS_ENQUIRY_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  public void createAnonymousEnquiry_Should_ReturnCreated_When_CsrfTokensMatch() throws Exception {
    this.mvc.perform(post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .content(new ObjectMapper().writeValueAsString(new EasyRandom().nextObject(
                CreateAnonymousEnquiryDTO.class)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    verify(this.createAnonymousEnquiryFacade, times(1))
        .createAnonymousEnquiry(any());
  }

  @Test
  public void createAnonymousEnquiry_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc.perform(post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(createAnonymousEnquiryFacade);
  }

  @Test
  @WithMockUser(authorities = {USER_DEFAULT})
  public void finishAnonymousConversation_Should_ReturnForbiddenAndCallNoMethods_When_NoValidAuthority()
      throws Exception {
    this.mvc.perform(put(FINISH_ANONYMOUS_CONVERSATION_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.finishAnonymousConversationFacade);
  }

  @Test
  @WithMockUser(authorities = {ANONYMOUS_DEFAULT})
  public void finishAnonymousConversation_Should_ReturnOk_When_CsrfTokensMatchAndAnonymousDefaultAuthority()
      throws Exception {
    this.mvc.perform(put(FINISH_ANONYMOUS_CONVERSATION_PATH)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.finishAnonymousConversationFacade, times(1))
        .finishConversation(any());
  }

  @Test
  public void finishAnonymousConversation_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc.perform(put(FINISH_ANONYMOUS_CONVERSATION_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(finishAnonymousConversationFacade);
  }

}
