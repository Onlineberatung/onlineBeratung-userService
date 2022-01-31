package de.caritas.cob.userservice.api.config.apiclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class MessageServiceApiClientConfigIT {

  @Autowired
  private MessageControllerApi messageControllerApi;

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  @Test
  public void configureMessageControllerApi_Should_setCorrectApiUrl() {
    String apiClientUrl = this.messageControllerApi.getApiClient().getBasePath();

    assertThat(apiClientUrl, is(this.messageServiceApiUrl));
  }
}
