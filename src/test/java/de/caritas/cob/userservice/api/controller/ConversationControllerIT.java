package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.userservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=testing")
public class ConversationControllerIT {

  private static final String ENQUIREIES_BASE_PATH = "/conversations/consultants/enquiries/";
  static final String GET_ANONYMOUS_ENQUIRIES_PATH = ENQUIREIES_BASE_PATH + "anonymous";
  static final String GET_REGISTERED_ENQUIRIES_PATH = ENQUIREIES_BASE_PATH + "registered";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ConversationListResolver conversationListResolver;

  @MockBean
  private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean
  private LinkDiscoverers linkDiscoverers;

  @Test
  public void getAnonymousEnquiries_Should_returnOk_When_requestParamsAreValid() throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0")
        .param("count", "10"))
        .andExpect(status().isOk());
  }

  @Test
  public void getAnonymousEnquiries_Should_returnBadRequest_When_offsetIsMissing() throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getAnonymousEnquiries_Should_returnBadRequest_When_countIsMissing() throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getAnonymousEnquiries_Should_returnBadRequest_When_offsetIsLowerThanZero() throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "-10")
        .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getAnonymousEnquiries_Should_returnBadRequest_When_countIsZero() throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0")
        .param("count", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getAnonymousEnquiries_Should_returnBadRequest_When_countIsLowerThanZero() throws Exception {
    this.mvc.perform(get(GET_ANONYMOUS_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0")
        .param("count", "-10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getRegisteredEnquiries_Should_returnOk_When_requestParamsAreValid() throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0")
        .param("count", "10"))
        .andExpect(status().isOk());
  }

  @Test
  public void getRegisteredEnquiries_Should_returnBadRequest_When_offsetIsMissing() throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getRegisteredEnquiries_Should_returnBadRequest_When_countIsMissing() throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getRegisteredEnquiries_Should_returnBadRequest_When_offsetIsLowerThanZero() throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "-10")
        .param("count", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getRegisteredEnquiries_Should_returnBadRequest_When_countIsZero() throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0")
        .param("count", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getRegisteredEnquiries_Should_returnBadRequest_When_countIsLowerThanZero() throws Exception {
    this.mvc.perform(get(GET_REGISTERED_ENQUIRIES_PATH)
        .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
        .param("offset", "0")
        .param("count", "-10"))
        .andExpect(status().isBadRequest());
  }

}
