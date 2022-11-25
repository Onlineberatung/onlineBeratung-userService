package de.caritas.cob.userservice.api.admin.service.admin.search.querybuilder;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.admin.service.FilterQueryBuilder;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

public class AdminFilterQueryBuilder extends FilterQueryBuilder {

  private static final String FIELD_USERNAME = "username";
  private static final String FIELD_LAST_NAME = "lastName";
  private static final String FIELD_EMAIL = "email";
  private static final String FIELD_AGENCY_IDS = "adminAgencies.agencyId";

  private AdminFilter adminFilter;

  protected AdminFilterQueryBuilder(final QueryBuilder queryBuilder) {
    super(queryBuilder);
  }

  public static AdminFilterQueryBuilder getInstance(QueryBuilder queryBuilder) {
    return new AdminFilterQueryBuilder(queryBuilder);
  }

  public AdminFilterQueryBuilder onAdminFilter(AdminFilter adminFilter) {
    this.adminFilter = adminFilter;
    return this;
  }

  @Override
  public Query buildQuery() {
    return nonNull(this.adminFilter) ? buildFilteredQuery() : this.queryBuilder.all().createQuery();
  }

  @Override
  protected Query buildFilteredQuery() {
    BooleanJunction<BooleanJunction> junction = this.queryBuilder.bool();

    addStringFilterCondition(this.adminFilter.getUsername(), FIELD_USERNAME, junction);
    addStringFilterCondition(this.adminFilter.getLastname(), FIELD_LAST_NAME, junction);
    addStringFilterCondition(this.adminFilter.getEmail(), FIELD_EMAIL, junction);
    addObjectFilterCondition(this.adminFilter.getAgencyId(), FIELD_AGENCY_IDS, junction);

    return junction.isEmpty() ? this.queryBuilder.all().createQuery() : junction.createQuery();
  }
}
