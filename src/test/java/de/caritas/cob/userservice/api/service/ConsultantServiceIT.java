package de.caritas.cob.userservice.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantmobiletoken.ConsultantMobileTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ConsultantServiceIT {

  @Autowired
  private ConsultantService consultantService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private ConsultantMobileTokenRepository consultantMobileTokenRepository;

  @BeforeEach
  void clearTokens() {
    this.consultantMobileTokenRepository.deleteAll();
  }

  @Test
  void addMobileTokensToConsultant_Should_persistMobileTokens_When_tokensAreUnique() {
    var consultantId = this.consultantRepository.findAll().iterator().next().getId();

    this.consultantService.addMobileAppToken(consultantId, "token");
    this.consultantService.addMobileAppToken(consultantId, "token2");
    this.consultantService.addMobileAppToken(consultantId, "token3");

    var resultConsultant = this.consultantService.getConsultant(consultantId);
    assertThat(resultConsultant.isPresent(), is(true));
    var consultantTokens = resultConsultant.get().getConsultantMobileTokens();
    assertThat(consultantTokens, hasSize(3));
  }

  @Test
  void addMobileTokensToConsultant_Should_throwConflictException_When_tokenAlreadyExists() {
    var consultantId = this.consultantRepository.findAll().iterator().next().getId();

    this.consultantService.addMobileAppToken(consultantId, "token");

    assertThrows(ConflictException.class,
        () -> this.consultantService.addMobileAppToken(consultantId, "token"));
  }

}
