package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.GET_ANONYMOUS_ENQUIRIES_PATH;
import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.GET_REGISTERED_ENQUIRIES_PATH;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import javax.servlet.http.Cookie;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class ConversationControllerAuthorizationIT {

  private final String CSRF_COOKIE = "csrfCookie";
  private final String CSRF_HEADER = "csrfHeader";
  private final String CSRF_VALUE = "test";
  private final Cookie csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ConversationListResolver conversationListResolver;

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getAnonymousEnquiries_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE)
        .param("offset", "0")
        .param("count", "10")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.conversationListResolver, times(1))
        .resolveConversations(0, 10, ANONYMOUS_ENQUIRY);
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
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT,
      Authority.VIEW_ALL_FEEDBACK_SESSIONS, Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USER_ADMIN})
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
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void getAnonymousEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

  @Test
  @WithMockUser(authorities = {Authority.CONSULTANT_DEFAULT})
  public void getRegisteredEnquiries_Should_ReturnOK_When_ProperlyAuthorizedWithConsultantAuthority()
      throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE)
        .param("offset", "0")
        .param("count", "10")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.conversationListResolver, times(1))
        .resolveConversations(0, 10, REGISTERED_ENQUIRY);
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
  @WithMockUser(authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT,
      Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.CREATE_NEW_CHAT, Authority.START_CHAT, Authority.STOP_CHAT,
      Authority.VIEW_ALL_FEEDBACK_SESSIONS, Authority.ASSIGN_CONSULTANT_TO_SESSION,
      Authority.ASSIGN_CONSULTANT_TO_ENQUIRY, Authority.USER_ADMIN})
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
  @WithMockUser(authorities = {Authority.USER_DEFAULT})
  public void getRegisteredEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(conversationListResolver);
  }

}
