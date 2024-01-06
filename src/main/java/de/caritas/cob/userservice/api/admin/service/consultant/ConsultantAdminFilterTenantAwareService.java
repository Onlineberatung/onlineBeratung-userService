package de.caritas.cob.userservice.api.admin.service.consultant;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.admin.service.consultant.querybuilder.ConsultantFilterQueryBuilder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import javax.persistence.EntityManagerFactory;
import lombok.NonNull;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Service class to provide filtered search for all {@link Consultant} entities based on tenant. */
@Service
@Primary
@ConditionalOnExpression("${multitenancy.enabled:true}")
public class ConsultantAdminFilterTenantAwareService extends ConsultantAdminFilterService {

  protected static final String TENANT_ID_SEARCH_FIELD = "tenantId";

  public ConsultantAdminFilterTenantAwareService(
      @NonNull EntityManagerFactory entityManagerFactory) {
    super(entityManagerFactory);
  }

  @Override
  protected FullTextQuery buildFilteredQuery(
      ConsultantFilter consultantFilter, FullTextEntityManager fullTextEntityManager) {

    QueryBuilder queryBuilder =
        fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Consultant.class)
            .get();

    Query tenantQuery =
        fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Consultant.class)
            .get()
            .keyword()
            .onField(TENANT_ID_SEARCH_FIELD)
            .matching(TenantContext.getCurrentTenant())
            .createQuery();

    Query query =
        ConsultantFilterQueryBuilder.getInstance(queryBuilder)
            .onConsultantFilter(consultantFilter)
            .buildQuery();

    Query resultQuery =
        TenantContext.isTechnicalOrSuperAdminContext()
            ? query
            : queryBuilder.bool().must(tenantQuery).must(query).createQuery();
    return fullTextEntityManager.createFullTextQuery(resultQuery, Consultant.class);
  }
}
