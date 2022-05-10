package de.caritas.cob.userservice.api.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;

@NoArgsConstructor
public class BeanAwareSpringLiquibase extends SpringLiquibase {

  private static ResourceLoader applicationContext;

  public static <T> T getBean(Class<T> beanClass) throws Exception {
    if (applicationContext instanceof ApplicationContext) {
      return ((ApplicationContext) applicationContext).getBean(beanClass);
    } else {
      throw new Exception("Resource loader is not an instance of ApplicationContext");
    }
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    super.setResourceLoader(resourceLoader);
    applicationContext = resourceLoader;
  }
}
