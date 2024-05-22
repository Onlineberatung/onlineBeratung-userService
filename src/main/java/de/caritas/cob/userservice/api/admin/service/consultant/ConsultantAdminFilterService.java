package de.caritas.cob.userservice.api.admin.service.consultant;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.OrderEnum;
import de.caritas.cob.userservice.api.admin.service.consultant.querybuilder.ConsultantFilterQueryBuilder;
import de.caritas.cob.userservice.api.model.Consultant;
import javax.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Service;

/** Service class to provide filtered search for all {@link Consultant} entities. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantAdminFilterService {

  private final @NonNull EntityManagerFactory entityManagerFactory;

  /**
   * Searches for consultants by given {@link ConsultantFilter}, limits the result by perPage and
   * generates a {@link ConsultantSearchResultDTO} containing hal links.
   *
   * @param consultantFilter the filter object containing filter values
   * @param page the current requested page
   * @param perPage the amount of items in one page
   * @return the result list
   */
  public ConsultantSearchResultDTO findFilteredConsultants(
      final Integer page,
      final Integer perPage,
      final ConsultantFilter consultantFilter,
      final Sort sort) {
    var fullTextEntityManager =
        Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());
    triggerLuceneToBuildIndex(fullTextEntityManager);
    var fullTextQuery = buildFilteredQuery(consultantFilter, fullTextEntityManager);
    fullTextQuery.setMaxResults(Math.max(perPage, 1));
    fullTextQuery.setFirstResult(Math.max((page - 1) * perPage, 0));
    fullTextQuery.setSort(buildSort(sort));

    var searchResultDTO =
        ConsultantSearchResultBuilder.getInstance(fullTextQuery)
            .withFilter(consultantFilter)
            .withSort(sort)
            .withPage(page)
            .withPerPage(perPage)
            .buildSearchResult();

    fullTextEntityManager.close();
    return searchResultDTO;
  }

  private static void triggerLuceneToBuildIndex(FullTextEntityManager fullTextEntityManager) {
    try {
      fullTextEntityManager.createIndexer(Consultant.class).startAndWait();
    } catch (InterruptedException e) {
      log.info("Lucene index building was interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  protected FullTextQuery buildFilteredQuery(
      ConsultantFilter consultantFilter, FullTextEntityManager fullTextEntityManager) {

    var queryBuilder =
        fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Consultant.class)
            .get();

    var query =
        ConsultantFilterQueryBuilder.getInstance(queryBuilder)
            .onConsultantFilter(consultantFilter)
            .buildQuery();

    return fullTextEntityManager.createFullTextQuery(query, Consultant.class);
  }

  private org.apache.lucene.search.Sort buildSort(Sort sort) {
    var luceneSort = new org.apache.lucene.search.Sort();
    if (nonNull(sort) && nonNull(sort.getField())) {
      var reverse = OrderEnum.DESC.equals(sort.getOrder());
      luceneSort.setSort(
          SortField.FIELD_SCORE,
          new SortField(sort.getField().getValue(), SortField.Type.STRING, reverse));
    }

    return luceneSort;
  }
}
