package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GENDER_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RELATION_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.TENANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationStatisticsEventTest {

  private RegistrationStatisticsEvent registrationStatisticsEvent;

  @Before
  public void setUp() {
    UserDTO registeredUser =
        UserDTO.builder()
            .age(AGE)
            .userGender(GENDER_VALUE)
            .counsellingRelation(RELATION_VALUE)
            .postcode(POSTCODE)
            .build();
    User createdUser = new User();
    createdUser.setUserId(USER_ID);
    createdUser.setTenantId(TENANT_ID);
    LocalDateTime now = LocalDateTime.now();
    createdUser.setCreateDate(now);
    registrationStatisticsEvent =
        new RegistrationStatisticsEvent(
            registeredUser, createdUser, SESSION_ID, "alk", List.of("alk", "drogen"));
  }

  @Test
  public void getEventType_Should_ReturnEventTypeRegistration() {
    assertThat(registrationStatisticsEvent.getEventType(), is(EventType.REGISTRATION));
  }
}
