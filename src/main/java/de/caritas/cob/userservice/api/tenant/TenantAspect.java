package de.caritas.cob.userservice.api.tenant;

import static de.caritas.cob.userservice.api.tenant.TenantResolverService.TECHNICAL_TENANT_ID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${multitenancy.enabled:true}")
public class TenantAspect {

  @PersistenceContext private final @NonNull EntityManager entityManager;

  @Before("execution(* de.caritas.cob.userservice.api.port..*(..)))")
  public void beforeQueryAspect() {

    if (TECHNICAL_TENANT_ID.equals(TenantContext.getCurrentTenant())) {
      return;
    }

    var filter = entityManager.unwrap(Session.class).enableFilter("tenantFilter");
    filter.setParameter("tenantId", TenantContext.getCurrentTenant());
    filter.validate();
  }
}
