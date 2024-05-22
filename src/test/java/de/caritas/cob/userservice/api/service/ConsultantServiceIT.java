package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.UserServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ConsultantServiceIT extends ConsultantServiceITBase {

  @BeforeEach
  void clearTokens() {
    this.consultantMobileTokenRepository.deleteAll();
  }

  @Test
  void addMobileTokensToConsultant_Should_persistMobileTokens_When_tokensAreUnique() {
    super.addMobileTokensToConsultant_Should_persistMobileTokens_When_tokensAreUnique();
  }

  @Test
  void addMobileTokensToConsultant_Should_throwConflictException_When_tokenAlreadyExists() {
    super.addMobileTokensToConsultant_Should_throwConflictException_When_tokenAlreadyExists();
  }
}
