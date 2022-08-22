package de.caritas.cob.userservice.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.port.out.ConsultantMobileTokenRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;

class ConsultantServiceITBase {

  @Autowired private ConsultantService consultantService;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired protected ConsultantMobileTokenRepository consultantMobileTokenRepository;

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

  void addMobileTokensToConsultant_Should_throwConflictException_When_tokenAlreadyExists() {
    TenantContext.setCurrentTenant(1L);
    var consultantId = this.consultantRepository.findAll().iterator().next().getId();

    this.consultantService.addMobileAppToken(consultantId, "token");

    assertThrows(
        ConflictException.class,
        () -> this.consultantService.addMobileAppToken(consultantId, "token"));
    TenantContext.clear();
  }
}
