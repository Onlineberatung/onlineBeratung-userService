package de.caritas.cob.userservice.api.admin.service;

import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.http.ResponseEntity;

public abstract class SearchResultBuilder<FILTER, SEARCH_RESULT_DTO> implements HalLinkBuilder {
  protected final FullTextQuery fullTextQuery;
  protected FILTER filter;

  protected SEARCH_RESULT_DTO searchResultDto;
  protected Sort sort;
  protected Integer page;
  protected Integer perPage;

  protected SearchResultBuilder(FullTextQuery fullTextQuery) {
    this.fullTextQuery = fullTextQuery;
  }

  public SearchResultBuilder<FILTER, SEARCH_RESULT_DTO> withFilter(FILTER filter) {
    this.filter = filter;
    return this;
  }

  public SearchResultBuilder<FILTER, SEARCH_RESULT_DTO> withSort(Sort sort) {
    this.sort = sort;
    return this;
  }

  public SearchResultBuilder<FILTER, SEARCH_RESULT_DTO> withPage(Integer page) {
    this.page = page;
    return this;
  }

  public SearchResultBuilder<FILTER, SEARCH_RESULT_DTO> withPerPage(Integer perPage) {
    this.perPage = perPage;
    return this;
  }

  public abstract SEARCH_RESULT_DTO buildSearchResult();

  protected HalLink buildSelfLink(final ResponseEntity<SEARCH_RESULT_DTO> responseEntity) {
    return buildHalLinkForParams(responseEntity);
  }

  protected HalLink buildHalLinkForParams(final ResponseEntity<SEARCH_RESULT_DTO> responseEntity) {
    return buildHalLink(responseEntity, HalLink.MethodEnum.GET);
  }

  protected HalLink buildNextLink(final ResponseEntity<SEARCH_RESULT_DTO> results) {
    if (hasNextPage()) {
      return buildHalLinkForParams(results);
    }
    return null;
  }

  protected boolean hasNextPage() {
    return this.fullTextQuery.getResultSize() > this.page * this.perPage;
  }

  protected HalLink buildPreviousLink(final ResponseEntity<SEARCH_RESULT_DTO> results) {
    if (this.page > 1) {
      return buildHalLinkForParams(results);
    }
    return null;
  }
}
