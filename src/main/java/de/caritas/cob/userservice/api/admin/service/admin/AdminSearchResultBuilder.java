package de.caritas.cob.userservice.api.admin.service.admin;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.PaginationLinks;
import de.caritas.cob.userservice.api.admin.service.SearchResultBuilder;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.search.jpa.FullTextQuery;

public class AdminSearchResultBuilder
    extends SearchResultBuilder<AdminFilter, AdminSearchResultDTO> {
  private AdminSearchResultBuilder(FullTextQuery fullTextQuery) {
    super(fullTextQuery);
  }

  public static AdminSearchResultBuilder getInstance(FullTextQuery fullTextQuery) {
    return new AdminSearchResultBuilder(fullTextQuery);
  }

  public AdminSearchResultDTO buildSearchResult() {
    Stream<Admin> resultStream = fullTextQuery.getResultStream();
    var resultList =
        resultStream
            .map(AdminResponseDTOBuilder::getInstance)
            .map(AdminResponseDTOBuilder::buildAgencyAdminResponseDTO)
            .collect(Collectors.toList());

    var paginationLinks =
        new PaginationLinks()
            .self(buildSelfLink())
            .next(buildNextLink())
            .previous(buildPreviousLink());

    return new AdminSearchResultDTO()
        .embedded(resultList)
        .links(paginationLinks)
        .total(fullTextQuery.getResultSize());
  }

  private HalLink buildSelfLink() {
    return super.buildSelfLink(
        methodOn(UseradminApi.class).getAgencyAdmins(page, perPage, filter, sort));
  }

  private HalLink buildNextLink() {
    return super.buildNextLink(
        methodOn(UseradminApi.class).getAgencyAdmins(page + 1, perPage, filter, sort));
  }

  private HalLink buildPreviousLink() {
    return buildPreviousLink(
        methodOn(UseradminApi.class).getAgencyAdmins(page - 1, perPage, filter, sort));
  }
}
