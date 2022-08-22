package de.caritas.cob.userservice.api.admin.service.consultant;

import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.springframework.beans.factory.InitializingBean;

/** Reindexer class for all indexed entities. */
@RequiredArgsConstructor
public class ConsultantReindexer implements InitializingBean {

  private final @NonNull FullTextEntityManager fullTextEntityManager;

  /** Indexes all entity fields marked as indexed on startup. */
  @Override
  public void afterPropertiesSet() throws Exception {
    LogService.logInfo("Going to index entities");
    this.fullTextEntityManager.createIndexer().startAndWait();
  }
}
