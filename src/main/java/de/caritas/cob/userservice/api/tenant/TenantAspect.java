package de.caritas.cob.userservice.api.tenant;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${multitenancy.enabled:true}")
public class TenantAspect {

  private final Long TECHNICAL_TENANT_ID = 0L;

  @PersistenceContext
  public EntityManager entityManager;

  @Before("execution(* de.caritas.cob.userservice.api.repository..*(..)))")
  public void beforeQueryAspect() {

    if (Long.valueOf(TECHNICAL_TENANT_ID).equals(TenantContext.getCurrentTenant())) {
      return;
    }

    Filter filter = entityManager.unwrap(Session.class)
        .enableFilter("tenantFilter");
    filter.setParameter("tenantId", TenantContext.getCurrentTenant());
    filter.validate();
  }
}
