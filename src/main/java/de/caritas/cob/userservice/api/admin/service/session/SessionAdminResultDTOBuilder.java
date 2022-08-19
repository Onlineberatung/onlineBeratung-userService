package de.caritas.cob.userservice.api.admin.service.session;

import static de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder.DEFAULT_PAGE;
import static de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder.DEFAULT_PER_PAGE;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.controller.UserAdminController;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.PaginationLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.model.Session;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

/** Builder to create a {@link SessionAdminResultDTO}. */
public class SessionAdminResultDTOBuilder implements HalLinkBuilder {

  private SessionFilter sessionFilter;
  private Integer page;
  private Integer perPage;
  private Page<Session> resultPage;

  private SessionAdminResultDTOBuilder() {}

  /**
   * Creates an {@link SessionAdminResultDTOBuilder} instance.
   *
   * @return a instance of {@link SessionAdminResultDTOBuilder}
   */
  public static SessionAdminResultDTOBuilder getInstance() {
    return new SessionAdminResultDTOBuilder();
  }

  /**
   * Sets the filter param.
   *
   * @param sessionFilter the filter object for building links
   * @return the current {@link SessionAdminResultDTOBuilder}
   */
  public SessionAdminResultDTOBuilder withFilter(SessionFilter sessionFilter) {
    this.sessionFilter = sessionFilter;
    return this;
  }

  /**
   * Sets the page param.
   *
   * @param page the page value for building links
   * @return the current {@link SessionAdminResultDTOBuilder}
   */
  public SessionAdminResultDTOBuilder withPage(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * Sets the perPage param.
   *
   * @param perPage the amount value of results per page for building links
   * @return the current {@link SessionAdminResultDTOBuilder}
   */
  public SessionAdminResultDTOBuilder withPerPage(Integer perPage) {
    this.perPage = perPage;
    return this;
  }

  /**
   * Sets the resultPage param.
   *
   * @param resultPage the repository result page object for building links and embedded objects
   * @return the current {@link SessionAdminResultDTOBuilder}
   */
  public SessionAdminResultDTOBuilder withResultPage(Page<Session> resultPage) {
    this.resultPage = resultPage;
    return this;
  }

  /**
   * Creates the {@link SessionAdminResultDTO}.
   *
   * @return the generated {@link SessionAdminResultDTO}
   */
  public SessionAdminResultDTO build() {
    ensureNonNullPaginationParams();
    return new SessionAdminResultDTO()
        .embedded(buildSessionAdminResult())
        .links(buildResultLinks())
        .total(buildTotal());
  }

  private void ensureNonNullPaginationParams() {
    this.page = nonNull(this.page) ? this.page : DEFAULT_PAGE;
    this.perPage = nonNull(this.perPage) ? this.perPage : DEFAULT_PER_PAGE;
  }

  private List<SessionAdminDTO> buildSessionAdminResult() {
    if (isNull(this.resultPage)) {
      return emptyList();
    }
    return this.resultPage.get().map(this::fromSession).collect(Collectors.toList());
  }

  private SessionAdminDTO fromSession(Session session) {
    return new SessionAdminDTO()
        .id(session.getId())
        .agencyId(session.getAgencyId().intValue())
        .consultantId(nonNull(session.getConsultant()) ? session.getConsultant().getId() : null)
        .consultingType(session.getConsultingTypeId())
        .email(session.getUser().getEmail())
        .postcode(session.getPostcode())
        .userId(session.getUser().getUserId())
        .username(session.getUser().getUsername())
        .isTeamSession(session.isTeamSession())
        .messageDate(String.valueOf(session.getEnquiryMessageDate()))
        .createDate(String.valueOf(session.getCreateDate()))
        .updateDate(String.valueOf(session.getUpdateDate()));
  }

  private PaginationLinks buildResultLinks() {
    return new PaginationLinks()
        .self(buildSelfLink())
        .next(buildNextLink())
        .previous(buildPreviousLink());
  }

  private HalLink buildSelfLink() {
    return buildHalLinkForParams(this.page, this.perPage, this.sessionFilter);
  }

  private HalLink buildHalLinkForParams(
      Integer page, Integer perPage, SessionFilter sessionFilter) {
    return buildHalLink(
        methodOn(UserAdminController.class).getSessions(page, perPage, sessionFilter),
        MethodEnum.GET);
  }

  private HalLink buildNextLink() {
    return hasNextPage()
        ? buildHalLinkForParams(this.page + 1, this.perPage, this.sessionFilter)
        : null;
  }

  private boolean hasNextPage() {
    return nonNull(this.resultPage) && this.resultPage.getTotalPages() > this.page;
  }

  private HalLink buildPreviousLink() {
    return hasPreviousPage()
        ? buildHalLinkForParams(this.page - 1, this.perPage, this.sessionFilter)
        : null;
  }

  private boolean hasPreviousPage() {
    return this.page > 1;
  }

  private int buildTotal() {
    return nonNull(this.resultPage) ? (int) this.resultPage.getTotalElements() : 0;
  }
}
