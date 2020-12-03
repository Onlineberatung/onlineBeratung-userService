package de.caritas.cob.userservice.api.admin.service;

import de.caritas.cob.userservice.api.admin.querybuilder.ConsultantFilterQueryBuilder;
import de.caritas.cob.userservice.api.model.ConsultantDTO;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import de.caritas.cob.userservice.api.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;

/**
 * Service class to provide filtered search for all {@link Consultant} entities.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAdminFilterService {

  private final @NonNull EntityManagerFactory entityManagerFactory;

  /**
   * Searches for consultants by given {@link ConsultantFilter}, limits the result by perPage and
   * generates a {@link ConsultantSearchResultDTO} containing hal links.
   *
   * @param consultantFilter the filter object containing filter values
   * @param page    the current requested page
   * @param perPage the amount of items in one page
   * @return the result list
   */
  public ConsultantSearchResultDTO findFilteredConsultants(final Integer page,
      final Integer perPage, final ConsultantFilter consultantFilter) {
    FullTextEntityManager fullTextEntityManager = Search
        .getFullTextEntityManager(entityManagerFactory.createEntityManager());

    FullTextQuery fullTextQuery = buildFilteredQuery(consultantFilter, fullTextEntityManager);
    fullTextQuery.setMaxResults(Math.max(perPage, 1));
    fullTextQuery.setFirstResult(Math.max((page - 1) * perPage, 0));

    Stream<Consultant> resultStream = fullTextQuery.getResultStream();
    List<ConsultantDTO> embedded = resultStream
        .map(this::fromConsultant)
        .collect(Collectors.toList());

    fullTextEntityManager.close();
    return new ConsultantSearchResultDTO()
        .embedded(embedded);
  }

  private FullTextQuery buildFilteredQuery(ConsultantFilter consultantFilter,
      FullTextEntityManager fullTextEntityManager) {

    QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
        .buildQueryBuilder()
        .forEntity(Consultant.class)
        .get();

    Query query = ConsultantFilterQueryBuilder.getInstance(queryBuilder)
        .onConsultantFilter(consultantFilter)
        .buildQuery();

    return fullTextEntityManager.createFullTextQuery(query, Consultant.class);
  }

  private ConsultantDTO fromConsultant(Consultant consultant) {
    return new ConsultantDTO()
        .id(consultant.getId())
        .username(consultant.getUsername())
        .firstname(consultant.getFirstName())
        .lastname(consultant.getLastName())
        .email(consultant.getEmail())
        .formalLanguage(consultant.isLanguageFormal())
        .teamConsultant(consultant.isTeamConsultant())
        .absent(consultant.isAbsent())
        .absenceMessage(consultant.getAbsenceMessage())
        .createDate(String.valueOf(consultant.getCreateDate()))
        .updateDate(String.valueOf(consultant.getUpdateDate()))
        .deleteDate(String.valueOf(consultant.getDeleteDate()));
  }

}
