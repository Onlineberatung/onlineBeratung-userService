package de.caritas.cob.userservice.api.config;

import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantReindexer;
import java.time.Clock;
import javax.persistence.EntityManagerFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

/** Contains some general spring boot application configurations */
@Configuration
@ComponentScan(basePackages = {"de.caritas.cob.userservice"})
@PropertySources({@PropertySource("classpath:messages.properties")})
public class AppConfig {

  /**
   * Activate the messages.properties for validation messages
   *
   * @param messageSource
   * @return
   */
  @Bean
  public LocalValidatorFactoryBean validator(MessageSource messageSource) {
    LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
    validatorFactoryBean.setValidationMessageSource(messageSource);
    return validatorFactoryBean;
  }

  // RestTemplate Bean
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  /**
   * Builds an indexer for hibernate search.
   *
   * @param entityManagerFactory the manager factory bean
   * @return an {@link AgencyReindexer} used to reindex entities
   */
  @Bean
  public ConsultantReindexer consultantReindexer(EntityManagerFactory entityManagerFactory) {
    FullTextEntityManager manager =
        Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());
    return new ConsultantReindexer(manager);
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
