package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.RequestBodyConstants.INVALID_USER_REQUEST_BODY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.ConversationDtoMapper;
import de.caritas.cob.userservice.api.config.auth.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.conversation.facade.AcceptAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.FinishAnonymousConversationFacade;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.port.in.Messaging;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=testing")
class ConversationControllerIT {

  private static final String ENQUIRIES_BASE_PATH = "/conversations/consultants/enquiries/";
  static final String GET_ANONYMOUS_ENQUIRIES_PATH = ENQUIRIES_BASE_PATH + "anonymous";
  static final String GET_REGISTERED_ENQUIRIES_PATH = ENQUIRIES_BASE_PATH + "registered";
  static final String POST_CREATE_ANONYMOUS_ENQUIRY_PATH = "/conversations/askers/anonymous/new";
  static final String ACCEPT_ANONYMOUS_ENQUIRY_PATH = "/conversations/askers/anonymous/1/accept";
  static final String FINISH_ANONYMOUS_CONVERSATION_PATH = "/conversations/anonymous/1/finish";

  @Autowired private MockMvc mvc;

  @MockBean private ConversationListResolver conversationListResolver;

  @MockBean private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  @MockBean private LinkDiscoverers linkDiscoverers;

  @MockBean private AcceptAnonymousEnquiryFacade acceptAnonymousEnquiryFacade;

  @MockBean private FinishAnonymousConversationFacade finishAnonymousConversationFacade;

  @SuppressWarnings("unused")
  @MockBean
  private ConversationDtoMapper conversationDtoMapper;

  @SuppressWarnings("unused")
  @MockBean
  private Messaging messaging;

  @SuppressWarnings("unused")
  @MockBean
  private AuthenticatedUser authenticatedUser;

  @MockBean private KeycloakConfigResolver keycloakConfigResolver;

  @Test
  void getAnonymousEnquiries_Should_returnOk_When_requestParamsAreValid() throws Exception {
    this.mvc
        .perform(
            get(GET_ANONYMOUS_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "10"))
        .andExpect(status().isOk());
  }

  @Test
  void getAnonymousEnquiries_Should_returnBadRequest_When_offsetIsMissing() throws Exception {
    this.mvc
        .perform(
            get(GET_ANONYMOUS_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAnonymousEnquiries_Should_returnBadRequest_When_countIsMissing() throws Exception {
    this.mvc
        .perform(
            get(GET_ANONYMOUS_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAnonymousEnquiries_Should_returnBadRequest_When_offsetIsLowerThanZero() throws Exception {
    this.mvc
        .perform(
            get(GET_ANONYMOUS_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "-10")
                .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAnonymousEnquiries_Should_returnBadRequest_When_countIsZero() throws Exception {
    this.mvc
        .perform(
            get(GET_ANONYMOUS_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAnonymousEnquiries_Should_returnBadRequest_When_countIsLowerThanZero() throws Exception {
    this.mvc
        .perform(
            get(GET_ANONYMOUS_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "-10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRegisteredEnquiries_Should_returnOk_When_requestParamsAreValid() throws Exception {
    this.mvc
        .perform(
            get(GET_REGISTERED_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "10"))
        .andExpect(status().isOk());
  }

  @Test
  void getRegisteredEnquiries_Should_returnBadRequest_When_offsetIsMissing() throws Exception {
    this.mvc
        .perform(
            get(GET_REGISTERED_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRegisteredEnquiries_Should_returnBadRequest_When_countIsMissing() throws Exception {
    this.mvc
        .perform(
            get(GET_REGISTERED_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRegisteredEnquiries_Should_returnBadRequest_When_offsetIsLowerThanZero()
      throws Exception {
    this.mvc
        .perform(
            get(GET_REGISTERED_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "-10")
                .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRegisteredEnquiries_Should_returnBadRequest_When_countIsZero() throws Exception {
    this.mvc
        .perform(
            get(GET_REGISTERED_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRegisteredEnquiries_Should_returnBadRequest_When_countIsLowerThanZero() throws Exception {
    this.mvc
        .perform(
            get(GET_REGISTERED_ENQUIRIES_PATH)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "-10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void acceptAnonymousEnquiry_Should_returnOk_When_requestParamsAreValid() throws Exception {
    this.mvc
        .perform(
            put(ACCEPT_ANONYMOUS_ENQUIRY_PATH).header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN))
        .andExpect(status().isOk());

    verify(this.acceptAnonymousEnquiryFacade, times(1)).acceptAnonymousEnquiry(1L);
  }

  @Test
  void acceptAnonymousEnquiry_Should_returnBadRequest_When_sessionIdIsInvalid() throws Exception {
    this.mvc
        .perform(
            put(ACCEPT_ANONYMOUS_ENQUIRY_PATH.replace("1", "invalid"))
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(this.acceptAnonymousEnquiryFacade);
  }

  @Test
  void createAnonymousEnquiry_Should_ReturnCreated_WhenProvidedWithValidRequestBody()
      throws Exception {
    this.mvc
        .perform(
            post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
                .content(
                    new ObjectMapper()
                        .writeValueAsString(
                            new EasyRandom().nextObject(CreateAnonymousEnquiryDTO.class)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  void createAnonymousEnquiry_Should_ReturnBadRequest_WhenProvidedWithInvalidRequestBody()
      throws Exception {
    this.mvc
        .perform(
            post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
                .content(INVALID_USER_REQUEST_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void finishAnonymousConversation_Should_ReturnBadRequest_WhenProvidedWithInvalidSessionId()
      throws Exception {
    this.mvc
        .perform(
            put(FINISH_ANONYMOUS_CONVERSATION_PATH.replace("1", "invalid"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void finishAnonymousConversation_Should_ReturnOk_WhenProvidedWithValidSessionId()
      throws Exception {
    this.mvc
        .perform(
            put(FINISH_ANONYMOUS_CONVERSATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
