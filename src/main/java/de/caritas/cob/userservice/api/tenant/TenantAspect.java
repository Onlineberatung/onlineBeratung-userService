package de.caritas.cob.userservice.api.tenant;

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

    if (TenantContext.isTechnicalOrSuperAdminContext()) {
      return;
    }

    var filter = entityManager.unwrap(Session.class).enableFilter("tenantFilter");
    filter.setParameter("tenantId", TenantContext.getCurrentTenant());
    filter.validate();
  }
}
