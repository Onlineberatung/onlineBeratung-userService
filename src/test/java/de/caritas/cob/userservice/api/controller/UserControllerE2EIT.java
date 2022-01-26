package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN_HEADER_PARAMETER_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID_HEADER_PARAMETER_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.rocketchat.RocketChatUserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultant.Language;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
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
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UserControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SessionRepository sessionRepository;

  @MockBean
  private AuthenticatedUser authenticatedUser;

  @MockBean
  private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @MockBean
  private RestTemplate restTemplate;

  private User user;

  private Consultant consultant;

  private Session session;

  private UpdateConsultantDTO updateConsultantDTO;

  private EnquiryMessageDTO enquiryMessageDTO;

  @AfterEach
  public void deleteObjects() {
    user = null;
    session = null;
    consultant = null;
    updateConsultantDTO = null;
    enquiryMessageDTO = null;
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void createEnquiryMessageWithLanguageShouldSaveLanguageAndRespondWithCreated()
      throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatInfoResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(true);

    mockMvc.perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")));

    var savedSession = sessionRepository.findById(session.getId());
    assertTrue(savedSession.isPresent());
    assertEquals(
        LanguageCode.getByCode(enquiryMessageDTO.getLanguage().getValue()),
        savedSession.get().getLanguageCode()
    );

    restoreSession();
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_DEFAULT})
  public void createEnquiryMessageWithoutLanguageShouldSaveDefaultLanguageAndRespondWithCreated()
      throws Exception {
    givenAUserWithASessionNotEnquired();
    givenValidRocketChatInfoResponse();
    givenValidRocketChatCreationResponse();
    givenAnEnquiryMessageDto(false);

    mockMvc.perform(
            post("/users/sessions/{sessionId}/enquiry/new", session.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .header(RC_TOKEN_HEADER_PARAMETER_NAME, RC_TOKEN)
                .header(RC_USER_ID_HEADER_PARAMETER_NAME, RC_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryMessageDTO))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("sessionId", is(session.getId().intValue())))
        .andExpect(jsonPath("rcGroupId", is("rcGroupId")));

    var savedSession = sessionRepository.findById(session.getId());
    assertTrue(savedSession.isPresent());
    assertEquals(LanguageCode.de, savedSession.get().getLanguageCode());

    restoreSession();
  }

  @Test
  @WithMockUser
  public void getConsultantPublicDataShouldRespondWithOk() throws Exception {
    givenAConsultantWithMultipleAgencies();

    mockMvc.perform(
            get("/users/consultants/{consultantId}", consultant.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("consultantId", is(consultant.getId())))
        .andExpect(jsonPath("firstName").doesNotExist())
        .andExpect(jsonPath("lastName").doesNotExist())
        .andExpect(jsonPath("agencies", hasSize(24)))
        .andExpect(jsonPath("agencies[0].id", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].name", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].postcode", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].city", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].description", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].teamAgency", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].offline", is(notNullValue())))
        .andExpect(jsonPath("agencies[0].consultingType", is(notNullValue())));

    assertEquals(24, consultant.getConsultantAgencies().size());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldSaveDefaultLanguageAndRespondWithOk() throws Exception {
    givenAValidConsultant();
    givenAMinimalUpdateConsultantDto(consultant.getEmail());
    givenValidRocketChatInfoResponse();

    mockMvc.perform(put("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    var savedLanguages = savedConsultant.get().getLanguages();
    assertEquals(1, savedLanguages.size());
    assertEquals(LanguageCode.de, savedLanguages.iterator().next().getLanguageCode());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldSaveGivenLanguagesAndRespondWithOk() throws Exception {
    givenAValidConsultant();
    givenAnUpdateConsultantDtoWithLanguages(consultant.getEmail());
    givenValidRocketChatInfoResponse();

    mockMvc.perform(put("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    var savedLanguages = savedConsultant.get().getLanguages();
    assertEquals(3, savedLanguages.size());
    savedLanguages.forEach(language -> assertTrue(updateConsultantDTO.getLanguages().contains(
        de.caritas.cob.userservice.api.model.LanguageCode.fromValue(
            language.getLanguageCode().toString()
        )
    )));
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldCascadeLanguageDeletionAndRespondWithOk() throws Exception {
    givenAValidConsultantSpeaking(easyRandom.nextObject(LanguageCode.class));
    givenAnUpdateConsultantDtoWithLanguages(consultant.getEmail());
    givenValidRocketChatInfoResponse();

    mockMvc.perform(put("/users/data")
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateConsultantDTO))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var savedConsultant = consultantRepository.findById(consultant.getId());
    assertTrue(savedConsultant.isPresent());
    var savedLanguages = savedConsultant.get().getLanguages();
    assertEquals(3, savedLanguages.size());
    savedLanguages.forEach(language -> assertTrue(updateConsultantDTO.getLanguages().contains(
        de.caritas.cob.userservice.api.model.LanguageCode.fromValue(
            language.getLanguageCode().toString())
    )));
  }

  private void givenAConsultantWithMultipleAgencies() {
    consultant = consultantRepository.findById("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe")
        .orElseThrow();
  }

  private void givenAValidConsultant() {
    consultant = consultantRepository.findAll().iterator().next();
    when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
  }

  private void givenAValidConsultantSpeaking(LanguageCode languageCode) {
    givenAValidConsultant();
    consultant.setLanguages(Set.of(new Language(consultant, languageCode)));
    consultant = consultantRepository.save(consultant);
  }

  private void givenAMinimalUpdateConsultantDto(String email) {
    updateConsultantDTO = new UpdateConsultantDTO()
        .email(email)
        .firstname(RandomStringUtils.randomAlphabetic(8))
        .lastname(RandomStringUtils.randomAlphabetic(12));
  }

  private void givenAnUpdateConsultantDtoWithLanguages(String email) {
    givenAMinimalUpdateConsultantDto(email);

    var languages = List.of(
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.LanguageCode.class),
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.LanguageCode.class),
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.LanguageCode.class)
    );
    updateConsultantDTO.languages(languages);
  }

  private void givenValidRocketChatInfoResponse() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    var body = new UserInfoResponseDTO();
    body.setSuccess(true);
    if (nonNull(user)) {
      body.setUser(new RocketChatUserDTO("", user.getUsername(), null));
    }
    var userInfoResponseDTO = ResponseEntity.ok(body);
    when(restTemplate.exchange(anyString(), any(), any(), eq(UserInfoResponseDTO.class)))
        .thenReturn(userInfoResponseDTO);
    when(restTemplate.exchange(anyString(), any(), any(), eq(UserInfoResponseDTO.class),
        anyString())).thenReturn(userInfoResponseDTO);
  }

  @SuppressWarnings("unchecked")
  private void givenValidRocketChatCreationResponse() {
    var uriTemplateHandler = mock(UriTemplateHandler.class);
    when(uriTemplateHandler.expand(anyString(), anyMap()))
        .thenReturn(easyRandom.nextObject(URI.class));
    when(restTemplate.getUriTemplateHandler()).thenReturn(uriTemplateHandler);
    when(restTemplate.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
        .thenReturn(ResponseEntity.ok().build());
  }

  private void givenAUserWithASessionNotEnquired() {
    user = userRepository.findById("552d3f10-1b6d-47ee-aec5-b88fbf988f9e").orElseThrow();
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    session = user.getSessions().stream()
        .filter(s -> isNull(s.getEnquiryMessageDate()))
        .findFirst()
        .orElseThrow();
  }

  private void givenAnEnquiryMessageDto(boolean isLanguageSet) {
    enquiryMessageDTO = easyRandom.nextObject(EnquiryMessageDTO.class);
    if (!isLanguageSet) {
      enquiryMessageDTO.setLanguage(null);
    }
  }

  private void restoreSession() {
    session.setEnquiryMessageDate(null);
    sessionRepository.save(session);
  }
}
