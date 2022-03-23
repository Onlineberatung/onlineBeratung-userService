package de.caritas.cob.userservice.api.config.apiclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class LiveEventNotificationServiceApiClientConfigIT {

  @Autowired
  private LiveControllerApi liveControllerApi;

  @MockBean
  private LinkDiscoverers linkDiscoverers;

  @Value("${live.service.api.url}")
  private String liveServiceApiUrl;

  @Test
  public void configureLiveControllerApi_Should_setCorrectApiUrl() {
    String apiClientUrl = this.liveControllerApi.getApiClient().getBasePath();

    assertThat(apiClientUrl, is(this.liveServiceApiUrl));
  }

}

