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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class MultitenancyWithSingleDomainTenantResolver implements TenantResolver {
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
    Optional<Consultant> consultant = consultantService.getConsultant(getConsultantId());
    if (consultant.isPresent()) {
      return Optional.of(consultant.get().getTenantId());
    }
    return Optional.empty();
  }

  private boolean requestParameterContainsConsultantId() {
    return StringUtils.isNotBlank(getConsultantId());
  }

  private String getConsultantId() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    return request.getParameter("cid");
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
