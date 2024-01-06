package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import javax.persistence.EntityManagerFactory;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.MustJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultantAdminFilterTenantAwareServiceTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  FullTextEntityManager fullTextEntityManager;

  @Mock EntityManagerFactory entityManagerFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  QueryBuilder queryBuilder;

  @Mock BooleanJunction<BooleanJunction> booleanJunction;

  @Mock MustJunction mustJunction;

  @Mock MustJunction secondMustJunction;

  @InjectMocks ConsultantAdminFilterTenantAwareService consultantAdminFilterTenantAwareService;

  @Test
  void
      buildFilteredQuery_Should_CreateFilteredQuery_WithoutTenantFilter_If_TenantContextIsSuperAdmin() {
    // given
    TenantContext.setCurrentTenant(TenantContext.TECHNICAL_TENANT_ID);
    when(fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Consultant.class)
            .get())
        .thenReturn(queryBuilder);
    when(queryBuilder.bool()).thenReturn(booleanJunction);
    when(booleanJunction.must(Mockito.any(Query.class))).thenReturn(mustJunction);

    // when
    consultantAdminFilterTenantAwareService.buildFilteredQuery(
        new ConsultantFilter().agencyId(59L), fullTextEntityManager);

    // then
    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(fullTextEntityManager)
        .createFullTextQuery(captor.capture(), Mockito.eq(Consultant.class));
    verify(booleanJunction).must(Mockito.any(Query.class));
    verify(mustJunction, Mockito.never()).must(Mockito.any(Query.class));
    TenantContext.clear();
  }

  @Test
  void
      buildFilteredQuery_Should_CreateFilteredQuery_WithTenantFilter_When_TenantContextIsNotSuperAdmin() {
    // given
    TenantContext.setCurrentTenant(1L);
    when(fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Consultant.class)
            .get())
        .thenReturn(queryBuilder);
    when(queryBuilder.bool()).thenReturn(booleanJunction);
    when(booleanJunction.must(Mockito.any(Query.class))).thenReturn(mustJunction);
    when(mustJunction.must(null)).thenReturn(secondMustJunction);

    // when
    consultantAdminFilterTenantAwareService.buildFilteredQuery(
        new ConsultantFilter().agencyId(59L), fullTextEntityManager);

    // then
    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(fullTextEntityManager)
        .createFullTextQuery(captor.capture(), Mockito.eq(Consultant.class));
    verify(booleanJunction, Mockito.times(2)).must(Mockito.any(Query.class));
    verify(mustJunction).must(Mockito.any());
    TenantContext.clear();
  }
}
