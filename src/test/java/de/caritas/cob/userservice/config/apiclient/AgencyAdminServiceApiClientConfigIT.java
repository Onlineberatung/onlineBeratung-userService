package de.caritas.cob.userservice.config.apiclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
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
public class AgencyAdminServiceApiClientConfigIT {

  @Autowired
  private AdminAgencyControllerApi adminAgencyControllerApi;

  @Value("${agency.admin.service.api.url}")
  private String adminAgencyApiBaseUrl;

  @Test
  public void configureLiveControllerApi_Should_setCorrectApiUrl() {
    String apiClientUrl = this.adminAgencyControllerApi.getApiClient().getBasePath();

    assertThat(apiClientUrl, is(this.adminAgencyApiBaseUrl));
  }

}
