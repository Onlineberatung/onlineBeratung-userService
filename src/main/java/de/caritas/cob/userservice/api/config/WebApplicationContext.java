package de.caritas.cob.userservice.api.config;

import de.caritas.cob.userservice.api.adapters.web.controller.validation.MinValueValidator;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for additional web mvc handlings.
 */
@Component
public class WebApplicationContext implements WebMvcConfigurer {

  /**
   * Provides access to add custom method argument resolvers.
   *
   * @param argumentResolvers springs argument resolvers
   */
  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new MinValueValidator());
  }
}
