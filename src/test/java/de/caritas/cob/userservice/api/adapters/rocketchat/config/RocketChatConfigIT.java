package de.caritas.cob.userservice.api.adapters.rocketchat.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("testing")
class RocketChatConfigIT {

  @SpyBean private RocketChatConfig underTest;

  @Test
  void configurationShouldLoadProperties() {
    assertEquals("https://testing.com/api/v1", underTest.getBaseUrl());
    assertEquals("0 0 * * * ?", underTest.getCredentialCron());
    assertEquals(
        "mongodb://<USERNAME>:<PASSWORD>@mongodb:27017/rocketchat?retryWrites=false",
        underTest.getMongoUrl());
  }

  @Test
  void getApiUrlShouldReturnUrlWithPath() {
    var path = "/this/is/a/path";
    var url = underTest.getApiUrl(path);

    assertEquals("https://testing.com/api/v1" + path, url);
  }

  @Test
  void getApiUrlShouldReplacePathParams() {
    var value = "a-value";
    var path = "/this/is/a/path/{a-variable}/suffix";
    var url = underTest.getApiUrl(path, value);

    assertEquals("https://testing.com/api/v1/this/is/a/path/a-value/suffix", url);
  }

  @Test
  void getApiUrlShouldRemoveAdditionalSlashes() {
    var path = "//this/is//a/path/";
    var url = underTest.getApiUrl(path);

    assertEquals("https://testing.com/api/v1/this/is/a/path", url);
  }

  @Test
  void getApiUrlShouldReturnUrlWithQueryParams() {
    var path = "/this/is/a/path?a=1&b=2";
    var url = underTest.getApiUrl(path);

    assertEquals("https://testing.com/api/v1" + path, url);
  }
}
