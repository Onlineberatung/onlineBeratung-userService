package de.caritas.cob.userservice.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.httpheader.HttpHeadersResolver;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class MultitenancyWithSingleDomainTenantResolverTest {

  private static final String CONSULTANT_ID = "cid-1234";
  private static final long ANOTHER_TENANT = 2L;

  @InjectMocks
  MultitenancyWithSingleDomainTenantResolver multitenancyWithSingleDomainTenantResolver;

  @Mock HttpServletRequest request;

  @Mock AgencyService agencyService;

  @Mock HttpHeadersResolver headersResolver;

  @Mock ConsultantService consultantService;

  @Mock private ServletRequestAttributes requestAttributes;

  @BeforeEach
  public void initialize() {
    TenantContext.clear();
  }

  @AfterEach
  public void tearDown() {
    resetRequestAttributes();
  }

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
      resolve_Should_GetTenantIdFromConsultant_When_FeatureMultitenancyWithSingleDomainIsEnabledAndNoAgencyIdIsProvidedAndUrlMatchesConsultantGetById() {
    // given
    givenRequestContextIsSet();
    ReflectionTestUtils.setField(
        multitenancyWithSingleDomainTenantResolver, "multitenancyWithSingleDomain", true);

    when(headersResolver.findHeaderValue("agencyId")).thenReturn(Optional.empty());
    when(request.getRequestURI()).thenReturn("/users/consultants/" + CONSULTANT_ID);

    EasyRandom random = new EasyRandom();
    Consultant consultant = random.nextObject(Consultant.class);
    consultant.setTenantId(ANOTHER_TENANT);
    when(consultantService.getConsultant(CONSULTANT_ID)).thenReturn(Optional.of(consultant));
    // when
    assertThat(multitenancyWithSingleDomainTenantResolver.canResolve(request)).isTrue();
    assertThat(multitenancyWithSingleDomainTenantResolver.resolve(request))
        .isEqualTo(Optional.of(ANOTHER_TENANT));
    assertThat(TenantContext.getCurrentTenant()).isNull();
  }

  @Test
  void
      resolve_Should_ResolveToEmptyTenant_When_FeatureMultitenancyWithSingleDomainIsEnabledAndNoAgencyIdIsProvidedAndUrlDoesNotMatchConsultantGetById() {
    // given
    givenRequestContextIsSet();
    ReflectionTestUtils.setField(
        multitenancyWithSingleDomainTenantResolver, "multitenancyWithSingleDomain", true);

    when(headersResolver.findHeaderValue("agencyId")).thenReturn(Optional.empty());
    when(request.getRequestURI()).thenReturn("/users/sessions/1");

    EasyRandom random = new EasyRandom();
    Consultant consultant = random.nextObject(Consultant.class);
    consultant.setTenantId(ANOTHER_TENANT);
    // when
    assertThat(multitenancyWithSingleDomainTenantResolver.canResolve(request)).isFalse();
    assertThat(multitenancyWithSingleDomainTenantResolver.resolve(request)).isEmpty();
    assertThat(TenantContext.getCurrentTenant()).isNull();
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

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }

  private void givenRequestContextIsSet() {
    when(requestAttributes.getRequest()).thenReturn(request);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }
}
