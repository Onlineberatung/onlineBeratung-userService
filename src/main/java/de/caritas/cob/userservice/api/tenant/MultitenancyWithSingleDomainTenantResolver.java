package de.caritas.cob.userservice.api.tenant;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.httpheader.HttpHeadersResolver;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MultitenancyWithSingleDomainTenantResolver implements TenantResolver {

  private static final String USERS_CONSULTANTS = "/users/consultants/";
  private static final String USERS_CONSULTANTS_BY_ID_URL_REGEX = USERS_CONSULTANTS + "[a-z0-9-]+";

  @Value("${feature.multitenancy.with.single.domain.enabled}")
  private boolean multitenancyWithSingleDomain;

  private final @NonNull AgencyService agencyService;

  private final @NonNull HttpHeadersResolver httpHeadersResolver;

  private final @NonNull ConsultantService consultantService;

  @Override
  public Optional<Long> resolve(HttpServletRequest request) {
    if (multitenancyWithSingleDomain) {
      Optional<Long> tenantIDfromAgency = resolveTenantFromAgency();
      if (tenantIDfromAgency.isEmpty() && requestParameterContainsConsultantId()) {
        return resolveTenantFromConsultantRequestParameter();
      } else {
        return tenantIDfromAgency;
      }
    }
    return Optional.empty();
  }

  private Optional<Long> resolveTenantFromConsultantRequestParameter() {
    // temporarily set technical tenant to be able to run query during tenant determination
    TenantContext.setCurrentTenant(0L);
    Optional<Consultant> consultant = consultantService.getConsultant(getConsultantId());
    TenantContext.clear();
    if (consultant.isPresent()) {
      return Optional.of(consultant.get().getTenantId());
    }
    return Optional.empty();
  }

  private boolean requestParameterContainsConsultantId() {
    HttpServletRequest request = getRequest();
    return request.getRequestURI().matches(USERS_CONSULTANTS_BY_ID_URL_REGEX);
  }

  private String getConsultantId() {
    return getRequest().getRequestURI().replace(USERS_CONSULTANTS, "");
  }

  private HttpServletRequest getRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }

  private Optional<Long> resolveTenantFromAgency() {
    Optional<Long> agencyId = httpHeadersResolver.findHeaderValue("agencyId");
    if (agencyId.isEmpty()) {
      log.debug("Agency id is empty, multitenancyWithSingleDomainTenantResolver does not resolve");
      return Optional.empty();
    }
    AgencyDTO agency = agencyService.getAgency(agencyId.get());
    validateResolvedAgencyContainsTenant(agency);
    return Optional.of(agency.getTenantId());
  }

  private void validateResolvedAgencyContainsTenant(AgencyDTO agency) {
    if (agency.getTenantId() == null) {
      throw new BadRequestException(
          "Cannot resolve tenant, as the resolved agency has null tenantId!");
    }
  }

  @Override
  public boolean canResolve(HttpServletRequest request) {
    return resolve(request).isPresent();
  }
}
