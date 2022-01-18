package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultant.Language;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import java.util.List;
import java.util.Set;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
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

  @MockBean
  private AuthenticatedUser authenticatedUser;

  @MockBean
  private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @MockBean
  private RestTemplate restTemplate;

  private Consultant consultant;

  private UpdateConsultantDTO updateConsultantDTO;

  @AfterEach
  public void deleteObjects() {
    consultant = null;
    updateConsultantDTO = null;
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldSaveDefaultLanguageAndRespondWithOk() throws Exception {
    givenAValidConsultant();
    givenAMinimalUpdateConsultantDto(consultant.getEmail());
    givenAValidRocketChatResponse();

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
    givenAValidRocketChatResponse();

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
    savedLanguages.forEach(language -> assertTrue(
            updateConsultantDTO.getLanguages().contains(language.getLanguageCode().toString())
        )
    );
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT})
  public void updateUserDataShouldCascadeLanguageDeletionAndRespondWithOk() throws Exception {
    givenAValidConsultantSpeaking(easyRandom.nextObject(LanguageCode.class));
    givenAnUpdateConsultantDtoWithLanguages(consultant.getEmail());
    givenAValidRocketChatResponse();

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
    savedLanguages.forEach(language -> assertTrue(
            updateConsultantDTO.getLanguages().contains(language.getLanguageCode().toString())
        )
    );
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
        easyRandom.nextObject(LanguageCode.class).toString(),
        easyRandom.nextObject(LanguageCode.class).toString(),
        easyRandom.nextObject(LanguageCode.class).toString()
    );
    updateConsultantDTO.languages(languages);
  }

  private void givenAValidRocketChatResponse() throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    var body = new UserInfoResponseDTO();
    body.setSuccess(true);
    var userInfoResponseDTO = ResponseEntity.ok(body);
    when(restTemplate.exchange(anyString(), any(), any(), eq(UserInfoResponseDTO.class)))
        .thenReturn(userInfoResponseDTO);
  }
}
