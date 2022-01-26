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

  @PersistenceContext
  public EntityManager entityManager;

  @Before("execution(* de.caritas.cob.userservice.api.repository..*(..)))")
  public void beforeQueryAspect() {

    if (Long.valueOf(0L).equals(TenantContext.getCurrentTenant())) {
      return;
    }

    Filter filter = entityManager.unwrap(Session.class)
        .enableFilter("tenantFilter");
    filter.setParameter("tenantId", TenantContext.getCurrentTenant());
    filter.validate();
  }
}
