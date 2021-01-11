package de.caritas.cob.userservice.api.admin.service;

import de.caritas.cob.userservice.api.admin.hallink.ConsultingTypePaginationLinksBuilder;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.ConsultingTypeAdminResultDTO;
import de.caritas.cob.userservice.api.model.ConsultingTypeResultDTO;
import de.caritas.cob.userservice.api.model.PaginationLinks;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;

/**
 * Service class to handle administrative operations on consulting types.
 */
@Service
@RequiredArgsConstructor
public class ConsultingTypeAdminService {

  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Returns all dioceses within the given page and perPage offsets.
   *
   * @param page    Number of page where to start in the query (1 = first page) (required) * @param
   * @param perPage Number of items which are being returned per page (required)
   * @return {@link ConsultingTypeAdminResultDTO}
   */
  public ConsultingTypeAdminResultDTO findConsultingTypes(Integer page, Integer perPage) {
    PagedListHolder<ConsultingTypeResultDTO> pagedListHolder = new PagedListHolder<>(
        fullSortedConsultingTypeResponseList());
    pagedListHolder.setPageSize(Math.max(perPage, 1));
    pagedListHolder.setPage(currentPage(page, pagedListHolder));

    return new ConsultingTypeAdminResultDTO()
        .embedded(page > pagedListHolder.getPageCount() ? Collections.emptyList()
            : pagedListHolder.getPageList())
        .links(buildPaginationLinks(page, perPage, pagedListHolder));
  }

  private List<ConsultingTypeResultDTO> fullSortedConsultingTypeResponseList() {
    return consultingTypeManager.getConsultingTypeSettingsMap().values().stream().sorted(
        Comparator.comparing(ConsultingTypeSettings::getConsultingType, Comparator.comparing(
            ConsultingType::getUrlName)))
        .map(this::fromConsultingTypeSettings)
        .collect(Collectors.toList());
  }

  private Integer currentPage(Integer page,
      PagedListHolder<ConsultingTypeResultDTO> pagedListHolder) {
    return Math.max(page < pagedListHolder.getPageCount() ? page - 1 : page, 0);
  }

  private ConsultingTypeResultDTO fromConsultingTypeSettings(ConsultingTypeSettings ctSettings) {
    return new ConsultingTypeResultDTO()
        .consultingTypeId(ctSettings.getConsultingType().getValue())
        .name(ctSettings.getConsultingType().getUrlName())
        .languageFormal(ctSettings.isLanguageFormal())
        .roles(ctSettings.getRoles())
        .sendWelcomeMessage(ctSettings.isSendWelcomeMessage())
        .welcomeMessage(ctSettings.getWelcomeMessage())
        .monitoring(ctSettings.isMonitoring())
        .feedbackChat(ctSettings.isFeedbackChat())
        .notifications(ctSettings.getNotifications());
  }

  private PaginationLinks buildPaginationLinks(Integer page, Integer perPage,
      PagedListHolder<ConsultingTypeResultDTO> pagedListHolder) {
    return ConsultingTypePaginationLinksBuilder
        .getInstance()
        .withPage(page)
        .withPerPage(perPage)
        .withPagedListHolder(pagedListHolder)
        .buildPaginationLinks();
  }
}
