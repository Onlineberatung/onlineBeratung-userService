package de.caritas.cob.userservice.api.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;

@NoArgsConstructor
public class BeanAwareSpringLiquibase extends SpringLiquibase {

  private static ResourceLoader applicationContext;

  public static <T> T getBean(Class<T> beanClass) throws InstantiationException {
    if (applicationContext instanceof ApplicationContext) {
      return ((ApplicationContext) applicationContext).getBean(beanClass);
    } else {
      throw new InstantiationException("Resource loader is not an instance of ApplicationContext");
    }
  }

  @Override
  @SuppressWarnings("java:S2696") // should not write to "static" fields
  public void setResourceLoader(ResourceLoader resourceLoader) {
    super.setResourceLoader(resourceLoader);
    applicationContext = resourceLoader;
  }
}
