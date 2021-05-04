package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.api.controller.ConversationControllerIT.POST_CREATE_ANONYMOUS_ENQUIRY_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.facade.conversation.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

  @Autowired private MockMvc mvc;

  @MockBean
  private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

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
  public void getAnonymousEnquiries_Should_ReturnForbiddenAndCallNoMethods_When_NoCsrfToken()
      throws Exception {
    this.mvc.perform(post(POST_CREATE_ANONYMOUS_ENQUIRY_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(createAnonymousEnquiryFacade);
  }
}