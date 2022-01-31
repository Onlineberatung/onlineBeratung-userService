package de.caritas.cob.userservice.api.config;

import java.util.Arrays;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "csrf")
public class CsrfSecurityProperties {

  private Whitelist whitelist;
  private ConfigProperty cookie;
  private ConfigProperty header;

  @Getter
  @Setter
  public static class ConfigProperty {

    private String property;

    @Override
    public String toString() {
      return new StringJoiner(", ", ConfigProperty.class.getSimpleName() + "[", "]")
          .add("property='" + property + "'")
          .toString();
    }
  }

  @Setter
  @Getter
  public static class Whitelist {

    private ConfigProperty header;
    private String[] adminUris;
    private String[] configUris;

    public Whitelist() {
      this.adminUris = new String[0];
      this.configUris = new String[0];
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", Whitelist.class.getSimpleName() + "[", "]")
          .add("\nheader=" + header)
          .add("\nadminUris=" + Arrays.toString(adminUris))
          .add("\nconfigUris=" + Arrays.toString(configUris))
          .toString();
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CsrfSecurityProperties.class.getSimpleName() + "[", "]")
        .add("\nwhitelist=" + whitelist)
        .add("\ncookie=" + cookie)
        .add("\nheader=" + header)
        .toString();
  }
}
