package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO.PresenceStatus;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceListDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceOtherDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.config.apiclient.TopicServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=testing")
@TestPropertySource(properties = "feature.topics.enabled=true")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ConversationControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);
  public static final String FIRST_TOPIC_NAME = "topic name";
  public static final String FIRST_TOPIC_DESC = "topic desc";
  public static final String FIRST_TOPIC_STATUS = "INACTIVE";

  @Autowired private MockMvc mockMvc;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private SessionRepository sessionRepository;

  @Autowired private UserRepository userRepository;

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @MockBean
  @Qualifier("restTemplate")
  private RestTemplate restTemplate;

  @MockBean
  @Qualifier("rocketChatRestTemplate")
  private RestTemplate rocketChatRestTemplate;

  @MockBean
  @Qualifier("topicControllerApiPrimary")
  private TopicControllerApi topicControllerApi;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ConsultingTypeControllerApi consultingTypeControllerApi;

  @MockBean private TopicServiceApiControllerFactory topicServiceApiControllerFactory;

  private Consultant consultant;

  private Session session;

  private LanguageCode initialLanguageCode;

  private boolean deleteSession;

  private User user;

  @AfterEach
  public void deleteAndRestore() {
    consultant = null;
    user = null;
    if (nonNull(session)) {
      if (deleteSession) {
        sessionRepository.delete(session);
        deleteSession = false;
      } else {
        session.setLanguageCode(initialLanguageCode);
        sessionRepository.save(session);
      }
      session = null;
    }
    initialLanguageCode = null;
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getRegisteredEnquiriesShouldExposeDefaultLanguageAndRespondWithOk() throws Exception {
    givenAnAuthenticatedConsultantWithMultipleAgencies();
    givenRocketChatSubscriptionUpdate();
    givenRocketChatRoomsGet();
    givenAValidTopicServiceResponse();

    mockMvc
        .perform(
            get("/conversations/consultants/enquiries/registered")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("offset", is(0)))
        .andExpect(jsonPath("count", is(1)))
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.agencyId", is(121)))
        .andExpect(jsonPath("sessions[0].session.language", is("de")))
        .andExpect(jsonPath("sessions[0].session.topic.id", is(1)))
        .andExpect(jsonPath("sessions[0].session.topic.name", is(FIRST_TOPIC_NAME)))
        .andExpect(jsonPath("sessions[0].session.topic.description", is(FIRST_TOPIC_DESC)))
        .andExpect(jsonPath("sessions[0].session.topic.status", is(FIRST_TOPIC_STATUS)));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  void getRegisteredEnquiriesShouldExposeSetLanguageAndRespondWithOk() throws Exception {
    givenAnAuthenticatedConsultantWithMultipleAgencies();
    givenASessionWithASetLanguage();
    givenRocketChatSubscriptionUpdate();
    givenRocketChatRoomsGet();
    givenAValidTopicServiceResponse();

    mockMvc
        .perform(
            get("/conversations/consultants/enquiries/registered")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .param("offset", "0")
                .param("count", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("offset", is(0)))
        .andExpect(jsonPath("count", is(1)))
        .andExpect(jsonPath("total", is(1)))
        .andExpect(jsonPath("sessions", hasSize(1)))
        .andExpect(jsonPath("sessions[0].session.agencyId", is(121)))
        .andExpect(jsonPath("sessions[0].session.language", is(session.getLanguageCode().name())))
        .andExpect(jsonPath("sessions[0].session.topic.id", is(1)))
        .andExpect(jsonPath("sessions[0].session.topic.name", is(FIRST_TOPIC_NAME)))
        .andExpect(jsonPath("sessions[0].session.topic.description", is(FIRST_TOPIC_DESC)))
        .andExpect(jsonPath("sessions[0].session.topic.status", is(FIRST_TOPIC_STATUS)));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ANONYMOUS_DEFAULT)
  void getAnonymousEnquiryDetailsShouldRespondWithNotFoundIfSessionDoesNotExist() throws Exception {
    var sessionId = givenAnUnknownSessionId();

    mockMvc
        .perform(
            get("/conversations/anonymous/{sessionId}", sessionId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ANONYMOUS_DEFAULT)
  void getAnonymousEnquiryDetailsShouldRespondWithForbiddenIfAuthenticatedUserNotFromSession()
      throws Exception {
    givenAnAnonymousAuthenticatedUser();
    givenAConsultantWithMultipleAgencies();
    givenANewAnonymousSession();

    mockMvc
        .perform(
            get("/conversations/anonymous/1")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ANONYMOUS_DEFAULT)
  void getAnonymousEnquiryDetailsShouldRespondIfNoneAvailable() throws Exception {
    givenAnAnonymousAuthenticatedUser();
    givenAConsultantWithMultipleAgencies();
    givenANewAnonymousSession();
    givenAValidRocketChatSystemUser();
    givenRocketChatUsersPresenceGet();

    mockMvc
        .perform(
            get("/conversations/anonymous/{sessionId}", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("numAvailableConsultants", is(0)))
        .andExpect(jsonPath("status", is("NEW")));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ANONYMOUS_DEFAULT)
  void getAnonymousEnquiryDetailsShouldRespondIfConsultantsAvailable() throws Exception {
    givenAnAnonymousAuthenticatedUser();
    givenAConsultantWithMultipleAgencies();
    givenANewAnonymousSession();
    givenAValidRocketChatSystemUser();
    givenRocketChatUsersPresenceGet(consultant.getRocketChatId());
    givenConsultingTypeServiceResponse(session.getConsultingTypeId());

    mockMvc
        .perform(
            get("/conversations/anonymous/{sessionId}", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("numAvailableConsultants", is(1)))
        .andExpect(jsonPath("status", is("NEW")));
  }

  private void givenAValidTopicServiceResponse() {
    var roomsGetDTO = new RoomsGetDTO();
    roomsGetDTO.setUpdate(new RoomsUpdateDTO[] {});
    var firstTopic =
        new de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO()
            .id(1L)
            .name(FIRST_TOPIC_NAME)
            .description(FIRST_TOPIC_DESC)
            .status(FIRST_TOPIC_STATUS);
    var secondTopic =
        new de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO()
            .id(2L)
            .name("topic name 2")
            .description("topic desc 2")
            .status("ACTIVE");
    when(topicServiceApiControllerFactory.createControllerApi()).thenReturn(topicControllerApi);
    when(topicControllerApi.getApiClient())
        .thenReturn(new de.caritas.cob.userservice.topicservice.generated.ApiClient());
    when(topicControllerApi.getAllTopics()).thenReturn(Lists.newArrayList(firstTopic, secondTopic));
  }

  private void givenAnAuthenticatedConsultantWithMultipleAgencies() {
    consultant =
        consultantRepository.findById("45816eb6-984b-411f-a818-996cd16e1f2a").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
  }

  private void givenAConsultantWithMultipleAgencies() {
    consultant =
        consultantRepository.findById("45816eb6-984b-411f-a818-996cd16e1f2a").orElseThrow();
  }

  private void givenRocketChatRoomsGet() {
    var roomsGetDTO = new RoomsGetDTO();
    RoomsUpdateDTO[] roomUpdates = {easyRandom.nextObject(RoomsUpdateDTO.class)};
    roomsGetDTO.setUpdate(roomUpdates);
    when(restTemplate.exchange(anyString(), any(), any(), eq(RoomsGetDTO.class)))
        .thenReturn(ResponseEntity.ok(roomsGetDTO));
  }

  private void givenRocketChatUsersPresenceGet(String... availableChatIds) {
    int numUsersLoggedIn = availableChatIds.length + 1 + easyRandom.nextInt(10);
    var users =
        easyRandom.objects(PresenceOtherDTO.class, numUsersLoggedIn).collect(Collectors.toList());
    for (int i = 0; i < availableChatIds.length; i++) {
      users.get(i).setId(availableChatIds[i]);
      users.get(i).setStatus(PresenceStatus.ONLINE);
    }
    for (int i = availableChatIds.length; i < numUsersLoggedIn; i++) {
      users.get(i).setStatus(aNotAvailableLoggedInStatus());
    }

    var presenceList = new PresenceListDTO();
    presenceList.setSuccess(true);
    presenceList.setUsers(users);

    when(rocketChatRestTemplate.exchange(
            contains("users.presence"), eq(HttpMethod.GET), any(), eq(PresenceListDTO.class)))
        .thenReturn(ResponseEntity.ok(presenceList));
  }

  private PresenceStatus aNotAvailableLoggedInStatus() {
    PresenceStatus status;
    do {
      status = easyRandom.nextObject(PresenceStatus.class);
    } while (status == PresenceStatus.ONLINE || status == PresenceStatus.OFFLINE);

    return status;
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

  private void givenAnAnonymousAuthenticatedUser() {
    user = userRepository.findById("9c4057d0-05ad-4e86-a47c-dc5bdeec03b9").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUser.getRoles()).thenReturn(Set.of("anonymous"));
  }

  private void givenANewAnonymousSession() {
    session = new Session();
    session.setUser(user);
    session.setConsultingTypeId(1);
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    session.setLanguageCode(LanguageCode.de);
    session.setPostcode(RandomStringUtils.randomNumeric(5));
    session.setAgencyId(consultant.getConsultantAgencies().iterator().next().getAgencyId());
    session.setStatus(SessionStatus.NEW);
    session.setTeamSession(false);
    session.setCreateDate(LocalDateTime.now());
    session.setGroupId(RandomStringUtils.randomAlphabetic(17));
    session.setIsConsultantDirectlySet(false);

    sessionRepository.save(session);
    deleteSession = true;
  }

  private Long givenAnUnknownSessionId() {
    Long sessionId;
    do {
      sessionId = (long) easyRandom.nextInt(1000);
    } while (sessionRepository.existsById(sessionId));

    return sessionId;
  }

  private void givenAValidRocketChatSystemUser() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getSystemUserSneaky()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rocketChatCredentialsProvider.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
  }

  private void givenConsultingTypeServiceResponse(Integer consultingTypeId) {
    consultingTypeControllerApi.getApiClient().setBasePath("https://www.google.de/");
    when(restTemplate.getUriTemplateHandler())
        .thenReturn(
            new UriTemplateHandler() {
              @SneakyThrows
              @Override
              public @NonNull URI expand(
                  @NonNull String uriTemplate, @NonNull Map<String, ?> uriVariables) {
                return new URI("");
              }

              @SneakyThrows
              @Override
              public @NonNull URI expand(
                  @NonNull String uriTemplate, Object @NonNull ... uriVariables) {
                return new URI("");
              }
            });

    var agencyId = consultant.getConsultantAgencies().iterator().next().getAgencyId();

    var body = new de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO();
    body.setConsultingType(consultingTypeId);
    body.setId(agencyId);
    ParameterizedTypeReference<
            java.util.List<
                de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO>>
        value = new ParameterizedTypeReference<>() {};
    when(restTemplate.exchange(any(RequestEntity.class), eq(value)))
        .thenReturn(ResponseEntity.ok(List.of(body)));
  }
}
