package de.caritas.cob.userservice.api.helper;

import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RunWith(MockitoJUnitRunner.class)
public class RequestHelperTest {

  private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
  private static final String HEADER_BEARER_KEY = "Bearer ";
  private static final String BEARER_TOKEN = "token";

  @Test
  public void getFormHttpHeaders_Should_Return_HttpHeaders_With_ContentType_ApplicationFormUrlencoded() {
    var httpHeaders = RequestHelper.getFormHttpHeaders();
    var controlHttpHeaders = new HttpHeaders();
    controlHttpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    Assertions.assertEquals(controlHttpHeaders, httpHeaders);
    Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED, httpHeaders.getContentType());
  }

  @Test
  public void getAuthorizedFormHttpHeaders_Should_Return_HttpHeader_With_BearerToken() {
    var httpHeaders = RequestHelper.getAuthorizedHttpHeaders(BEARER_TOKEN,
        MediaType.APPLICATION_FORM_URLENCODED);

    Assertions.assertTrue(httpHeaders.containsKey(HEADER_AUTHORIZATION_KEY));
    Assertions.assertTrue(httpHeaders.containsValue(List.of(HEADER_BEARER_KEY + BEARER_TOKEN)));
    Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED, httpHeaders.getContentType());
  }

}
