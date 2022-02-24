package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.admin.model.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.admin.model.ConsultantDTO;
import de.caritas.cob.userservice.api.admin.model.ConsultantFilter;
import de.caritas.cob.userservice.api.admin.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.admin.model.HalLink;
import de.caritas.cob.userservice.api.admin.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.admin.model.PaginationLinks;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.search.jpa.FullTextQuery;

/**
 * Builder class to generate a {@link ConsultantSearchResultDTO} containing available hal links and
 * result of {@link ConsultantDTO} elements.
 */
public class ConsultantSearchResultBuilder implements HalLinkBuilder {

  private final FullTextQuery fullTextQuery;
  private ConsultantFilter consultantFilter;
  private Integer page;
  private Integer perPage;

  private ConsultantSearchResultBuilder(FullTextQuery fullTextQuery) {
    this.fullTextQuery = fullTextQuery;
  }

  /**
   * Creates the {@link ConsultantSearchResultBuilder} instance.
   *
   * @param fullTextQuery mandatory filtered search query for result extraction
   * @return a instance of {@link ConsultantSearchResultBuilder}
   */
  public static ConsultantSearchResultBuilder getInstance(FullTextQuery fullTextQuery) {
    return new ConsultantSearchResultBuilder(fullTextQuery);
  }

  /**
   * Sets the filter param.
   *
   * @param consultantFilter the filter value for building links
   * @return the current {@link ConsultantSearchResultBuilder}
   */
  public ConsultantSearchResultBuilder withFilter(ConsultantFilter consultantFilter) {
    this.consultantFilter = consultantFilter;
    return this;
  }

  /**
   * Sets the page param.
   *
   * @param page the page value for building links
   * @return the current {@link ConsultantSearchResultBuilder}
   */
  public ConsultantSearchResultBuilder withPage(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * Sets the perPage param.
   *
   * @param perPage the amount value of results per page for building links
   * @return the current {@link ConsultantSearchResultBuilder}
   */
  public ConsultantSearchResultBuilder withPerPage(Integer perPage) {
    this.perPage = perPage;
    return this;
  }

  /**
   * Generates the {@link ConsultantSearchResultDTO} containing all results and navigation hal
   * links.
   *
   * @return the generated {@link ConsultantSearchResultDTO}
   */
  public ConsultantSearchResultDTO buildConsultantSearchResult() {
    Stream<Consultant> resultStream = fullTextQuery.getResultStream();
    List<ConsultantAdminResponseDTO> resultList = resultStream
        .map(ConsultantResponseDTOBuilder::getInstance)
        .map(ConsultantResponseDTOBuilder::buildResponseDTO)
        .collect(Collectors.toList());

    PaginationLinks paginationLinks = new PaginationLinks()
        .self(buildSelfLink())
        .next(buildNextLink())
        .previous(buildPreviousLink());

    return new ConsultantSearchResultDTO()
        .embedded(resultList)
        .links(paginationLinks)
        .total(fullTextQuery.getResultSize());
  }

  private HalLink buildSelfLink() {
    return buildHalLinkForParams(this.page, this.perPage);
  }

  private HalLink buildHalLinkForParams(Integer page, Integer perPage) {
    return buildHalLink(
        methodOn(UseradminApi.class).getConsultants(page, perPage, this.consultantFilter),
        MethodEnum.GET);
  }

  private HalLink buildNextLink() {
    if (hasNextPage()) {
      return buildHalLinkForParams(this.page + 1, this.perPage);
    }
    return null;
  }

  private boolean hasNextPage() {
    return this.fullTextQuery.getResultSize() > this.page * this.perPage;
  }

  private HalLink buildPreviousLink() {
    if (this.page > 1) {
      return buildHalLinkForParams(this.page - 1, this.perPage);
    }
    return null;
  }

}
