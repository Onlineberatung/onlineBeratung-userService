package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AssignSessionStatisticsEventTest {

  private AssignSessionStatisticsEvent assignSessionStatisticsEvent;

  @BeforeEach
  public void setup() throws NoSuchFieldException, IllegalAccessException {
    assignSessionStatisticsEvent =
        new AssignSessionStatisticsEvent(CONSULTANT_ID, UserRole.CONSULTANT, SESSION_ID);
  }

  @Test
  public void getEventType_Should_ReturnEventTypeCreateMessage() {

    assertThat(assignSessionStatisticsEvent.getEventType(), is(EventType.ASSIGN_SESSION));
  }

  @Test
  public void getPayload_Should_ReturnValidJsonPayload() {

    String expectedJson =
        "{"
            + "  \"userId\":\""
            + CONSULTANT_ID
            + "\","
            + "  \"userRole\":\""
            + UserRole.CONSULTANT
            + "\","
            + "  \"sessionId\":"
            + SESSION_ID
            + ","
            + "  \"timestamp\":\""
            + OffsetDateTime.now(ZoneOffset.UTC)
            + "\","
            + "  \"eventType\":\""
            + EventType.ASSIGN_SESSION
            + "\""
            + "}";

    Optional<String> result = assignSessionStatisticsEvent.getPayload();

    assertThat(result.isPresent(), is(true));
    assertThat(
        result.get(),
        jsonEquals(expectedJson)
            .whenIgnoringPaths("timestamp", "requestReferer", "requestUri", "requestUserId"));
  }
}
