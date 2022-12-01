package de.caritas.cob.userservice.api.admin.service.consultant.querybuilder;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.admin.service.FilterQueryBuilder;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

/** Builder for the filter based query used by hibernate search. */
public class ConsultantFilterQueryBuilder extends FilterQueryBuilder {

  private static final String FIELD_USERNAME = "username";
  private static final String FIELD_LAST_NAME = "lastName";
  private static final String FIELD_EMAIL = "email";
  private static final String FIELD_ABSENT = "absent";
  private static final String FIELD_AGENCY_IDS = "consultantAgencies.agencyId";

  private ConsultantFilter consultantFilter;

  private ConsultantFilterQueryBuilder(QueryBuilder queryBuilder) {
    super(queryBuilder);
  }

  /**
   * Creates the {@link ConsultantFilterQueryBuilder} instance.
   *
   * @param queryBuilder the query builder to build the filters on
   * @return the {@link ConsultantFilterQueryBuilder} instance
   */
  public static ConsultantFilterQueryBuilder getInstance(QueryBuilder queryBuilder) {
    return new ConsultantFilterQueryBuilder(queryBuilder);
  }

  /**
   * Sets the {@link ConsultantFilter} object to be included in the query.
   *
   * @param consultantFilter the {@link ConsultantFilter}
   * @return the current {@link ConsultantFilterQueryBuilder}
   */
  public ConsultantFilterQueryBuilder onConsultantFilter(ConsultantFilter consultantFilter) {
    this.consultantFilter = consultantFilter;
    return this;
  }

  /**
   * Builds the filter query for hibernate search. If no filter is set in {@link ConsultantFilter},
   * a unfiltered query will be returned.
   *
   * @return the created {@link Query}
   */
  public Query buildQuery() {
    return nonNull(this.consultantFilter)
        ? buildFilteredQuery()
        : this.queryBuilder.all().createQuery();
  }

  protected Query buildFilteredQuery() {
    BooleanJunction<BooleanJunction> junction = this.queryBuilder.bool();

    addStringFilterCondition(this.consultantFilter.getUsername(), FIELD_USERNAME, junction);
    addStringFilterCondition(this.consultantFilter.getLastname(), FIELD_LAST_NAME, junction);
    addStringFilterCondition(this.consultantFilter.getEmail(), FIELD_EMAIL, junction);
    addObjectFilterCondition(this.consultantFilter.getAbsent(), FIELD_ABSENT, junction);
    addObjectFilterCondition(this.consultantFilter.getAgencyId(), FIELD_AGENCY_IDS, junction);

    return junction.isEmpty() ? this.queryBuilder.all().createQuery() : junction.createQuery();
  }
}
