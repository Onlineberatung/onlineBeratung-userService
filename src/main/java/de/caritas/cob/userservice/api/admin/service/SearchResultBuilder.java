package de.caritas.cob.userservice.api.admin.service;

import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.http.ResponseEntity;

public abstract class SearchResultBuilder<F, S> implements HalLinkBuilder {

  protected final FullTextQuery fullTextQuery;
  protected F filter;

  protected S searchResultDto;
  protected Sort sort;
  protected Integer page;
  protected Integer perPage;

  protected SearchResultBuilder(FullTextQuery fullTextQuery) {
    this.fullTextQuery = fullTextQuery;
  }

  public SearchResultBuilder<F, S> withFilter(F filter) {
    this.filter = filter;
    return this;
  }

  public SearchResultBuilder<F, S> withSort(Sort sort) {
    this.sort = sort;
    return this;
  }

  public SearchResultBuilder<F, S> withPage(Integer page) {
    this.page = page;
    return this;
  }

  public SearchResultBuilder<F, S> withPerPage(Integer perPage) {
    this.perPage = perPage;
    return this;
  }

  public abstract S buildSearchResult();

  protected HalLink buildSelfLink(final ResponseEntity<S> responseEntity) {
    return buildHalLinkForParams(responseEntity);
  }

  protected HalLink buildHalLinkForParams(final ResponseEntity<S> responseEntity) {
    return buildHalLink(responseEntity, HalLink.MethodEnum.GET);
  }

  protected HalLink buildNextLink(final ResponseEntity<S> results) {
    if (hasNextPage()) {
      return buildHalLinkForParams(results);
    }
    return null;
  }

  protected boolean hasNextPage() {
    return this.fullTextQuery.getResultSize() > this.page * this.perPage;
  }

  protected HalLink buildPreviousLink(final ResponseEntity<S> results) {
    if (this.page > 1) {
      return buildHalLinkForParams(results);
    }
    return null;
  }
}
