package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.testHelper.RequestBodyConstants.INVALID_USER_REQUEST_BODY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.facade.conversation.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=testing")
public class ConversationControllerIT {

  static final String POST_CREATE_ANONYMOUS_ENQUIRY_PATH = "/conversations/askers/anonymous/new";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean
  private LinkDiscoverers linkDiscoverers;

  @MockBean
  private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  @Test
  public void createAnonymousEnquiry_Should_ReturnCreated_WhenProvidedWithValidRequestBody()
      throws Exception {

    mvc.perform(post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
        .content(new ObjectMapper().writeValueAsString(new EasyRandom().nextObject(
            CreateAnonymousEnquiryDTO.class)))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  public void createAnonymousEnquiry_Should_ReturnBadRequest_WhenProvidedWithInvalidRequestBody()
      throws Exception {

    mvc.perform(post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
        .content(INVALID_USER_REQUEST_BODY)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}
