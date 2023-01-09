package de.caritas.cob.userservice.api.admin.service.admin.search;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.FieldEnum;
import de.caritas.cob.userservice.api.admin.service.admin.AdminSearchResultBuilder;
import de.caritas.cob.userservice.api.admin.service.admin.search.querybuilder.AdminFilterQueryBuilder;
import de.caritas.cob.userservice.api.model.Admin;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFilterService {

  private final @NonNull EntityManagerFactory entityManagerFactory;

  public AdminSearchResultDTO findFilteredAdmins(
      final Integer page, final Integer perPage, final AdminFilter adminFilter, Sort sort) {
    var fullTextEntityManager =
        Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());

    sort = getValidSorter(sort);
    var fullTextQuery = buildFilteredQuery(adminFilter, fullTextEntityManager);
    fullTextQuery.setMaxResults(Math.max(perPage, 1));
    fullTextQuery.setFirstResult(Math.max((page - 1) * perPage, 0));
    fullTextQuery.setSort(buildSort(sort));

    var searchResultDTO =
        AdminSearchResultBuilder.getInstance(fullTextQuery)
            .withFilter(adminFilter)
            .withSort(sort)
            .withPage(page)
            .withPerPage(perPage)
            .buildSearchResult();

    fullTextEntityManager.close();
    return searchResultDTO;
  }

  protected FullTextQuery buildFilteredQuery(
      AdminFilter adminFilter, FullTextEntityManager fullTextEntityManager) {

    var queryBuilder =
        fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Admin.class).get();

    var query =
        AdminFilterQueryBuilder.getInstance(queryBuilder).onAdminFilter(adminFilter).buildQuery();

    return fullTextEntityManager.createFullTextQuery(query, Admin.class);
  }

  private org.apache.lucene.search.Sort buildSort(Sort sort) {
    var luceneSort = new org.apache.lucene.search.Sort();
    if (nonNull(sort) && nonNull(sort.getField())) {
      var reverse = Sort.OrderEnum.DESC.equals(sort.getOrder());
      luceneSort.setSort(
          SortField.FIELD_SCORE,
          new SortField(sort.getField().getValue(), SortField.Type.STRING, reverse));
    }

    return luceneSort;
  }

  private Sort getValidSorter(Sort sort) {
    if (sort == null
        || Stream.of(Sort.FieldEnum.values()).noneMatch(providedSortFieldIgnoringCase(sort))) {
      sort = new Sort();
      sort.setField(Sort.FieldEnum.LASTNAME);
      sort.setOrder(Sort.OrderEnum.ASC);
    }
    return sort;
  }

  private Predicate<FieldEnum> providedSortFieldIgnoringCase(Sort sort) {
    return field -> {
      if (nonNull(sort.getField())) {
        return field.getValue().equalsIgnoreCase(sort.getField().getValue());
      }
      return false;
    };
  }
}
