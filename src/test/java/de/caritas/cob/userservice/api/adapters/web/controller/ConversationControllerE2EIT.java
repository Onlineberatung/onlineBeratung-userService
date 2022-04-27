package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConversationControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private SessionRepository sessionRepository;

  @MockBean
  private AuthenticatedUser authenticatedUser;

  @MockBean
  @Qualifier("restTemplate")
  private RestTemplate restTemplate;

  private Consultant consultant;

  private Session session;

  private LanguageCode initialLanguageCode;

  @AfterEach
  public void deleteAndRestore() {
    consultant = null;
    if (nonNull(session)) {
      session.setLanguageCode(initialLanguageCode);
      sessionRepository.save(session);
      session = null;
    }
    initialLanguageCode = null;
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getRegisteredEnquiriesShouldExposeDefaultLanguageAndRespondWithOk() throws Exception {
    givenAConsultantWithMultipleAgencies();
    givenRocketChatSubscriptionUpdate();
    givenRocketChatRoomsGet();

    mockMvc.perform(
            get("/conversations/consultants/enquiries/registered")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "100")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("offset", is(0)))
        .andExpect(jsonPath("count", is(1)))
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.agencyId", is(121)))
        .andExpect(jsonPath("sessions[0].session.language", is("de")));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void getRegisteredEnquiriesShouldExposeSetLanguageAndRespondWithOk() throws Exception {
    givenAConsultantWithMultipleAgencies();
    givenASessionWithASetLanguage();
    givenRocketChatSubscriptionUpdate();
    givenRocketChatRoomsGet();

    mockMvc.perform(
            get("/conversations/consultants/enquiries/registered")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "100")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("offset", is(0)))
        .andExpect(jsonPath("count", is(1)))
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.agencyId", is(121)))
        .andExpect(jsonPath("sessions[0].session.language", is(session.getLanguageCode().name())));
  }

  private void givenAConsultantWithMultipleAgencies() {
    consultant = consultantRepository.findById("45816eb6-984b-411f-a818-996cd16e1f2a")
        .orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
  }

  private void givenRocketChatRoomsGet() {
    var roomsGetDTO = new RoomsGetDTO();
    RoomsUpdateDTO[] roomUpdates = {easyRandom.nextObject(RoomsUpdateDTO.class)};
    roomsGetDTO.setUpdate(roomUpdates);
    when(restTemplate.exchange(anyString(), any(), any(), eq(RoomsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(roomsGetDTO));
  }

  private void givenRocketChatSubscriptionUpdate() {
    var subscriptionsGetDTO = new SubscriptionsGetDTO();
    subscriptionsGetDTO.setSuccess(true);
    SubscriptionsUpdateDTO[] subscriptionUpdates = {
        easyRandom.nextObject(SubscriptionsUpdateDTO.class)
    };
    subscriptionsGetDTO.setUpdate(subscriptionUpdates);
    when(restTemplate.exchange(anyString(), any(), any(), eq(SubscriptionsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(subscriptionsGetDTO));
  }

  private void givenASessionWithASetLanguage() {
    session = sessionRepository.findById(1200L).orElseThrow();
    initialLanguageCode = session.getLanguageCode();
    session.setLanguageCode(easyRandom.nextObject(LanguageCode.class));
    sessionRepository.save(session);
  }
}
