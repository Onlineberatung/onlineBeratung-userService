package de.caritas.cob.userservice.api.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.plugin.core.SimplePluginRegistry;

@Configuration
public class SwaggerConfig {

  @Bean
  public LinkDiscoverers discoverers() {
    List<LinkDiscoverer> plugins = new ArrayList<>();
    plugins.add(new org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer());
    return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
  }
}
