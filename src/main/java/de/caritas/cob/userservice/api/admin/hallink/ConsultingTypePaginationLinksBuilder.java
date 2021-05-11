package de.caritas.cob.userservice.api.admin.hallink;

import static de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder.DEFAULT_PAGE;
import static de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder.DEFAULT_PER_PAGE;
import static java.util.Objects.nonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.model.ConsultingTypeResultDTO;
import de.caritas.cob.userservice.api.model.HalLink;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.model.PaginationLinks;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;
import org.springframework.beans.support.PagedListHolder;

public class ConsultingTypePaginationLinksBuilder implements HalLinkBuilder {

  private Integer page;
  private Integer perPage;
  private PagedListHolder<ConsultingTypeResultDTO> pagedListHolder;

  private ConsultingTypePaginationLinksBuilder() {
  }

  /**
   * Creates a {@link ConsultingTypePaginationLinksBuilder} instance.
   *
   * @return an instance of {@link ConsultingTypePaginationLinksBuilder}
   */
  public static ConsultingTypePaginationLinksBuilder getInstance() {
    return new ConsultingTypePaginationLinksBuilder();
  }

  /**
   * Sets the page param.
   *
   * @param page the page value for building links
   * @return the current {@link ConsultingTypePaginationLinksBuilder}
   */
  public ConsultingTypePaginationLinksBuilder withPage(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * Sets the perPage param.
   *
   * @param perPage the amount value of results per page for building links
   * @return the current {@link ConsultingTypePaginationLinksBuilder}
   */
  public ConsultingTypePaginationLinksBuilder withPerPage(Integer perPage) {
    this.perPage = perPage;
    return this;
  }

  /**
   * Sets the {@link PagedListHolder}.
   *
   * @param pagedListHolder {@link PagedListHolder}
   * @return the current {@link ConsultingTypePaginationLinksBuilder}
   */
  public ConsultingTypePaginationLinksBuilder withPagedListHolder(
      PagedListHolder<ConsultingTypeResultDTO> pagedListHolder) {
    this.pagedListHolder = pagedListHolder;
    return this;
  }
}