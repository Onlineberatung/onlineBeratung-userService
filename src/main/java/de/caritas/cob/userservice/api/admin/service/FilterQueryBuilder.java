package de.caritas.cob.userservice.api.admin.service;

import static java.util.Objects.nonNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

public abstract class FilterQueryBuilder {
  protected final QueryBuilder queryBuilder;

  protected FilterQueryBuilder(QueryBuilder queryBuilder) {
    this.queryBuilder = queryBuilder;
  }

  public abstract Query buildQuery();

  protected abstract Query buildFilteredQuery();

  protected void addStringFilterCondition(
      String filterValue, String targetField, BooleanJunction<BooleanJunction> junction) {
    if (StringUtils.isNotBlank(filterValue)) {
      addFilterCondition(filterValue, targetField, junction);
    }
  }

  protected void addFilterCondition(
      Object filterValue, String targetField, BooleanJunction<BooleanJunction> junction) {
    junction.must(
        this.queryBuilder.keyword().onField(targetField).matching(filterValue).createQuery());
  }

  protected void addObjectFilterCondition(
      Object filterValue, String targetField, BooleanJunction<BooleanJunction> junction) {
    if (nonNull(filterValue)) {
      addFilterCondition(filterValue, targetField, junction);
    }
  }
}
