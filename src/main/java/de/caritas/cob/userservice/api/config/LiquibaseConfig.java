package de.caritas.cob.userservice.api.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@Profile("!testing")
public class LiquibaseConfig {

  @Bean
  public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties liquibaseProperties) {

    var liquibase = new BeanAwareSpringLiquibase();
    liquibase.setContexts(liquibaseProperties.getContexts());
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
    liquibase.setClearCheckSums(liquibaseProperties.isClearChecksums());
    liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
    liquibase.setDataSource(dataSource);
    liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setLabels(liquibase.getLabels());
    liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
    liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
    liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
    liquibase.setShouldRun(liquibaseProperties.isEnabled());
    liquibase.setTag(liquibaseProperties.getTag());
    liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());

    return liquibase;
  }
}
