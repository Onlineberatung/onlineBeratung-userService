package de.caritas.cob.userservice.api.adapters.rocketchat.config;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import java.util.Arrays;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "rocket-chat")
public class RocketChatConfig {

  private static final String CONTENT_TYPE = "Content-Type";

  private static final char PATH_SEPARATOR = '/';

  @URL
  private String baseUrl;

  @Bean("rocketChatRestTemplate")
  public RestTemplate rocketChatRestTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder
        .defaultHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
        .build();
  }

  public String getApiUrl(String path) {
    return getApiUrl(path, "");
  }

  public String getApiUrl(String path, String arg) {
    var queryParams = fromUriString(baseUrl + path).build().getQueryParams();
    if (!queryParams.isEmpty()) {
      path = path.substring(0, path.indexOf("?"));
    }

    var builder = fromUriString(baseUrl);
    Arrays.stream(StringUtils
        .trimLeadingCharacter(path, PATH_SEPARATOR)
        .replaceAll("(\\{).*(})", arg)
        .split(Character.toString(PATH_SEPARATOR))).forEach(builder::pathSegment);
    queryParams.forEach(builder::queryParam);

    return builder.toUriString();
  }
}
