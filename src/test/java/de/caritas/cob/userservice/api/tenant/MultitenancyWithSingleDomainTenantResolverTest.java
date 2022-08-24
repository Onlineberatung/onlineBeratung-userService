package de.caritas.cob.userservice.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.httpheader.HttpHeadersResolver;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MultitenancyWithSingleDomainTenantResolverTest {

  @InjectMocks
  MultitenancyWithSingleDomainTenantResolver multitenancyWithSingleDomainTenantResolver;

  @Mock HttpServletRequest request;

  @Mock AgencyService agencyService;

  @Mock HttpHeadersResolver headersResolver;

  @Test
  void resolve_Should_ResolveToEmpty_When_FeatureMultitenancyWithSingleDomainIsDisabled() {
    // when, then
    assertThat(multitenancyWithSingleDomainTenantResolver.canResolve(request)).isFalse();
    assertThat(multitenancyWithSingleDomainTenantResolver.resolve(request)).isEmpty();
  }

  @Test
  void
      resolve_Should_CallAgencyServiceToResolveTenant_When_FeatureMultitenancyWithSingleDomainIsEnabled() {
    // given
    ReflectionTestUtils.setField(
        multitenancyWithSingleDomainTenantResolver, "multitenancyWithSingleDomain", true);
    when(headersResolver.findHeaderValue("agencyId")).thenReturn(Optional.of(1L));
    when(agencyService.getAgency(1L)).thenReturn(new AgencyDTO().tenantId(2L));
    // when
    assertThat(multitenancyWithSingleDomainTenantResolver.canResolve(request)).isTrue();
    assertThat(multitenancyWithSingleDomainTenantResolver.resolve(request))
        .isEqualTo(Optional.of(2L));
  }

  @Test
  void
      resolve_Should_ThrowBadRequestException_When_AgencyIdProvidedInHeader_ButAgencyDoesNotContainValidTenantId() {
    // given
    ReflectionTestUtils.setField(
        multitenancyWithSingleDomainTenantResolver, "multitenancyWithSingleDomain", true);
    when(headersResolver.findHeaderValue("agencyId")).thenReturn(Optional.of(1L));
    when(agencyService.getAgency(1L)).thenReturn(new AgencyDTO());
    // when, then
    assertThrows(
        BadRequestException.class,
        () -> multitenancyWithSingleDomainTenantResolver.resolve(request));
  }
}
